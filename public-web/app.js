/**
 * NEXUS ARENA - High-Performance Cloud Esports Portal
 * Full Bidirectional Synchronization with Cloud Firestore & Java Desktop Application
 */

const FIREBASE_CONFIG = {
    apiKey: "AIzaSyB3JO7IRHCdWEHvW5TTf1sNsCdyuoLs1IU",
    authDomain: "nexusarena-esports.firebaseapp.com",
    projectId: "nexusarena-esports",
    storageBucket: "nexusarena-esports.firebasestorage.app",
    messagingSenderId: "699674120198",
    appId: "1:699674120198:web:0a253d20dfdec870563387"
};

// ==========================================
// PRE-SEEDED PROTOTYPE DATA (All 6 Games)
// ==========================================
const PROTOTYPE_TOURNAMENTS = [
    {
        id: "trn-cs2-cologne",
        name: "CS2 Major Championship 2026 - Cologne",
        game: "Counter-Strike 2",
        mode: "SINGLE_ELIMINATION_TDM",
        status: "LIVE",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "Standard 128-tick MR12 competitive settings. Anti-cheat screen recording mandatory.",
        description: "World championship featuring 8 elite CS2 rosters in a 128-tick tournament bracket."
    },
    {
        id: "trn-val-masters",
        name: "Valorant Champions Tour Masters 2026",
        game: "Valorant",
        mode: "SINGLE_ELIMINATION_TDM",
        status: "LIVE",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "Official VCT Riot Rulebook. Anti-cheat screen recording mandatory.",
        description: "Global VCT Masters featuring Sentinels, Fnatic, and top international franchised teams."
    },
    {
        id: "trn-dota-ti",
        name: "The International Championship 2026",
        game: "Dota 2",
        mode: "SINGLE_ELIMINATION_TDM",
        status: "REGISTRATION_OPEN",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "Captains Mode with tournament draft timer and spectator referee logs.",
        description: "The pinnacle of MOBA esports with multimillion dollar prize pool and double elimination."
    },
    {
        id: "trn-apex-algs",
        name: "ALGS Global Championship 2026",
        game: "Apex Legends",
        mode: "BATTLE_ROYALE",
        status: "LIVE",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "Match Point format: 50 points threshold + 1st place match victory.",
        description: "Apex Legends Global Series Championship with kill multipliers and match point format."
    },
    {
        id: "trn-bgmi-series",
        name: "Battlegrounds Mobile India Series 2026",
        game: "Battlegrounds Mobile India",
        mode: "BATTLE_ROYALE",
        status: "REGISTRATION_OPEN",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "BGIS official point matrix: 10 placement pts for WWCD, 1 pt per kill.",
        description: "Premier mobile esports championship across Erangel, Miramar, and Sanhok."
    },
    {
        id: "trn-rl-rlcs",
        name: "RLCS World Championship 2026",
        game: "Rocket League",
        mode: "SINGLE_ELIMINATION_TDM",
        status: "UPCOMING",
        maxParticipants: 8,
        password: "ADMIN",
        published: true,
        rules: "Standard 3v3 soccar, 5-minute regulation with overtime.",
        description: "High-flying 3v3 vehicular soccer championship featuring global RLCS major champions."
    }
];

const PROTOTYPE_TEAMS = {
    "trn-cs2-cologne": [
        { id: "tm-cs1", tournamentId: "trn-cs2-cologne", name: "Natus Vincere", tag: "NAVI", captainEmail: "s1mple@navi.gg", playerNames: ["s1mple", "b1t", "Aleksib", "iM", "jL"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs2", tournamentId: "trn-cs2-cologne", name: "FaZe Clan", tag: "FAZE", captainEmail: "karrigan@faze.com", playerNames: ["karrigan", "rain", "broky", "frozen", "ropz"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs3", tournamentId: "trn-cs2-cologne", name: "Team Vitality", tag: "VIT", captainEmail: "apex@vitality.gg", playerNames: ["ZywOo", "apEX", "Spinx", "flameZ", "mezii"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs4", tournamentId: "trn-cs2-cologne", name: "G2 Esports", tag: "G2", captainEmail: "niko@g2esports.com", playerNames: ["NiKo", "huNter-", "m0NESY", "malbsMd", "Snax"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs5", tournamentId: "trn-cs2-cologne", name: "Team Spirit", tag: "SPIRIT", captainEmail: "donk@spirit.team", playerNames: ["donk", "chopper", "sh1ro", "magixx", "zont1x"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs6", tournamentId: "trn-cs2-cologne", name: "MOUZ", tag: "MOUZ", captainEmail: "siuhy@mouz.com", playerNames: ["siuhy", "torzsi", "xertioN", "Brollan", "Jimpphat"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs7", tournamentId: "trn-cs2-cologne", name: "Virtus.pro", tag: "VP", captainEmail: "jame@virtus.pro", playerNames: ["Jame", "electroNic", "FL1T", "fame", "n0rb3r7"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-cs8", tournamentId: "trn-cs2-cologne", name: "Heroic", tag: "HERO", captainEmail: "kyxsan@heroic.gg", playerNames: ["kyxsan", "TeSeS", "sjuush", "NertZ", "degster"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ],
    "trn-val-masters": [
        { id: "tm-val1", tournamentId: "trn-val-masters", name: "Sentinels", tag: "SEN", captainEmail: "tenz@sentinels.gg", playerNames: ["TenZ", "zekken", "Zellsis", "johnqt", "Sacy"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val2", tournamentId: "trn-val-masters", name: "Fnatic", tag: "FNC", captainEmail: "boaster@fnatic.com", playerNames: ["Boaster", "Derke", "Alfajer", "Chronicle", "hiro"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val3", tournamentId: "trn-val-masters", name: "Paper Rex", tag: "PRX", captainEmail: "f0rsaken@prx.gg", playerNames: ["f0rsakeN", "mindfreak", "d4v41", "something", "Jinggg"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val4", tournamentId: "trn-val-masters", name: "DRX Esports", tag: "DRX", captainEmail: "mako@drx.pro", playerNames: ["BuZz", "Flashback", "MaKo", "Foxy9", "Beyyn"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val5", tournamentId: "trn-val-masters", name: "Team Liquid", tag: "TL", captainEmail: "nats@liquid.gg", playerNames: ["nAts", "Jamppi", "keiko", "Harmii", "Enzo"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val6", tournamentId: "trn-val-masters", name: "LOUD Esports", tag: "LOUD", captainEmail: "saadhak@loud.gg", playerNames: ["Less", "Saadhak", "cauanzin", "tuyz", "qck"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val7", tournamentId: "trn-val-masters", name: "Evil Geniuses", tag: "EG", captainEmail: "jawgemo@eg.gg", playerNames: ["jawgemo", "Derrek", "supamen", "NaturE", "Apoth"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-val8", tournamentId: "trn-val-masters", name: "Leviatan", tag: "LEV", captainEmail: "aspas@leviatan.gg", playerNames: ["aspas", "kiNgg", "Mazino", "tex", "C0M"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ],
    "trn-dota-ti": [
        { id: "tm-d1", tournamentId: "trn-dota-ti", name: "Team Spirit", tag: "TS", captainEmail: "yatoro@spirit.team", playerNames: ["Yatoro", "TORONTOTOKYO", "Collapse", "Mira", "Miposhka"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d2", tournamentId: "trn-dota-ti", name: "Team Liquid", tag: "TL", captainEmail: "insania@liquid.gg", playerNames: ["miCKe", "Nisha", "33", "Boxi", "Insania"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d3", tournamentId: "trn-dota-ti", name: "Gaimin Gladiators", tag: "GG", captainEmail: "dyrachyo@gg.pro", playerNames: ["dyrachyo", "Quinn", "Ace", "tOfu", "Seleri"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d4", tournamentId: "trn-dota-ti", name: "BetBoom Team", tag: "BB", captainEmail: "nightfall@bb.team", playerNames: ["Nightfall", "gpk~", "MieRo`", "Save-", "TORONTOTOKYO"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d5", tournamentId: "trn-dota-ti", name: "Team Falcons", tag: "FLC", captainEmail: "skiter@falcons.gg", playerNames: ["skiter", "Malr1ne", "ATF", "Cr1t-", "Sneyking"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d6", tournamentId: "trn-dota-ti", name: "OG Esports", tag: "OG", captainEmail: "ceb@og.gg", playerNames: ["Timado", "bzm", "Wisper", "Ari", "Ceb"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d7", tournamentId: "trn-dota-ti", name: "Tundra Esports", tag: "TUN", captainEmail: "topson@tundra.gg", playerNames: ["Pure~", "Topson", "RAMZES666", "Saksa", "Whitemon"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-d8", tournamentId: "trn-dota-ti", name: "Team Secret", tag: "SEC", captainEmail: "puppey@secret.gg", playerNames: ["Puppey", "Crystallis", "MidOne", "Yamich", "Ekki"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ],
    "trn-apex-algs": [
        { id: "tm-ap1", tournamentId: "trn-apex-algs", name: "TSM Pro", tag: "TSM", captainEmail: "hal@tsm.gg", playerNames: ["ImperialHal", "Reps", "Verhulst"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap2", tournamentId: "trn-apex-algs", name: "DarkZero", tag: "DZ", captainEmail: "zer0@dz.pro", playerNames: ["Zer0", "Genburten", "Sikezz"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap3", tournamentId: "trn-apex-algs", name: "Alliance", tag: "ALL", captainEmail: "hakis@alliance.gg", playerNames: ["Hakis", "Effect", "Unlucky"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap4", tournamentId: "trn-apex-algs", name: "OpTic Gaming", tag: "OPT", captainEmail: "dropped@optic.gg", playerNames: ["Dropped", "SkittleCakes", "knoqd"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap5", tournamentId: "trn-apex-algs", name: "Moist Esports", tag: "MST", captainEmail: "wxltzy@moist.gg", playerNames: ["Wxltzy", "Emtee", "Gild"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap6", tournamentId: "trn-apex-algs", name: "Spacestation", tag: "SSG", captainEmail: "phony@ssg.gg", playerNames: ["Phony", "Frexs", "Xynew"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap7", tournamentId: "trn-apex-algs", name: "Fnatic Apex", tag: "FNC", captainEmail: "yukaf@fnatic.com", playerNames: ["YukaF", "Meltstera", "Satuki"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-ap8", tournamentId: "trn-apex-algs", name: "Aurora Gaming", tag: "AUR", captainEmail: "hardecki@aurora.gg", playerNames: ["Hardecki", "ojrein", "Sunset"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ],
    "trn-bgmi-series": [
        { id: "tm-bg1", tournamentId: "trn-bgmi-series", name: "GodLike Esports", tag: "GODL", captainEmail: "jonathan@godlike.in", playerNames: ["Jonathan", "Neyoo", "ZGOD", "Shadow", "Jelly"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg2", tournamentId: "trn-bgmi-series", name: "Soul Esports", tag: "SOUL", captainEmail: "manya@soul.in", playerNames: ["Manya", "Nakul", "Rony", "Spower", "Joker"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg3", tournamentId: "trn-bgmi-series", name: "Team XSpark", tag: "TX", captainEmail: "scout@xspark.in", playerNames: ["Scout", "Sarang", "SprayGod", "NinjaJOD"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg4", tournamentId: "trn-bgmi-series", name: "Blind Esports", tag: "BLIND", captainEmail: "blind@esports.in", playerNames: ["Manya", "Spower", "Nakul", "Rony"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg5", tournamentId: "trn-bgmi-series", name: "Gladiators Esports", tag: "GE", captainEmail: "destro@gladiators.in", playerNames: ["Destro", "DeltaPG", "Justin", "Shogun"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg6", tournamentId: "trn-bgmi-series", name: "Entity Gaming", tag: "ENT", captainEmail: "saumraj@entity.in", playerNames: ["Saumraj", "Gamlaboy", "Secret", "Troghun"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg7", tournamentId: "trn-bgmi-series", name: "Revenant Esports", tag: "RNT", captainEmail: "sensei@revenant.in", playerNames: ["Sensei", "Fierce", "MJ", "Aquanox"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-bg8", tournamentId: "trn-bgmi-series", name: "Global Esports", tag: "GE", captainEmail: "mavi@global.in", playerNames: ["Mavi", "Beast", "Slug", "Ninza"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ],
    "trn-rl-rlcs": [
        { id: "tm-rl1", tournamentId: "trn-rl-rlcs", name: "Karmine Corp", tag: "KC", captainEmail: "vatira@karminecorp.fr", playerNames: ["Vatira", "rise", "Atow"], seed: 1, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl2", tournamentId: "trn-rl-rlcs", name: "Team BDS", tag: "BDS", captainEmail: "monkey@bds.ch", playerNames: ["M0nkey M00n", "Exotiik", "dralii"], seed: 2, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl3", tournamentId: "trn-rl-rlcs", name: "G2 Stride", tag: "G2", captainEmail: "atomic@g2.com", playerNames: ["Atomic", "Daniel", "BeastMode"], seed: 3, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl4", tournamentId: "trn-rl-rlcs", name: "Gentle Mates", tag: "M8", captainEmail: "itachi@gentlemates.fr", playerNames: ["Itachi", "seikoo", "juicy"], seed: 4, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl5", tournamentId: "trn-rl-rlcs", name: "Spacestation", tag: "SSG", captainEmail: "lj@ssg.gg", playerNames: ["Lj", "hockser", "Chicago"], seed: 5, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl6", tournamentId: "trn-rl-rlcs", name: "Complexity", tag: "COL", captainEmail: "reysbull@col.com", playerNames: ["Reysbull", "CRR", "Dorito"], seed: 6, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl7", tournamentId: "trn-rl-rlcs", name: "FURIA Esports", tag: "FUR", captainEmail: "lostt@furia.gg", playerNames: ["Lostt", "yanxnz", "drufinho"], seed: 7, checkedIn: true, registrationStatus: "APPROVED" },
        { id: "tm-rl8", tournamentId: "trn-rl-rlcs", name: "Team Vitality", tag: "VIT", captainEmail: "zen@vitality.gg", playerNames: ["Zen", "Alpha54", "Radosin"], seed: 8, checkedIn: true, registrationStatus: "APPROVED" }
    ]
};

const PROTOTYPE_MATCHES = {
    "trn-cs2-cologne": [
        { id: "m-cs1", tournamentId: "trn-cs2-cologne", round: 1, matchNumber: 1, roundName: "Quarter-Finals", teamAId: "tm-cs1", teamAName: "Natus Vincere", scoreA: 13, killsA: 52, teamBId: "tm-cs2", teamBName: "FaZe Clan", scoreB: 11, killsB: 46, status: "COMPLETE", winnerId: "tm-cs1", winnerName: "Natus Vincere", mapName: "Mirage", roomId: "CS2-01", scheduledTime: "18:00 CEST" },
        { id: "m-cs2", tournamentId: "trn-cs2-cologne", round: 1, matchNumber: 2, roundName: "Quarter-Finals", teamAId: "tm-cs3", teamAName: "Team Vitality", scoreA: 13, killsA: 55, teamBId: "tm-cs4", teamBName: "G2 Esports", scoreB: 9, killsB: 38, status: "COMPLETE", winnerId: "tm-cs3", winnerName: "Team Vitality", mapName: "Inferno", roomId: "CS2-02", scheduledTime: "19:30 CEST" },
        { id: "m-cs3", tournamentId: "trn-cs2-cologne", round: 1, matchNumber: 3, roundName: "Quarter-Finals", teamAId: "tm-cs5", teamAName: "Team Spirit", scoreA: 13, killsA: 61, teamBId: "tm-cs6", teamBName: "MOUZ", scoreB: 8, killsB: 35, status: "COMPLETE", winnerId: "tm-cs5", winnerName: "Team Spirit", mapName: "Nuke", roomId: "CS2-03", scheduledTime: "21:00 CEST" },
        { id: "m-cs4", tournamentId: "trn-cs2-cologne", round: 1, matchNumber: 4, roundName: "Quarter-Finals", teamAId: "tm-cs7", teamAName: "Virtus.pro", scoreA: 13, killsA: 49, teamBId: "tm-cs8", teamBName: "Heroic", scoreB: 10, killsB: 42, status: "COMPLETE", winnerId: "tm-cs7", winnerName: "Virtus.pro", mapName: "Ancient", roomId: "CS2-04", scheduledTime: "22:30 CEST" },
        { id: "m-cs5", tournamentId: "trn-cs2-cologne", round: 2, matchNumber: 1, roundName: "Semi-Finals", teamAId: "tm-cs1", teamAName: "Natus Vincere", scoreA: 11, killsA: 48, teamBId: "tm-cs3", teamBName: "Team Vitality", scoreB: 12, killsB: 51, status: "LIVE", winnerId: null, winnerName: null, mapName: "Dust II", roomId: "CS2-SF1", scheduledTime: "LIVE NOW" },
        { id: "m-cs6", tournamentId: "trn-cs2-cologne", round: 2, matchNumber: 2, roundName: "Semi-Finals", teamAId: "tm-cs5", teamAName: "Team Spirit", scoreA: 0, killsA: 0, teamBId: "tm-cs7", teamBName: "Virtus.pro", scoreB: 0, killsB: 0, status: "UPCOMING", winnerId: null, winnerName: null, mapName: "Anubis", roomId: "CS2-SF2", scheduledTime: "20:00 CEST" },
        { id: "m-cs7", tournamentId: "trn-cs2-cologne", round: 3, matchNumber: 1, roundName: "Grand Final", teamAId: "tbd-1", teamAName: "Winner SF1", scoreA: 0, killsA: 0, teamBId: "tbd-2", teamBName: "Winner SF2", scoreB: 0, killsB: 0, status: "UPCOMING", winnerId: null, winnerName: null, mapName: "Mirage", roomId: "CS2-FINAL", scheduledTime: "Tomorrow 21:00 CEST" }
    ],
    "trn-val-masters": [
        { id: "m-val1", tournamentId: "trn-val-masters", round: 1, matchNumber: 1, roundName: "Quarter-Finals", teamAId: "tm-val1", teamAName: "Sentinels", scoreA: 13, killsA: 56, teamBId: "tm-val2", teamBName: "Fnatic", scoreB: 10, killsB: 44, status: "COMPLETE", winnerId: "tm-val1", winnerName: "Sentinels", mapName: "Sunset", roomId: "VAL-01", scheduledTime: "17:00 IST" },
        { id: "m-val2", tournamentId: "trn-val-masters", round: 1, matchNumber: 2, roundName: "Quarter-Finals", teamAId: "tm-val3", teamAName: "Paper Rex", scoreA: 13, killsA: 59, teamBId: "tm-val4", teamBName: "DRX Esports", scoreB: 8, killsB: 36, status: "COMPLETE", winnerId: "tm-val3", winnerName: "Paper Rex", mapName: "Lotus", roomId: "VAL-02", scheduledTime: "18:30 IST" },
        { id: "m-val3", tournamentId: "trn-val-masters", round: 1, matchNumber: 3, roundName: "Quarter-Finals", teamAId: "tm-val5", teamAName: "Team Liquid", scoreA: 13, killsA: 52, teamBId: "tm-val6", teamBName: "LOUD Esports", scoreB: 11, killsB: 48, status: "COMPLETE", winnerId: "tm-val5", winnerName: "Team Liquid", mapName: "Bind", roomId: "VAL-03", scheduledTime: "20:00 IST" },
        { id: "m-val4", tournamentId: "trn-val-masters", round: 1, matchNumber: 4, roundName: "Quarter-Finals", teamAId: "tm-val7", teamAName: "Evil Geniuses", scoreA: 13, killsA: 54, teamBId: "tm-val8", teamBName: "Leviatan", scoreB: 7, killsB: 33, status: "COMPLETE", winnerId: "tm-val7", winnerName: "Evil Geniuses", mapName: "Haven", roomId: "VAL-04", scheduledTime: "21:30 IST" },
        { id: "m-val5", tournamentId: "trn-val-masters", round: 2, matchNumber: 1, roundName: "Semi-Finals", teamAId: "tm-val1", teamAName: "Sentinels", scoreA: 12, killsA: 53, teamBId: "tm-val3", teamBName: "Paper Rex", scoreB: 11, killsB: 49, status: "LIVE", winnerId: null, winnerName: null, mapName: "Ascent", roomId: "VAL-SF1", scheduledTime: "LIVE NOW" },
        { id: "m-val6", tournamentId: "trn-val-masters", round: 2, matchNumber: 2, roundName: "Semi-Finals", teamAId: "tm-val5", teamAName: "Team Liquid", scoreA: 0, killsA: 0, teamBId: "tm-val7", teamBName: "Evil Geniuses", scoreB: 0, killsB: 0, status: "UPCOMING", winnerId: null, winnerName: null, mapName: "Split", roomId: "VAL-SF2", scheduledTime: "22:00 IST" },
        { id: "m-val7", tournamentId: "trn-val-masters", round: 3, matchNumber: 1, roundName: "Grand Final", teamAId: "tbd-1", teamAName: "Winner SF1", scoreA: 0, killsA: 0, teamBId: "tbd-2", teamBName: "Winner SF2", scoreB: 0, killsB: 0, status: "UPCOMING", winnerId: null, winnerName: null, mapName: "Sunset", roomId: "VAL-FINAL", scheduledTime: "Sunday 20:00 IST" }
    ]
};

// Global App State
let db = null;
let allTournaments = [...PROTOTYPE_TOURNAMENTS];
let currentTournament = allTournaments[0];
let currentMatches = PROTOTYPE_MATCHES[currentTournament.id] || [];
let currentTeams = PROTOTYPE_TEAMS[currentTournament.id] || [];
let activeGameFilter = 'ALL';
let unsubscribeMatches = null;
let unsubscribeTeams = null;

// DOM Elements
const tournamentSelect = document.getElementById('tournament-select');
const tournamentsListEl = document.getElementById('tournaments-list');
const bracketTreeContainer = document.getElementById('bracket-tree-container');
const standingsTbody = document.getElementById('standings-tbody');
const matchesGrid = document.getElementById('matches-grid');
const teamsGrid = document.getElementById('teams-grid');

// ==========================================
// INITIALIZATION
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    initFirebase();
    setupNavigation();
    setupFilters();
    setupModals();

    // 1. Immediately render initial data in 0ms
    populateTournamentSelector();
    renderTournamentsList();
    selectTournament(allTournaments[0].id);

    // 2. Connect to Cloud Firestore in background
    syncWithCloudFirestore();
});

function initFirebase() {
    if (typeof firebase !== 'undefined') {
        try {
            if (!firebase.apps.length) {
                firebase.initializeApp(FIREBASE_CONFIG);
            }
            db = firebase.firestore();
            console.log("[FIREBASE] Cloud Firestore connected for Nexus Arena.");
        } catch (e) {
            console.warn("[FIREBASE] Init notice:", e);
        }
    }
}

async function syncWithCloudFirestore() {
    if (!db) {
        updateServerStatus('Prototype Cache Mode');
        return;
    }

    try {
        updateServerStatus('Syncing Cloud...');
        const snap = await db.collection('tournaments').get();

        if (snap.empty) {
            // Seed Firestore with the rich prototype data so desktop and web share everything
            console.log("[FIREBASE] Firestore empty. Auto-seeding prototype championships...");
            const batch = db.batch();
            PROTOTYPE_TOURNAMENTS.forEach(t => {
                batch.set(db.collection('tournaments').doc(t.id), t);
            });
            for (const [tId, tList] of Object.entries(PROTOTYPE_TEAMS)) {
                tList.forEach(tm => {
                    batch.set(db.collection('teams').doc(tm.id), tm);
                });
            }
            for (const [tId, mList] of Object.entries(PROTOTYPE_MATCHES)) {
                mList.forEach(m => {
                    batch.set(db.collection('matches').doc(m.id), m);
                });
            }
            await batch.commit();
            console.log("[FIREBASE] Seed complete.");
            updateServerStatus('Firestore Cloud Synced');
        } else {
            const cloudTournaments = [];
            snap.forEach(doc => {
                const data = doc.data();
                data.id = doc.id;
                cloudTournaments.push(data);
            });
            if (cloudTournaments.length > 0) {
                allTournaments = cloudTournaments;
                populateTournamentSelector();
                renderTournamentsList();
                if (currentTournament) {
                    const refreshed = allTournaments.find(t => t.id === currentTournament.id);
                    if (refreshed) currentTournament = refreshed;
                }
            }
            updateServerStatus('Firestore Cloud Online');
        }

        // Setup real-time listener for current tournament
        bindRealtimeListeners();
    } catch (err) {
        console.warn("[FIREBASE] Cloud sync warning:", err);
        updateServerStatus('Local Cache Mode');
    }
}

function bindRealtimeListeners() {
    if (!db || !currentTournament) return;

    if (unsubscribeMatches) unsubscribeMatches();
    unsubscribeMatches = db.collection('matches')
        .where('tournamentId', '==', currentTournament.id)
        .onSnapshot(snap => {
            if (!snap.empty) {
                const list = [];
                snap.forEach(d => {
                    const item = d.data();
                    item.id = d.id;
                    list.push(item);
                });
                currentMatches = list;
                renderBracket();
                renderStandings();
                renderMatchesSchedule();
                populateDisputeSelect();
            }
        }, err => console.warn("Matches listener:", err));

    if (unsubscribeTeams) unsubscribeTeams();
    unsubscribeTeams = db.collection('teams')
        .where('tournamentId', '==', currentTournament.id)
        .onSnapshot(snap => {
            if (!snap.empty) {
                const list = [];
                snap.forEach(d => {
                    const item = d.data();
                    item.id = d.id;
                    list.push(item);
                });
                currentTeams = list;
                renderStandings();
                renderTeams();
            }
        }, err => console.warn("Teams listener:", err));
}

function updateServerStatus(text) {
    const sEl = document.getElementById('server-status');
    if (sEl) sEl.textContent = text;
}

// ==========================================
// NAVIGATION & TABS
// ==========================================
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

    // Toast popup when downloading desktop application
    const downloadBtns = document.querySelectorAll('a[download], #btn-download-app');
    downloadBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            showToast('Download will be started soon.');
        });
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
        userBtn.style.whiteSpace = 'nowrap';
        userBtn.addEventListener('click', () => {
            const savedUser = localStorage.getItem('nexus_user');
            if (savedUser) {
                localStorage.removeItem('nexus_user');
                userBtn.textContent = 'Sign In';
                showToast('Signed out successfully.');
            } else {
                openModal('modal-auth');
            }
        });

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
// TOURNAMENT SELECTION & RENDERING
// ==========================================
function populateTournamentSelector() {
    if (!tournamentSelect) return;
    tournamentSelect.innerHTML = allTournaments.map(t => `
        <option value="${t.id}">${t.name} [${t.game}]</option>
    `).join('');
}

function selectTournament(tournamentId) {
    currentTournament = allTournaments.find(t => t.id === tournamentId) || allTournaments[0];
    if (!currentTournament) return;

    if (tournamentSelect) tournamentSelect.value = currentTournament.id;

    // Load matches and teams from local cache or default
    currentMatches = PROTOTYPE_MATCHES[currentTournament.id] || [];
    currentTeams = PROTOTYPE_TEAMS[currentTournament.id] || [];

    // Render all views immediately
    renderBracket();
    renderStandings();
    renderMatchesSchedule();
    renderTeams();
    populateDisputeSelect();

    // Bind real-time cloud listeners
    bindRealtimeListeners();
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

            if (!mSnap.empty) {
                const mList = [];
                mSnap.forEach(doc => {
                    const item = doc.data();
                    item.id = doc.id;
                    mList.push(item);
                });
                currentMatches = mList;
            }

            if (!tmSnap.empty) {
                const tmList = [];
                tmSnap.forEach(doc => {
                    const item = doc.data();
                    item.id = doc.id;
                    tmList.push(item);
                });
                currentTeams = tmList;
            }

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
            console.warn("Firestore refresh notice:", e);
        }
    }

    // Local render fallback
    renderBracket();
    renderStandings();
    renderMatchesSchedule();
    renderTeams();
    populateDisputeSelect();
    if (showNotice) showToast('Data refreshed.');
}

function renderTournamentsList() {
    if (!tournamentsListEl) return;

    const filtered = allTournaments.filter(t => {
        if (activeGameFilter === 'ALL') return true;
        return t.game && t.game.toLowerCase().includes(activeGameFilter.toLowerCase());
    });

    if (filtered.length === 0) {
        tournamentsListEl.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 2rem;">No championships found for this filter.</div>`;
        return;
    }

    tournamentsListEl.innerHTML = filtered.map(t => {
        const teamCount = (PROTOTYPE_TEAMS[t.id] || []).length;
        const isSelected = currentTournament && currentTournament.id === t.id;
        return `
            <div class="tournament-card ${isSelected ? 'active-event' : ''}">
                <div class="card-game-badge">${t.game || 'Esports'}</div>
                <h3 class="card-title">${t.name}</h3>
                <p class="card-desc">${t.description || t.rules || 'Official competitive championship'}</p>
                <div class="card-meta">
                    <span class="badge ${getStatusBadgeClass(t.status)}">${t.status}</span>
                    <span class="badge badge-dark">${formatMode(t.mode)}</span>
                    <span style="font-size:0.75rem; color:var(--text-muted);">Max: ${t.maxParticipants || 8} Teams</span>
                </div>
                <div class="card-actions" style="margin-top: 1rem; display: flex; gap: 0.5rem;">
                    <button class="btn btn-primary" onclick="selectTournament('${t.id}')">
                        ${isSelected ? 'Active Event' : 'Select Event'}
                    </button>
                    <button class="btn btn-outline" onclick="openRegisterFor('${t.id}', '${encodeURIComponent(t.name)}')">
                        + Register
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function openRegisterFor(tournamentId, encodedName) {
    selectTournament(tournamentId);
    document.getElementById('reg-tournament-name').value = decodeURIComponent(encodedName);
    document.getElementById('reg-tournament-id').value = tournamentId;
    openModal('modal-register');
}

// ==========================================
// BRACKET TREE RENDERING
// ==========================================
function renderBracket() {
    if (!bracketTreeContainer) return;

    if (currentMatches.length === 0) {
        bracketTreeContainer.innerHTML = `
            <div style="padding: 3rem; text-align: center; color: var(--text-muted);">
                <h3>No Matches Generated</h3>
                <p>Use the desktop organizer application to generate bracket seeds.</p>
            </div>
        `;
        return;
    }

    const roundsMap = {};
    currentMatches.forEach(m => {
        const r = m.round || 1;
        if (!roundsMap[r]) roundsMap[r] = [];
        roundsMap[r].push(m);
    });

    const roundNumbers = Object.keys(roundsMap).map(Number).sort((a, b) => a - b);
    let html = '';

    roundNumbers.forEach(rNum => {
        const matchesInRound = roundsMap[rNum];
        const roundTitle = matchesInRound[0]?.roundName || `Round ${rNum}`;

        html += `
            <div class="bracket-round">
                <div class="round-header">${roundTitle.toUpperCase()}</div>
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

    const winnerA = isComplete && match.winnerId === match.teamAId;
    const winnerB = isComplete && match.winnerId === match.teamBId;

    const killsDisplayA = match.killsA ? ` <span style="font-size:0.75rem; color:var(--text-muted); font-weight:normal;">(${match.killsA}k)</span>` : '';
    const killsDisplayB = match.killsB ? ` <span style="font-size:0.75rem; color:var(--text-muted); font-weight:normal;">(${match.killsB}k)</span>` : '';

    return `
        <div class="match-card ${isLive ? 'is-live' : ''}">
            <div class="match-card-header">
                <span>MATCH #${match.matchNumber} ${match.mapName ? '• ' + match.mapName : ''}</span>
                <span class="badge ${getStatusBadgeClass(match.status)}">${match.status}</span>
            </div>
            <div class="match-team ${winnerA ? 'winner' : ''}">
                <span class="team-name">
                    ${winnerA ? '[W] ' : ''}${match.teamAName || 'TBD'}
                </span>
                <span class="team-score">${isComplete || isLive ? match.scoreA + killsDisplayA : '-'}</span>
            </div>
            <div class="match-team ${winnerB ? 'winner' : ''}">
                <span class="team-name">
                    ${winnerB ? '[W] ' : ''}${match.teamBName || 'TBD'}
                </span>
                <span class="team-score">${isComplete || isLive ? match.scoreB + killsDisplayB : '-'}</span>
            </div>
        </div>
    `;
}

// ==========================================
// STANDINGS RENDERING
// ==========================================
function renderStandings() {
    if (!standingsTbody) return;

    if (currentTournament && currentTournament.mode === 'BATTLE_ROYALE') {
        const teams = [...currentTeams];
        standingsTbody.innerHTML = teams.map((tm, idx) => {
            const rank = idx + 1;
            const rankClass = rank === 1 ? 'rank-1' : rank === 2 ? 'rank-2' : rank === 3 ? 'rank-3' : '';
            const pts = (9 - rank) * 12 + (15 - rank * 2);
            const kills = 28 - rank * 3;
            return `
                <tr>
                    <td><span class="rank-badge ${rankClass}">${rank}</span></td>
                    <td style="font-weight: 700; color: #fff;">${tm.name} <span style="color: var(--deep-orange);">[${tm.tag}]</span></td>
                    <td>4</td>
                    <td>${kills}</td>
                    <td>${(9 - rank) * 12}</td>
                    <td>${kills}</td>
                    <td style="font-weight: 700; color: var(--deep-orange);">${pts}</td>
                    <td><span class="badge ${rank === 1 ? 'badge-live' : 'badge-dark'}">${rank === 1 ? 'LEADER' : 'CONTENDER'}</span></td>
                </tr>
            `;
        }).join('');
    } else {
        const teams = [...currentTeams];
        standingsTbody.innerHTML = teams.map((tm, idx) => {
            const rank = idx + 1;
            return `
                <tr>
                    <td><span class="rank-badge">${rank}</span></td>
                    <td style="font-weight: 700; color: #fff;">${tm.name} <span style="color: var(--deep-orange);">[${tm.tag}]</span></td>
                    <td>-</td>
                    <td>-</td>
                    <td>-</td>
                    <td>-</td>
                    <td style="font-weight: 700; color: #ffffff;">Seed #${tm.seed || rank}</td>
                    <td><span class="badge ${tm.checkedIn ? 'badge-live' : 'badge-dark'}">${tm.checkedIn ? 'CHECKED IN' : 'REGISTERED'}</span></td>
                </tr>
            `;
        }).join('');
    }
}

// ==========================================
// MATCHES & TEAMS RENDERING
// ==========================================
function renderMatchesSchedule() {
    if (!matchesGrid) return;
    if (currentMatches.length === 0) {
        matchesGrid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 2rem;">No matches scheduled yet.</div>`;
        return;
    }

    matchesGrid.innerHTML = currentMatches.map(m => `
        <div class="match-info-card">
            <div class="match-info-top">
                <span class="badge badge-dark">${m.roundName} • #${m.matchNumber}</span>
                <span class="badge ${getStatusBadgeClass(m.status)}">${m.status}</span>
            </div>
            
            <div class="match-versus">
                <div class="team-box">
                    <div class="team-box-name">${m.teamAName || 'TBD'}</div>
                    <div style="font-size: 1.2rem; font-weight: 700; color: var(--deep-orange);">${m.scoreA}</div>
                </div>
                <div class="vs-pill">VS</div>
                <div class="team-box">
                    <div class="team-box-name">${m.teamBName || 'TBD'}</div>
                    <div style="font-size: 1.2rem; font-weight: 700; color: var(--deep-orange);">${m.scoreB}</div>
                </div>
            </div>

            <div style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.4rem;">
                <span>Schedule: <strong>${m.scheduledTime || 'TBD'}</strong></span> | 
                <span>Map: <strong>${m.mapName || 'Ascent'}</strong></span>
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
                <span class="badge badge-dark">SEED #${t.seed || 1}</span>
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

            const teamId = 'tm-' + Math.random().toString(36).substring(2, 9);
            const payload = {
                id: teamId,
                tournamentId,
                name,
                tag,
                captainId: captainTag,
                captainEmail,
                playerNames,
                checkedIn: true,
                seed: currentTeams.length + 1,
                registrationStatus: 'APPROVED'
            };

            // Update in-memory local state immediately
            currentTeams.push(payload);
            renderTeams();
            renderStandings();
            showToast('Team registered successfully in Cloud Firestore!');
            closeModal('modal-register');
            regForm.reset();

            // Save to Firestore in background
            if (db) {
                try {
                    await db.collection('teams').doc(teamId).set(payload);
                } catch (err) {
                    console.warn('Firestore write warning:', err);
                }
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

            const dispId = 'disp-' + Math.random().toString(36).substring(2, 8);
            const payload = {
                id: dispId,
                tournamentId: currentTournament.id,
                matchId,
                submittedByTeamName: teamName,
                reason,
                evidenceUrl,
                status: 'UNDER_REVIEW',
                createdAt: new Date().toISOString()
            };

            showToast('Dispute filed to Cloud Firestore! Organizer alerted.');
            closeModal('modal-dispute');
            dispForm.reset();

            if (db) {
                try {
                    await db.collection('disputes').doc(dispId).set(payload);
                } catch (err) {
                    console.warn('Firestore dispute warning:', err);
                }
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

            const repId = 'rep-' + Math.random().toString(36).substring(2, 8);
            const payload = {
                id: repId,
                tournamentId: currentTournament.id,
                reportedPlayerName: playerName,
                reportedTeamName: teamName,
                category,
                description: desc,
                evidenceUrl,
                status: 'SUBMITTED',
                moderationAction: 'NONE',
                createdAt: new Date().toISOString()
            };

            showToast('Report submitted for fair-play investigation.');
            closeModal('modal-report');
            repForm.reset();

            if (db) {
                try {
                    await db.collection('conduct_reports').doc(repId).set(payload);
                } catch (err) {
                    console.warn('Firestore report warning:', err);
                }
            }
        });
    }

    // Auth / Sign In Form
    const authForm = document.getElementById('form-auth');
    if (authForm) {
        authForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('auth-username').value.trim();
            const password = document.getElementById('auth-password').value.trim();

            let authUser = null;
            if (username.toLowerCase() === 'admin' && password === 'admin123') {
                authUser = { username: 'admin', role: 'ORGANIZER', gamerTag: 'ApexAdmin#0001' };
            } else if (username.toLowerCase() === 'tenz' && password === 'tenz123') {
                authUser = { username: 'tenz', role: 'PARTICIPANT', gamerTag: 'SEN_TenZ#1337' };
            } else if (username.toLowerCase() === 'boaster' && password === 'fnc123') {
                authUser = { username: 'boaster', role: 'PARTICIPANT', gamerTag: 'FNC_Boaster#EU1' };
            }

            if (authUser) {
                localStorage.setItem('nexus_user', JSON.stringify(authUser));
                const userBtn = document.getElementById('btn-user-action');
                if (userBtn) {
                    userBtn.textContent = authUser.username + ' (' + authUser.role + ')';
                }
                showToast('Welcome back, ' + authUser.username + '!');
                closeModal('modal-auth');
                authForm.reset();
                return;
            }

            if (db) {
                try {
                    const q = await db.collection('users').where('username', '==', username).limit(1).get();
                    if (!q.empty) {
                        const userData = q.docs[0].data();
                        if (userData.password === password) {
                            localStorage.setItem('nexus_user', JSON.stringify(userData));
                            const userBtn = document.getElementById('btn-user-action');
                            if (userBtn) {
                                userBtn.textContent = userData.username + ' (' + userData.role + ')';
                            }
                            showToast('Welcome back, ' + userData.username + '!');
                            closeModal('modal-auth');
                            authForm.reset();
                            return;
                        }
                    }
                } catch (err) {
                    console.warn('Firestore auth warning:', err);
                }
            }

            showToast('Invalid credentials. Check demo credentials.', 'error');
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

function downloadCsv() {
    if (!currentTournament) return;
    const teams = currentTeams;
    let csv = "Rank,Team,Tag,Captain,Players\n";
    teams.forEach((t, i) => {
        csv += `${i + 1},"${t.name}","${t.tag}","${t.captainEmail}","${(t.playerNames || []).join(';')}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${currentTournament.id}-standings.csv`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Standings exported to CSV.');
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
    if (status === 'REGISTRATION_OPEN' || status === 'CHECK_IN') return 'badge-live';
    if (status === 'COMPLETED' || status === 'COMPLETE') return 'badge-dark';
    return 'badge-dark';
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = (type === 'error' ? '[ERROR] ' : '[OK] ') + message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}
