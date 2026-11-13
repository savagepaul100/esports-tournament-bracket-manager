package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.AuditLog;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AuditController {

    private final TournamentService tournamentService;

    @Autowired
    public AuditController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/audit-logs")
    public ApiResponse<List<AuditLog>> getAuditLogs() {
        return ApiResponse.ok(tournamentService.getAllAuditLogs());
    }
}
