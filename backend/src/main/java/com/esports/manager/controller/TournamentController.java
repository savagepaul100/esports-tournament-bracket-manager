package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.Tournament;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
public class TournamentController {

    private final TournamentService tournamentService;

    @Autowired
    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ApiResponse<List<Tournament>> getTournaments(@RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
        if (publishedOnly) {
            return ApiResponse.ok(tournamentService.getPublishedTournaments());
        }
        return ApiResponse.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tournament>> getTournamentById(@PathVariable String id) {
        return tournamentService.getTournamentById(id)
                .map(t -> ResponseEntity.ok(ApiResponse.ok(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ApiResponse<Tournament> createTournament(@RequestBody Tournament tournament,
                                                    @RequestParam(value = "actor", defaultValue = "Organizer") String actor) {
        return ApiResponse.ok("Tournament created successfully", tournamentService.createTournament(tournament, actor));
    }

    @PutMapping("/{id}")
    public ApiResponse<Tournament> updateTournament(@PathVariable String id,
                                                    @RequestBody Tournament tournament,
                                                    @RequestParam(value = "actor", defaultValue = "Organizer") String actor) {
        return ApiResponse.ok("Tournament updated", tournamentService.updateTournament(id, tournament, actor));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Tournament> updateStatus(@PathVariable String id,
                                                @RequestBody StatusUpdateRequest req) {
        String actor = req.actor != null ? req.actor : "Organizer";
        return ApiResponse.ok("Status updated", tournamentService.updateStatus(id, req.status, actor));
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<String> setPublished(@PathVariable String id,
                                            @RequestParam boolean published,
                                            @RequestParam(value = "actor", defaultValue = "Organizer") String actor) {
        tournamentService.setPublished(id, published, actor);
        return ApiResponse.ok("Publication status updated to " + published, "OK");
    }

    @GetMapping("/{id}/export/csv")
    public ResponseEntity<String> exportCsv(@PathVariable String id) {
        String csv = tournamentService.exportResultsToCsv(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tournament-" + id + "-results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
