/**
 * NEXUS ARENA - Public Portal Frontend Engine
 * Powered by Firebase Cloud Firestore & Spring Boot REST API
 */

const FIREBASE_CONFIG = {
    apiKey: "AIzaSyB3JO7IRHCdWEHvW5TTf1sNsCdyuoLs1IU",
    authDomain: "nexusarena-esports.firebaseapp.com",
    projectId: "nexusarena-esports",
    storageBucket: "nexusarena-esports.firebasestorage.app",
    messagingSenderId: "699674120198",
    appId: "1:699674120198:web:0a253d20dfdec870563387"
};

let db = null;
if (typeof firebase !== 'undefined') {
    try {
        if (!firebase.apps.length) {
            firebase.initializeApp(FIREBASE_CONFIG);
        }
        db = firebase.firestore();
        console.log("🔥 Initialized Firebase Cloud Firestore for Nexus Arena.");
    } catch (e) {
        console.warn("Could not init Firebase SDK:", e);
    }
}

const API_BASE = window.location.origin;

let allTournaments = [];
let currentTournament = null;
let currentMatches = [];
let currentTeams = [];
let activeGameFilter = 'ALL';
let unsubscribeMatches = null;

// DOM Elements
const tournamentSelect = document.getElementById('tournament-select');
const tournamentsListEl = document.getElementById('tournaments-list');
const bracketTreeContainer = document.getElementById('bracket-tree-container');
const standingsTbody = document.getElementById('standings-tbody');
const matchesGrid = document.getElementById('matches-grid');
const teamsGrid = document.getElementById('teams-grid');

// Hero elements
const heroTitle = document.getElementById('hero-title');
const heroDesc = document.getElementById('hero-desc');
const heroGameBadge = document.getElementById('hero-game-badge');
const heroStatusBadge = document.getElementById('hero-status-badge');
const heroModeBadge = document.getElementById('hero-mode-badge');
const heroTeamCount = document.getElementById('hero-team-count');
const heroMatchesCount = document.getElementById('hero-matches-count');

// ==========================================
// INITIALIZATION
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    setupFilters();
    setupModals();
    loadTournaments();

    // Live sync polling every 5 seconds (safety fallback)
    setInterval(() => {
        if (currentTournament) {
            refreshTournamentData(false);
        }
    }, 5000);
});

function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            const tabId = link.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    document.getElementById('btn-refresh').addEventListener('click', () => {
        refreshTournamentData(true);
    });

    document.getElementById('btn-open-register').addEventListener('click', () => {
        if (!currentTournament) return;
        document.getElementById('reg-tournament-name').value = currentTournament.name;
        document.getElementById('reg-tournament-id').value = currentTournament.id;
        openModal('modal-register');
    });

    tournamentSelect.addEventListener('change', (e) => {
        selectTournament(e.target.value);
    });

    const userBtn = document.getElementById('btn-user-action');
    if (userBtn) {
        userBtn.addEventListener('click', () => {
            const savedUser = localStorage.getItem('nexus_user');
            if (savedUser) {
                // Logout action
                localStorage.removeItem('nexus_user');
                userBtn.textContent = 'Sign In';
                showToast('Signed out successfully.');
            } else {
                openModal('modal-auth');
            }
        });

        // Initialize user button if previously signed in
        const savedUser = localStorage.getItem('nexus_user');
        if (savedUser) {
            try {
                const u = JSON.parse(savedUser);
                userBtn.textContent = u.username + ' (' + u.role + ')';
            } catch (ignored) {}
        }
    }
}

function switchTab(tabId) {
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

    const activeBtn = document.querySelector(`.nav-link[data-tab="${tabId}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    const activePane = document.getElementById(tabId);
    if (activePane) activePane.classList.add('active');
}

function setupFilters() {
    const chips = document.querySelectorAll('.filter-chip');
    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            chips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            activeGameFilter = chip.getAttribute('data-filter');
            renderTournamentsList();
        });
    });
}

// ==========================================
// DATA FETCHING (Cloud Firestore & REST)
// ==========================================
async function loadTournaments() {
    if (db) {
        try {
            const snap = await db.collection('tournaments').get();
            const list = [];
            snap.forEach(doc => {
                const data = doc.data();
                data.id = doc.id;
                list.push(data);
            });
            if (list.length > 0) {
                allTournaments = list;
                populateTournamentSelector();
                renderTournamentsList();
                if (!currentTournament) {
                    selectTournament(allTournaments[0].id);
                }
                const sEl = document.getElementById('server-status');
                if (sEl) sEl.textContent = 'Cloud Firestore Live (Synced)';
                return;
            }
        } catch (e) {
            console.warn("Firestore direct load failed, falling back to REST:", e);
        }
    }

    try {
        const res = await fetch(`${API_BASE}/api/tournaments`);
        const json = await res.json();
        if (json.success && json.data) {
            allTournaments = json.data;
            populateTournamentSelector();
            renderTournamentsList();

            if (allTournaments.length > 0 && !currentTournament) {
                selectTournament(allTournaments[0].id);
            }
            const sEl = document.getElementById('server-status');
            if (sEl) sEl.textContent = 'Connected (REST API)';
        }
    } catch (err) {
        console.error('Failed to load tournaments:', err);
        showToast('Connecting to Nexus Arena cloud...', 'info');
    }
}

async function selectTournament(tournamentId) {
    currentTournament = allTournaments.find(t => t.id === tournamentId);
    if (!currentTournament) return;

    tournamentSelect.value = tournamentId;
    updateHeroBanner();
    await refreshTournamentData(false);

    // Setup real-time listener for current tournament matches
    if (db) {
        if (unsubscribeMatches) unsubscribeMatches();
        unsubscribeMatches = db.collection('matches')
            .where('tournamentId', '==', tournamentId)
            .onSnapshot(snap => {
                const list = [];
                snap.forEach(d => {
                    const item = d.data();
                    item.id = d.id;
                    list.push(item);
                });
                currentMatches = list;
                heroMatchesCount.textContent = currentMatches.length;
                renderBracket();
                renderStandings();
                renderMatchesSchedule();
            }, err => {
                console.warn('Real-time match listener warning:', err);
            });
    }
}

async function refreshTournamentData(showNotice = false) {
    if (!currentTournament) return;

    if (db) {
        try {
            const [tDoc, mSnap, tmSnap] = await Promise.all([
                db.collection('tournaments').doc(currentTournament.id).get(),
                db.collection('matches').where('tournamentId', '==', currentTournament.id).get(),
                db.collection('teams').where('tournamentId', '==', currentTournament.id).get()
            ]);

            if (tDoc.exists) {
                const d = tDoc.data();
                d.id = tDoc.id;
                currentTournament = d;
            }

            const mList = [];
            mSnap.forEach(doc => {
                const item = doc.data();
                item.id = doc.id;
                mList.push(item);
            });
            currentMatches = mList;

            const tmList = [];
            tmSnap.forEach(doc => {
                const item = doc.data();
                item.id = doc.id;
                tmList.push(item);
            });
            currentTeams = tmList;

            updateHeroBanner();
            renderBracket();
            renderStandings();
            renderMatchesSchedule();
            renderTeams();
            populateDisputeSelect();

            if (showNotice) {
                showToast('Synchronized with Cloud Firestore!');
            }
            return;
        } catch (e) {
            console.warn("Firestore refresh failed, fallback to REST:", e);
        }
    }

    try {
        const [tRes, mRes, tmRes] = await Promise.all([
            fetch(`${API_BASE}/api/tournaments/${currentTournament.id}`).then(r => r.json()),
            fetch(`${API_BASE}/api/tournaments/${currentTournament.id}/matches`).then(r => r.json()),
            fetch(`${API_BASE}/api/tournaments/${currentTournament.id}/teams`).then(r => r.json())
        ]);

        if (tRes.success) currentTournament = tRes.data;
        if (mRes.success) currentMatches = mRes.data;
        if (tmRes.success) currentTeams = tmRes.data;

        updateHeroBanner();
        renderBracket();
        renderStandings();
        renderMatchesSchedule();
        renderTeams();
        populateDisputeSelect();

        if (showNotice) {
            showToast('Tournament data synchronized!');
        }
    } catch (err) {
        console.warn('Sync failed:', err);
    }
}

// ==========================================
// RENDERERS
// ==========================================
function updateHeroBanner() {
    if (!currentTournament) return;
    heroTitle.textContent = currentTournament.name;
    heroDesc.textContent = currentTournament.description || 'Official championship tournament.';
    heroGameBadge.textContent = currentTournament.game.toUpperCase();
    heroStatusBadge.textContent = '● ' + currentTournament.status.replace('_', ' ');
    heroModeBadge.textContent = formatMode(currentTournament.mode);

    heroTeamCount.textContent = currentTeams.length;
    heroMatchesCount.textContent = currentMatches.length;

    const liveMatch = currentMatches.find(m => m.status === 'LIVE');
    const heroRoomEl = document.getElementById('hero-room');
    if (liveMatch && liveMatch.roomId) {
        heroRoomEl.textContent = liveMatch.roomId;
    } else {
        heroRoomEl.textContent = 'WAITING';
    }

    const summaryText = document.getElementById('tournament-summary-text');
    summaryText.innerHTML = `Status: <span class="badge ${getStatusBadgeClass(currentTournament.status)}">${currentTournament.status.replace('_', ' ')}</span>`;
}

function populateTournamentSelector() {
    tournamentSelect.innerHTML = allTournaments.map(t => `
        <option value="${t.id}">${t.name} (${t.game} - ${t.status})</option>
    `).join('');
}

function renderTournamentsList() {
    const filtered = allTournaments.filter(t => {
        if (activeGameFilter === 'ALL') return true;
        return t.game.toLowerCase().includes(activeGameFilter.toLowerCase());
    });

    tournamentsListEl.innerHTML = filtered.map(t => {
        const isSelected = currentTournament && currentTournament.id === t.id;
        return `
            <div class="tournament-card ${isSelected ? 'active-selected' : ''}" onclick="selectTournament('${t.id}'); switchTab('bracket-view')">
                <div>
                    <div class="t-card-header">
                        <span class="badge badge-purple">${t.game}</span>
                        <span class="badge ${getStatusBadgeClass(t.status)}">${t.status.replace('_', ' ')}</span>
                    </div>
                    <div class="t-card-title">${t.name}</div>
                    <div class="t-card-desc">${t.description || 'No description provided.'}</div>
                </div>
                <div class="t-card-meta">
                    <span>Format: ${formatMode(t.mode)}</span>
                    <span>Max: ${t.maxParticipants} Teams</span>
                </div>
            </div>
        `;
    }).join('');
}

function renderBracket() {
    if (!bracketTreeContainer) return;

    if (currentTournament.mode === 'BATTLE_ROYALE') {
        bracketTreeContainer.innerHTML = `
            <div style="text-align: center; margin: 3rem auto; color: var(--text-secondary);">
                <p style="font-size: 1.2rem; font-family: var(--font-heading); color: var(--neon-cyan);">BATTLE ROYALE TOURNAMENT FORMAT</p>
                <p style="margin-top: 0.5rem;">Battle Royale lobbies compete simultaneously. Check the <strong>Standings</strong> tab for leaderboard scores and round placements.</p>
                <button class="btn btn-primary" style="margin-top: 1rem;" onclick="switchTab('standings-view')">View Standings</button>
            </div>
        `;
        return;
    }

    if (currentMatches.length === 0) {
        bracketTreeContainer.innerHTML = `
            <div style="text-align: center; margin: 3rem auto; color: var(--text-muted);">
                <p style="font-family: var(--font-heading); font-size: 1.1rem;">Brackets have not been generated yet.</p>
                <p style="font-size: 0.85rem; margin-top: 0.4rem;">The tournament organizer will generate the bracket once registration and check-in close.</p>
            </div>
        `;
        return;
    }

    // Group matches by round
    const roundsMap = {};
    currentMatches.forEach(m => {
        if (!roundsMap[m.round]) roundsMap[m.round] = [];
        roundsMap[m.round].push(m);
    });

    const roundNumbers = Object.keys(roundsMap).map(Number).sort((a, b) => a - b);

    let html = '';
    roundNumbers.forEach(rNum => {
        const matchesInRound = roundsMap[rNum];
        const roundTitle = matchesInRound[0]?.roundName || `Round ${rNum}`;

        html += `
            <div class="bracket-round">
                <div class="round-header">${roundTitle}</div>
                <div class="round-matches">
                    ${matchesInRound.map(m => renderMatchCard(m)).join('')}
                </div>
            </div>
        `;
    });

    bracketTreeContainer.innerHTML = html;
}

function renderMatchCard(match) {
    const isLive = match.status === 'LIVE';
    const isComplete = match.status === 'COMPLETE';
    const isBye = match.bye;

    const winnerA = isComplete && match.winnerId === match.teamAId;
    const winnerB = isComplete && match.winnerId === match.teamBId;

    return `
        <div class="match-card ${isLive ? 'is-live' : ''}">
            <div class="match-card-header">
                <span>MATCH #${match.matchNumber}</span>
                <span class="badge ${getStatusBadgeClass(match.status)}">${match.status}</span>
            </div>
            <div class="match-team ${winnerA ? 'winner' : ''}">
                <span class="team-name">
                    ${winnerA ? '👑 ' : ''}${match.teamAName || 'TBD'}
                </span>
                <span class="team-score">${isComplete || isLive ? match.scoreA : '-'}</span>
            </div>
            <div class="match-team ${winnerB ? 'winner' : ''}">
                <span class="team-name">
                    ${winnerB ? '👑 ' : ''}${match.teamBName || 'TBD'}
                </span>
                <span class="team-score">${isComplete || isLive ? match.scoreB : '-'}</span>
            </div>
        </div>
    `;
}

function renderStandings() {
    if (!standingsTbody) return;

    if (currentTournament.mode === 'BATTLE_ROYALE') {
        // Collect points from BR placements across all matches
        const teamStats = {};

        currentMatches.forEach(m => {
            if (m.brPlacements) {
                m.brPlacements.forEach(p => {
                    if (!teamStats[p.teamId]) {
                        teamStats[p.teamId] = {
                            teamId: p.teamId,
                            name: p.teamName,
                            matches: 0,
                            kills: 0,
                            placementPts: 0,
                            killPts: 0,
                            totalPts: 0
                        };
                    }
                    teamStats[p.teamId].matches += 1;
                    teamStats[p.teamId].kills += p.kills;
                    teamStats[p.teamId].placementPts += p.placementPoints;
                    teamStats[p.teamId].killPts += p.killPoints;
                    teamStats[p.teamId].totalPts += p.totalPoints;
                });
            }
        });

        const list = Object.values(teamStats).sort((a, b) => b.totalPts - a.totalPts);

        if (list.length === 0) {
            standingsTbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No Battle Royale round results recorded yet.</td></tr>`;
            return;
        }

        standingsTbody.innerHTML = list.map((st, idx) => {
            const rank = idx + 1;
            const rankClass = rank === 1 ? 'rank-1' : rank === 2 ? 'rank-2' : rank === 3 ? 'rank-3' : '';
            return `
                <tr>
                    <td><span class="rank-badge ${rankClass}">${rank}</span></td>
                    <td style="font-weight: 700; color: #fff;">${st.name}</td>
                    <td>${st.matches}</td>
                    <td>${st.kills}</td>
                    <td>${st.placementPts}</td>
                    <td>${st.killPts}</td>
                    <td style="font-weight: 700; color: var(--neon-cyan);">${st.totalPts}</td>
                    <td><span class="badge ${rank === 1 ? 'badge-live' : 'badge-dark'}">${rank === 1 ? 'LEADER' : 'CONTENDER'}</span></td>
                </tr>
            `;
        }).join('');
    } else {
        // Single elimination standings based on bracket progress
        const teams = [...currentTeams];
        standingsTbody.innerHTML = teams.map((tm, idx) => {
            const rank = idx + 1;
            return `
                <tr>
                    <td><span class="rank-badge">${rank}</span></td>
                    <td style="font-weight: 700; color: #fff;">${tm.name} <span style="color: var(--neon-cyan);">[${tm.tag}]</span></td>
                    <td>-</td>
                    <td>-</td>
                    <td>-</td>
                    <td>-</td>
                    <td style="font-weight: 700; color: var(--neon-green);">${tm.seed ? 'Seed #' + tm.seed : '-'}</td>
                    <td><span class="badge ${tm.checkedIn ? 'badge-purple' : 'badge-dark'}">${tm.checkedIn ? 'CHECKED IN' : 'REGISTERED'}</span></td>
                </tr>
            `;
        }).join('');
    }
}

function renderMatchesSchedule() {
    if (!matchesGrid) return;
    if (currentMatches.length === 0) {
        matchesGrid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 2rem;">No matches scheduled yet.</div>`;
        return;
    }

    matchesGrid.innerHTML = currentMatches.map(m => `
        <div class="match-info-card">
            <div class="match-info-top">
                <span class="badge badge-purple">${m.roundName} • #${m.matchNumber}</span>
                <span class="badge ${getStatusBadgeClass(m.status)}">${m.status}</span>
            </div>
            
            <div class="match-versus">
                <div class="team-box">
                    <div class="team-box-name">${m.teamAName || 'TBD'}</div>
                    <div style="font-size: 1.2rem; font-weight: 700; color: var(--neon-green);">${m.scoreA}</div>
                </div>
                <div class="vs-pill">VS</div>
                <div class="team-box">
                    <div class="team-box-name">${m.teamBName || 'TBD'}</div>
                    <div style="font-size: 1.2rem; font-weight: 700; color: var(--neon-green);">${m.scoreB}</div>
                </div>
            </div>

            <div style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.4rem;">
                <span>🕒 Schedule: <strong>${m.scheduledTime || 'TBD'}</strong></span> | 
                <span>🗺️ Map: <strong>${m.mapName || 'Ascent'}</strong></span>
            </div>

            <div class="room-details-box">
                <span>Room ID: <strong>${m.roomId || 'NX-ROOM-PENDING'}</strong></span>
                <span>Password: <strong>${m.roomPassword || '••••••••'}</strong></span>
            </div>
        </div>
    `).join('');
}

function renderTeams() {
    if (!teamsGrid) return;
    if (currentTeams.length === 0) {
        teamsGrid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 2rem;">No teams registered yet.</div>`;
        return;
    }

    teamsGrid.innerHTML = currentTeams.map(t => `
        <div class="team-card">
            <div class="team-card-header">
                <span style="font-family: var(--font-heading); font-size: 1.15rem; font-weight: 700; color: #fff;">${t.name}</span>
                <span class="team-card-tag">${t.tag}</span>
            </div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">
                Captain: <strong style="color: var(--text-secondary);">${t.captainEmail || 'N/A'}</strong>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 0.75rem;">
                <span class="badge ${t.checkedIn ? 'badge-live' : 'badge-dark'}">${t.checkedIn ? 'READY' : 'PENDING CHECK-IN'}</span>
                <span class="badge badge-purple">SEED #${t.seed || 1}</span>
            </div>
            <div class="roster-tags">
                ${(t.playerNames || []).map(p => `<span class="player-tag">${p}</span>`).join('')}
            </div>
        </div>
    `).join('');
}

function populateDisputeSelect() {
    const sel = document.getElementById('disp-match-select');
    if (!sel) return;
    sel.innerHTML = currentMatches.map(m => `
        <option value="${m.id}">Match #${m.matchNumber}: ${m.teamAName} vs ${m.teamBName} (${m.roundName})</option>
    `).join('');
}

// ==========================================
// MODALS & FORMS
// ==========================================
function setupModals() {
    // Register Team Form
    const regForm = document.getElementById('form-register-team');
    if (regForm) {
        regForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const tournamentId = document.getElementById('reg-tournament-id').value;
            const name = document.getElementById('reg-team-name').value;
            const tag = document.getElementById('reg-team-tag').value;
            const captainTag = document.getElementById('reg-captain-name').value;
            const captainEmail = document.getElementById('reg-captain-email').value;
            const playersRaw = document.getElementById('reg-players').value;

            const playerNames = playersRaw.split(',').map(s => s.trim()).filter(Boolean);

            const payload = {
                tournamentId,
                name,
                tag,
                captainId: captainTag,
                captainEmail,
                playerNames
            };

            if (db) {
                try {
                    const teamId = 'tm-' + Math.random().toString(36).substring(2, 9);
                    payload.id = teamId;
                    payload.checkedIn = true;
                    payload.seed = currentTeams.length + 1;
                    payload.registrationStatus = 'APPROVED';
                    await db.collection('teams').doc(teamId).set(payload);
                    showToast('Team registered successfully in Cloud Firestore!');
                    closeModal('modal-register');
                    regForm.reset();
                    await refreshTournamentData(false);
                    return;
                } catch (err) {
                    console.warn('Firestore team registration fallback to REST:', err);
                }
            }

            try {
                const res = await fetch(`${API_BASE}/api/tournaments/${tournamentId}/teams`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (json.success) {
                    showToast('Team registered successfully! Awaiting organizer approval.');
                    closeModal('modal-register');
                    regForm.reset();
                    refreshTournamentData(false);
                } else {
                    showToast(json.message || 'Registration failed', 'error');
                }
            } catch (err) {
                showToast('Failed to connect to backend', 'error');
            }
        });
    }

    // Dispute Form
    const dispForm = document.getElementById('form-dispute');
    if (dispForm) {
        dispForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!currentTournament) return;
            const matchId = document.getElementById('disp-match-select').value;
            const teamName = document.getElementById('disp-team-name').value;
            const reason = document.getElementById('disp-reason').value;
            const evidenceUrl = document.getElementById('disp-evidence').value;

            const payload = {
                tournamentId: currentTournament.id,
                matchId,
                submittedByTeamName: teamName,
                reason,
                evidenceUrl
            };

            if (db) {
                try {
                    const dispId = 'disp-' + Math.random().toString(36).substring(2, 8);
                    payload.id = dispId;
                    payload.status = 'UNDER_REVIEW';
                    payload.createdAt = new Date().toISOString();
                    await db.collection('disputes').doc(dispId).set(payload);
                    showToast('Dispute filed to Cloud Firestore! Organizer alerted.');
                    closeModal('modal-dispute');
                    dispForm.reset();
                    await refreshTournamentData(false);
                    return;
                } catch (err) {
                    console.warn('Firestore dispute fallback to REST:', err);
                }
            }

            try {
                const res = await fetch(`${API_BASE}/api/tournaments/${currentTournament.id}/disputes`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (json.success) {
                    showToast('Dispute filed! Organizer notified.');
                    closeModal('modal-dispute');
                    dispForm.reset();
                    refreshTournamentData(false);
                } else {
                    showToast(json.message || 'Error filing dispute', 'error');
                }
            } catch (err) {
                showToast('Error filing dispute', 'error');
            }
        });
    }

    // Report Form
    const repForm = document.getElementById('form-report');
    if (repForm) {
        repForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!currentTournament) return;
            const playerName = document.getElementById('rep-player-name').value;
            const teamName = document.getElementById('rep-team-name').value;
            const category = document.getElementById('rep-category').value;
            const desc = document.getElementById('rep-desc').value;
            const evidenceUrl = document.getElementById('rep-evidence').value;

            const payload = {
                tournamentId: currentTournament.id,
                reportedPlayerName: playerName,
                reportedTeamName: teamName,
                category,
                description: desc,
                evidenceUrl
            };

            if (db) {
                try {
                    const repId = 'rep-' + Math.random().toString(36).substring(2, 8);
                    payload.id = repId;
                    payload.status = 'SUBMITTED';
                    payload.moderationAction = 'NONE';
                    payload.createdAt = new Date().toISOString();
                    await db.collection('conduct_reports').doc(repId).set(payload);
                    showToast('Report submitted to Cloud Firestore for fair-play investigation.');
                    closeModal('modal-report');
                    repForm.reset();
                    return;
                } catch (err) {
                    console.warn('Firestore report fallback to REST:', err);
                }
            }

            try {
                const res = await fetch(`${API_BASE}/api/tournaments/${currentTournament.id}/conduct-reports`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (json.success) {
                    showToast('Report submitted for fair-play investigation.');
                    closeModal('modal-report');
                    repForm.reset();
                } else {
                    showToast(json.message || 'Error submitting report', 'error');
                }
            } catch (err) {
                showToast('Error submitting report', 'error');
            }
        });
    }

    // Auth / Sign In Form
    const authForm = document.getElementById('form-auth');
    if (authForm) {
        authForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('auth-username').value;
            const password = document.getElementById('auth-password').value;

            if (db) {
                try {
                    const q = await db.collection('users').where('username', '==', username).limit(1).get();
                    if (!q.empty) {
                        const userData = q.docs[0].data();
                        userData.id = q.docs[0].id;
                        if (userData.password === password) {
                            localStorage.setItem('nexus_user', JSON.stringify(userData));
                            const userBtn = document.getElementById('btn-user-action');
                            if (userBtn) {
                                userBtn.textContent = userData.username + ' (' + userData.role + ')';
                            }
                            showToast('Welcome back, ' + userData.username + ' (Cloud Authenticated)!');
                            closeModal('modal-auth');
                            authForm.reset();
                            return;
                        }
                    }
                } catch (err) {
                    console.warn('Firestore auth fallback to REST:', err);
                }
            }

            try {
                const res = await fetch(`${API_BASE}/api/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });
                const json = await res.json();
                if (json.success && json.data) {
                    localStorage.setItem('nexus_user', JSON.stringify(json.data));
                    const userBtn = document.getElementById('btn-user-action');
                    if (userBtn) {
                        userBtn.textContent = json.data.username + ' (' + json.data.role + ')';
                    }
                    showToast('Welcome back, ' + json.data.username + '!');
                    closeModal('modal-auth');
                    authForm.reset();
                } else {
                    showToast(json.message || 'Invalid credentials', 'error');
                }
            } catch (err) {
                showToast('Failed to authenticate with backend', 'error');
            }
        });
    }
}

function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('open');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
}

function openDisputeModal() {
    populateDisputeSelect();
    openModal('modal-dispute');
}

function openReportModal() {
    openModal('modal-report');
}

function downloadCsv() {
    if (!currentTournament) return;
    window.open(`${API_BASE}/api/tournaments/${currentTournament.id}/export/csv`, '_blank');
}

// Helpers
function formatMode(mode) {
    if (!mode) return 'Standard';
    if (mode === 'SINGLE_ELIMINATION_TDM') return 'Single Elimination (TDM)';
    if (mode === 'SINGLE_ELIMINATION_CLASH') return 'Single Elimination (Clash)';
    if (mode === 'BATTLE_ROYALE') return 'Battle Royale Standings';
    return mode;
}

function getStatusBadgeClass(status) {
    if (status === 'IN_PROGRESS' || status === 'LIVE') return 'badge-live';
    if (status === 'REGISTRATION_OPEN' || status === 'CHECK_IN') return 'badge-purple';
    if (status === 'COMPLETED' || status === 'COMPLETE') return 'badge-dark';
    return 'badge-dark';
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = (type === 'error' ? '⚠️ ' : '⚡ ') + message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}
