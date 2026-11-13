package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.RegistrationStatus;
import com.esports.manager.model.Team;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TeamController {

    private final TournamentService tournamentService;

    @Autowired
    public TeamController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/tournaments/{tournamentId}/teams")
    public ApiResponse<List<Team>> getTeams(@PathVariable String tournamentId) {
        return ApiResponse.ok(tournamentService.getTeamsByTournament(tournamentId));
    }

    @PostMapping("/api/tournaments/{tournamentId}/teams")
    public ApiResponse<Team> registerTeam(@PathVariable String tournamentId,
                                          @RequestBody Team team,
                                          @RequestParam(value = "actor", defaultValue = "Participant") String actor) {
        team.setTournamentId(tournamentId);
        try {
            Team registered = tournamentService.registerTeam(team, actor);
            return ApiResponse.ok("Team registration submitted", registered);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/teams/{teamId}/status")
    public ApiResponse<Team> updateRegistrationStatus(@PathVariable String teamId,
                                                      @RequestBody RegistrationDecisionRequest req) {
        String actor = req.actor != null ? req.actor : "Organizer";
        try {
            Team updated = tournamentService.updateRegistrationStatus(teamId, req.status, req.reason, actor);
            return ApiResponse.ok("Registration updated to " + req.status, updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/teams/{teamId}/checkin")
    public ApiResponse<Team> setCheckIn(@PathVariable String teamId,
                                        @RequestParam boolean checkedIn,
                                        @RequestParam(value = "actor", defaultValue = "Organizer") String actor) {
        try {
            Team updated = tournamentService.setCheckIn(teamId, checkedIn, actor);
            return ApiResponse.ok("Check-in status updated", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
