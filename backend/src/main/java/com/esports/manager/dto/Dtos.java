package com.esports.manager.dto;

import com.esports.manager.model.*;
import java.util.List;

public class Dtos {

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public Role role;
        public String gamerTag;
    }

    public static class ScoreRequest {
        public int scoreA;
        public int scoreB;
        public String reason;
        public String actor;
    }

    public static class BrScoreRequest {
        public List<BrTeamPlacement> placements;
        public String actor;
    }

    public static class ScheduleRequest {
        public String scheduledTime;
        public String roomId;
        public String roomPassword;
        public String mapName;
        public String actor;
    }

    public static class StatusUpdateRequest {
        public TournamentStatus status;
        public String actor;
    }

    public static class RegistrationDecisionRequest {
        public RegistrationStatus status;
        public String reason;
        public String actor;
    }

    public static class DisputeResolveRequest {
        public String decision;
        public String notes;
        public String reviewer;
    }

    public static class ConductResolveRequest {
        public ModerationAction action;
        public String reason;
        public String reviewer;
    }

    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;

        public ApiResponse() {}

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, "Success", data);
        }

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
