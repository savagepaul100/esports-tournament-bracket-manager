package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.Match;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MatchController {

    private final TournamentService tournamentService;

    @Autowired
    public MatchController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/tournaments/{tournamentId}/matches")
    public ApiResponse<List<Match>> getMatches(@PathVariable String tournamentId) {
        return ApiResponse.ok(tournamentService.getMatchesByTournament(tournamentId));
    }

    @PostMapping("/api/tournaments/{tournamentId}/bracket/generate")
    public ApiResponse<List<Match>> generateBracket(@PathVariable String tournamentId,
                                                    @RequestParam(value = "actor", defaultValue = "Organizer") String actor) {
        try {
            List<Match> matches = tournamentService.generateSingleEliminationBracket(tournamentId, actor);
            return ApiResponse.ok("Bracket generated successfully with " + matches.size() + " matches", matches);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/matches/{matchId}/schedule")
    public ApiResponse<Match> updateSchedule(@PathVariable String matchId,
                                             @RequestBody ScheduleRequest req) {
        String actor = req.actor != null ? req.actor : "Organizer";
        try {
            Match updated = tournamentService.updateMatchSchedule(matchId, req.scheduledTime, req.roomId, req.roomPassword, req.mapName, actor);
            return ApiResponse.ok("Match schedule updated", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/matches/{matchId}/score")
    public ApiResponse<Match> recordScore(@PathVariable String matchId,
                                          @RequestBody ScoreRequest req) {
        String actor = req.actor != null ? req.actor : "Organizer";
        try {
            Match updated = tournamentService.recordMatchResult(matchId, req.scoreA, req.scoreB, req.reason, actor);
            return ApiResponse.ok("Score confirmed and winner advanced", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/matches/{matchId}/br-placements")
    public ApiResponse<Match> recordBrPlacements(@PathVariable String matchId,
                                                 @RequestBody BrScoreRequest req) {
        String actor = req.actor != null ? req.actor : "Organizer";
        try {
            Match updated = tournamentService.recordBattleRoyalePlacements(matchId, req.placements, actor);
            return ApiResponse.ok("Battle Royale round results recorded", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
