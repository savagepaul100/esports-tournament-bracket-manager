package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.ConductReport;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ConductController {

    private final TournamentService tournamentService;

    @Autowired
    public ConductController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/tournaments/{tournamentId}/conduct-reports")
    public ApiResponse<List<ConductReport>> getReports(@PathVariable String tournamentId) {
        return ApiResponse.ok(tournamentService.getConductReportsByTournament(tournamentId));
    }

    @PostMapping("/api/tournaments/{tournamentId}/conduct-reports")
    public ApiResponse<ConductReport> submitReport(@PathVariable String tournamentId,
                                                   @RequestBody ConductReport report,
                                                   @RequestParam(value = "actor", defaultValue = "Participant") String actor) {
        report.setTournamentId(tournamentId);
        try {
            ConductReport saved = tournamentService.submitConductReport(report, actor);
            return ApiResponse.ok("Conduct report submitted", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/conduct-reports/{reportId}/resolve")
    public ApiResponse<ConductReport> resolveReport(@PathVariable String reportId,
                                                    @RequestBody ConductResolveRequest req) {
        try {
            ConductReport resolved = tournamentService.resolveConductReport(reportId, req.action, req.reason, req.reviewer);
            return ApiResponse.ok("Moderation action applied", resolved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
