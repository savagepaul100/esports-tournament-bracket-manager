package com.esports.manager;

import com.esports.manager.model.*;
import com.esports.manager.repository.LocalJsonDataStore;
import com.esports.manager.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TournamentServiceTest {

    private TournamentService service;
    private LocalJsonDataStore dataStore;

    @BeforeEach
    public void setup(@TempDir Path tempDir) {
        dataStore = new LocalJsonDataStore(tempDir);
        service = new TournamentService(dataStore);
    }

    @Test
    public void testSingleEliminationBracketWithByes() {
        // Create tournament
        Tournament t = new Tournament();
        t.setName("Test Valorant Cup");
        t.setGame("Valorant");
        t.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        Tournament created = service.createTournament(t, "org-1");

        // Open registration
        service.updateStatus(created.getId(), TournamentStatus.REGISTRATION_OPEN, "org-1");

        // Register 5 teams (Power of 2 is 8 -> exactly 3 BYEs!)
        for (int i = 1; i <= 5; i++) {
            Team tm = new Team();
            tm.setTournamentId(created.getId());
            tm.setName("Team " + i);
            tm.setTag("T" + i);
            Team reg = service.registerTeam(tm, "p-" + i);
            service.updateRegistrationStatus(reg.getId(), RegistrationStatus.APPROVED, "Approved for bracket", "org-1");
        }

        // Generate bracket
        List<Match> matches = service.generateSingleEliminationBracket(created.getId(), "org-1");

        assertNotNull(matches);
        // Total matches for 8 bracket slots = 4 (R1) + 2 (R2) + 1 (R3) = 7 matches
        assertEquals(7, matches.size());

        // Check Round 1 byes
        long byeMatches = matches.stream().filter(m -> m.getRound() == 1 && m.isBye()).count();
        assertEquals(3, byeMatches, "Expected 3 byes for 5 teams in an 8-slot bracket");

        // Verify tournament moved to IN_PROGRESS
        Tournament refreshed = service.getTournamentById(created.getId()).orElseThrow();
        assertEquals(TournamentStatus.IN_PROGRESS, refreshed.getStatus());
    }

    @Test
    public void testScoreConfirmationAndWinnerAdvancement() {
        Tournament t = new Tournament();
        t.setName("Championship 4-Team");
        t.setGame("CS2");
        t.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        Tournament created = service.createTournament(t, "org-1");

        for (int i = 1; i <= 4; i++) {
            Team tm = new Team();
            tm.setTournamentId(created.getId());
            tm.setName("Squad " + i);
            Team reg = service.registerTeam(tm, "p-" + i);
            service.updateRegistrationStatus(reg.getId(), RegistrationStatus.APPROVED, "Approved", "org-1");
        }

        List<Match> matches = service.generateSingleEliminationBracket(created.getId(), "org-1");
        // Matches: 2 in round 1 (SF), 1 in round 2 (Final)
        Match sf1 = matches.stream().filter(m -> m.getRound() == 1 && m.getMatchNumber() == 2).findFirst().orElseThrow();
        Match grandFinal = matches.stream().filter(m -> m.getRound() == 2).findFirst().orElseThrow();

        // Record score for SF1: Squad 1 beats Squad 2 (13 to 7)
        Match completedSF1 = service.recordMatchResult(sf1.getId(), 13, 7, "Regular time win", "org-1");
        assertEquals(MatchStatus.COMPLETE, completedSF1.getStatus());
        assertEquals("Squad 1", completedSF1.getWinnerName());

        // Grand final should now have Squad 1 advanced!
        Match updatedFinal = service.getMatchesByTournament(created.getId()).stream()
                .filter(m -> m.getId().equals(grandFinal.getId()))
                .findFirst().orElseThrow();

        assertTrue("Squad 1".equals(updatedFinal.getTeamAName()) || "Squad 1".equals(updatedFinal.getTeamBName()),
                "Winner should have advanced to Grand Final");
    }
}
