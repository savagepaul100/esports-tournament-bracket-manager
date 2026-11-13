package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.Notification;
import com.esports.manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class NotificationController {

    private final TournamentService tournamentService;

    @Autowired
    public NotificationController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/notifications")
    public ApiResponse<List<Notification>> getNotifications(@RequestParam String userId) {
        return ApiResponse.ok(tournamentService.getNotificationsForUser(userId));
    }

    @PutMapping("/api/notifications/{id}/read")
    public ApiResponse<String> markRead(@PathVariable String id) {
        tournamentService.markNotificationRead(id);
        return ApiResponse.ok("Notification marked as read", "OK");
    }
}
