package com.esports.desktop.client;

import com.esports.desktop.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class ApiClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private UserDto currentUser;

    public ApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        // Default login as Organizer
        this.currentUser = new UserDto();
        this.currentUser.setId("usr-admin");
        this.currentUser.setUsername("admin");
        this.currentUser.setRole("ORGANIZER");
        this.currentUser.setGamerTag("ApexAdmin#0001");
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }

    public boolean isBackendAlive() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            Map<String, String> body = Map.of("username", username, "password", password);
            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                this.currentUser = mapper.treeToValue(root.path("data"), UserDto.class);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<TournamentDto> getTournaments() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<TournamentDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getTournaments: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public TournamentDto createTournament(TournamentDto tournament) {
        try {
            String json = mapper.writeValueAsString(tournament);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments?actor=" + currentUser.getUsername()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.treeToValue(root.path("data"), TournamentDto.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateTournamentStatus(String tournamentId, String status) {
        try {
            Map<String, String> body = Map.of("status", status, "actor", currentUser.getUsername());
            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/status"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TeamDto> getTeams(String tournamentId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/teams"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<TeamDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getTeams: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean updateTeamStatus(String teamId, String status, String reason) {
        try {
            Map<String, String> body = Map.of(
                    "status", status,
                    "reason", reason != null ? reason : "Organizer decision",
                    "actor", currentUser.getUsername()
            );
            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/teams/" + teamId + "/status"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setTeamCheckIn(String teamId, boolean checkedIn) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/teams/" + teamId + "/checkin?checkedIn=" + checkedIn + "&actor=" + currentUser.getUsername()))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MatchDto> getMatches(String tournamentId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/matches"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<MatchDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getMatches: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<MatchDto> generateBracket(String tournamentId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/bracket/generate?actor=" + currentUser.getUsername()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<MatchDto>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public boolean updateMatchSchedule(String matchId, String time, String roomId, String password, String map) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("scheduledTime", time);
            body.put("roomId", roomId);
            body.put("roomPassword", password);
            body.put("mapName", map);
            body.put("actor", currentUser.getUsername());

            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/matches/" + matchId + "/schedule"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, String reason) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("scoreA", scoreA);
            body.put("scoreB", scoreB);
            body.put("reason", reason != null ? reason : "Match completed");
            body.put("actor", currentUser.getUsername());

            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/matches/" + matchId + "/score"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DisputeDto> getDisputes(String tournamentId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/disputes"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<DisputeDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getDisputes: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean resolveDispute(String disputeId, String decision, String notes) {
        try {
            Map<String, String> body = Map.of(
                    "decision", decision,
                    "notes", notes != null ? notes : "",
                    "reviewer", currentUser.getUsername()
            );
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/disputes/" + disputeId + "/resolve"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ConductReportDto> getConductReports(String tournamentId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tournaments/" + tournamentId + "/conduct-reports"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<ConductReportDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getConductReports: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean resolveConductReport(String reportId, String action, String reason) {
        try {
            Map<String, String> body = Map.of(
                    "action", action,
                    "reason", reason != null ? reason : "",
                    "reviewer", currentUser.getUsername()
            );
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/conduct-reports/" + reportId + "/resolve"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AuditLogDto> getAuditLogs() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/audit-logs"))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            if (root.path("success").asBoolean()) {
                return mapper.convertValue(root.path("data"), new TypeReference<List<AuditLogDto>>() {});
            }
        } catch (Exception e) {
            System.err.println("API error getAuditLogs: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
