package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.Dispute;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class DisputeController {

    private final TournamentService tournamentService;

    @Autowired
    public DisputeController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/tournaments/{tournamentId}/disputes")
    public ApiResponse<List<Dispute>> getDisputes(@PathVariable String tournamentId) {
        return ApiResponse.ok(tournamentService.getDisputesByTournament(tournamentId));
    }

    @PostMapping("/api/tournaments/{tournamentId}/disputes")
    public ApiResponse<Dispute> submitDispute(@PathVariable String tournamentId,
                                              @RequestBody Dispute dispute,
                                              @RequestParam(value = "actor", defaultValue = "Participant") String actor) {
        dispute.setTournamentId(tournamentId);
        try {
            Dispute saved = tournamentService.submitDispute(dispute, actor);
            return ApiResponse.ok("Dispute submitted for organizer review", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/disputes/{disputeId}/resolve")
    public ApiResponse<Dispute> resolveDispute(@PathVariable String disputeId,
                                               @RequestBody DisputeResolveRequest req) {
        try {
            Dispute resolved = tournamentService.resolveDispute(disputeId, req.decision, req.notes, req.reviewer);
            return ApiResponse.ok("Dispute resolved", resolved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
