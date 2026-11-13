package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class DashboardView extends VBox {
    private final ApiClient apiClient;
    private final Runnable onNavigateToTournaments;

    public DashboardView(ApiClient apiClient, Runnable onNavigateToTournaments) {
        this.apiClient = apiClient;
        this.onNavigateToTournaments = onNavigateToTournaments;

        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        initView();
    }

    private void initView() {
        getChildren().clear();

        // Header
        VBox headerBox = new VBox(6);
        Label title = new Label("ORGANIZER COMMAND CENTER");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Welcome back, " + apiClient.getCurrentUser().getGamerTag() + " • Real-Time Tournament Operations");
        subtitle.getStyleClass().add("view-subtitle");

        // Guidance Callout Card
        HBox guidanceBox = new HBox(10);
        guidanceBox.setAlignment(Pos.CENTER_LEFT);
        guidanceBox.setStyle("-fx-background-color: #18181b; -fx-border-color: #ea580c; -fx-border-width: 1 1 1 3; " +
                "-fx-border-radius: 2; -fx-background-radius: 2; -fx-padding: 8 14;");
        Label guideIcon = new Label("💡");
        guideIcon.setStyle("-fx-font-size: 15px;");
        VBox guideText = new VBox(2);
        Label guideTitle = new Label("STANDINGS & OPERATIONS OVERVIEW");
        guideTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");
        Label guideDesc = new Label("Track real-time matches, active rosters, and fair-play disputes synchronized 100% with Cloud Firestore.");
        guideDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        guideText.getChildren().addAll(guideTitle, guideDesc);
        guidanceBox.getChildren().addAll(guideIcon, guideText);

        headerBox.getChildren().addAll(title, subtitle, guidanceBox);

        // Fetch data from high-speed cache
        List<TournamentDto> tournaments = apiClient.getTournaments();
        int totalTournaments = tournaments.size();
        int activeMatches = 0;
        int pendingDisputes = 0;
        int pendingReports = 0;
        int totalTeams = 0;

        for (TournamentDto t : tournaments) {
            totalTeams += apiClient.getTeams(t.getId()).size();
            List<MatchDto> matches = apiClient.getMatches(t.getId());
            activeMatches += matches.stream().filter(m -> "LIVE".equals(m.getStatus()) || "UPCOMING".equals(m.getStatus())).count();
            pendingDisputes += apiClient.getDisputes(t.getId()).stream().filter(d -> "SUBMITTED".equals(d.getStatus()) || "UNDER_REVIEW".equals(d.getStatus())).count();
            pendingReports += apiClient.getConductReports(t.getId()).stream().filter(r -> "SUBMITTED".equals(r.getStatus())).count();
        }

        // Stats Row (Red, Deep Orange, Gray, White Palette)
        HBox statsGrid = new HBox(14);
        statsGrid.getChildren().addAll(
                createStatCard("TOTAL TOURNAMENTS", String.valueOf(totalTournaments), "#ffffff"),
                createStatCard("ACTIVE MATCHES", String.valueOf(activeMatches), "#dc2626"),
                createStatCard("REGISTERED TEAMS", String.valueOf(totalTeams), "#ea580c"),
                createStatCard("PENDING DISPUTES", String.valueOf(pendingDisputes), "#f97316"),
                createStatCard("CONDUCT REPORTS", String.valueOf(pendingReports), "#ef4444")
        );

        // Quick Actions & Live Link
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.getStyleClass().add("card-panel");

        Label actionLabel = new Label("QUICK OPS: ");
        actionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Button btnNewTournament = new Button("+ New Tournament");
        btnNewTournament.getStyleClass().add("btn-primary");
        btnNewTournament.setOnAction(e -> onNavigateToTournaments.run());

        Button btnOpenPortal = new Button("🌐 Live Cloud Portal (nexusarena-esports.web.app)");
        btnOpenPortal.getStyleClass().add("btn-cyan");
        btnOpenPortal.setOnAction(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://nexusarena-esports.web.app"));
                }
            } catch (Exception ex) {
                CyberAlert.showInfo("Cloud Portal", "Visit: https://nexusarena-esports.web.app");
            }
        });

        Button btnSeed = new Button("⚡ Seed Prototype Data");
        btnSeed.getStyleClass().add("btn-success");
        btnSeed.setOnAction(e -> {
            boolean confirm = CyberAlert.showConfirm("Seed Prototype Data",
                    "Populate Cloud Firestore with live championship matches, ongoing game, and rosters?",
                    "This initializes real-time tournament operations across desktop and web portal.");
            if (confirm) {
                if (apiClient.seedPrototypeData()) {
                    CyberAlert.showInfo("Seed Complete", "Cloud Firestore populated with live esports tournament data!");
                    initView();
                } else {
                    CyberAlert.showError("Seed Error", "Failed to seed data. Check network/Firebase connection.");
                }
            }
        });

        Button btnRefresh = new Button("↻ Sync Data");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> initView());

        actionsBox.getChildren().addAll(actionLabel, btnNewTournament, btnOpenPortal, btnSeed, btnRefresh);

        // Recent Activity Feed
        VBox activityCard = new VBox(12);
        activityCard.getStyleClass().add("card-panel");
        Label activityTitle = new Label("RECENT AUDIT & ACTIVITY FEED");
        activityTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ea580c;");

        VBox logsContainer = new VBox(8);
        List<AuditLogDto> auditLogs = apiClient.getAuditLogs();
        int maxLogs = Math.min(6, auditLogs.size());
        if (maxLogs == 0) {
            logsContainer.getChildren().add(new Label("No recent activities recorded."));
        } else {
            for (int i = 0; i < maxLogs; i++) {
                AuditLogDto log = auditLogs.get(i);
                HBox logRow = new HBox(10);
                logRow.setAlignment(Pos.CENTER_LEFT);
                logRow.setStyle("-fx-padding: 6 8; -fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 4;");

                Label timeLabel = new Label(log.getTimestamp() != null && log.getTimestamp().length() >= 19 ? log.getTimestamp().substring(11, 19) : "NOW");
                timeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");

                Label actionBadge = new Label(log.getAction());
                actionBadge.getStyleClass().addAll("badge-pill", "badge-purple-fx");

                Label actorLabel = new Label("by " + log.getActor() + ":");
                actorLabel.setStyle("-fx-text-fill: #94a3b8;");

                Label reasonLabel = new Label(log.getReason() != null ? log.getReason() : "");
                reasonLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: 500;");

                logRow.getChildren().addAll(timeLabel, actionBadge, actorLabel, reasonLabel);
                logsContainer.getChildren().add(logRow);
            }
        }
        activityCard.getChildren().addAll(activityTitle, logsContainer);

        getChildren().addAll(headerBox, statsGrid, actionsBox, activityCard);
    }

    private VBox createStatCard(String title, String value, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");
        lblValue.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }
}
