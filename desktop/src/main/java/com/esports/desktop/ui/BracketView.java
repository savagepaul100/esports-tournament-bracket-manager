package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.MatchDto;
import com.esports.desktop.model.TournamentDto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class BracketView extends VBox {
    private final ApiClient apiClient;
    private final ComboBox<TournamentDto> cmbTournament;
    private final HBox bracketContainer;
    private final ScrollPane scrollPane;

    public BracketView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("INTERACTIVE BRACKET & TREE ADVANCEMENT");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Generate single-elimination tournament trees with byes, track live scores, and inspect progression.");
        subtitle.getStyleClass().add("view-subtitle");

        HBox guidanceBox = new HBox(10);
        guidanceBox.setAlignment(Pos.CENTER_LEFT);
        guidanceBox.setStyle("-fx-background-color: #18181b; -fx-border-color: #ea580c; -fx-border-width: 1 1 1 3; " +
                "-fx-border-radius: 2; -fx-background-radius: 2; -fx-padding: 8 14; -fx-margin: 0 0 10 0;");
        Label guideIcon = new Label("[INFO]");
        guideIcon.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #ea580c;");
        VBox guideText = new VBox(2);
        Label guideTitle = new Label("INTERACTIVE BRACKET TREE GUIDE");
        guideTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");
        Label guideDesc = new Label("Click any match node to update live rounds and kills in real time. Winners automatically advance to the next round.");
        guideDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        guideText.getChildren().addAll(guideTitle, guideDesc);
        guidanceBox.getChildren().addAll(guideIcon, guideText);

        headerBox.getChildren().addAll(title, subtitle, guidanceBox);

        // Control bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Select Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(320);
        cmbTournament.setOnAction(e -> loadBracket());

        Button btnGenerate = new Button("Generate Bracket");
        btnGenerate.getStyleClass().add("btn-primary");
        btnGenerate.setOnAction(e -> generateBracket());

        Button btnSeed = new Button("Seed Prototype Data");
        btnSeed.getStyleClass().add("btn-success");
        btnSeed.setOnAction(e -> {
            boolean confirm = CyberAlert.showConfirm("Seed Prototype Data",
                    "Populate Cloud Firestore with live tournament bracket and ongoing match?",
                    "This initializes the tournament tree with live scores and progression.");
            if (confirm) {
                if (apiClient.seedPrototypeData()) {
                    CyberAlert.showInfo("Data Seeded", "Bracket populated with live tournament data!");
                    loadTournaments();
                    loadBracket();
                }
            }
        });

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadBracket();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnGenerate, btnSeed, btnRefresh);

        // Bracket Canvas/Container
        bracketContainer = new HBox(28);
        bracketContainer.setStyle("-fx-padding: 24; -fx-background-color: #0d111c; -fx-background-radius: 12;");
        bracketContainer.setAlignment(Pos.CENTER_LEFT);

        scrollPane = new ScrollPane(bracketContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(headerBox, topBar, scrollPane);
        loadTournaments();
    }

    private void loadTournaments() {
        List<TournamentDto> list = apiClient.getTournaments();
        cmbTournament.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty() && cmbTournament.getValue() == null) {
            cmbTournament.setValue(list.get(0));
            loadBracket();
        }
    }

    private void generateBracket() {
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) {
            CyberAlert.showInfo("Interactive Bracket", "Please select a tournament first.");
            return;
        }

        boolean confirmed = CyberAlert.showConfirm("Bracket Generation",
                "Generate single-elimination bracket for " + selected.getName() + "?",
                "This will calculate power-of-two slots, seed bye matches if needed, and initialize round progression.");

        if (confirmed) {
            List<MatchDto> matches = apiClient.generateBracket(selected.getId());
            if (!matches.isEmpty()) {
                CyberAlert.showInfo("Bracket Generated", "Bracket generated with " + matches.size() + " matches! Cloud Firestore updated.");
                loadBracket();
            } else {
                CyberAlert.showError("Generation Failed", "Could not generate bracket. Ensure at least 2 approved teams exist.");
            }
        }
    }

    private void loadBracket() {
        bracketContainer.getChildren().clear();
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) return;

        List<MatchDto> matches = apiClient.getMatches(selected.getId());
        if (matches.isEmpty()) {
            Label empty = new Label("No matches generated yet. Click 'Generate Bracket' above once teams are approved.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 40;");
            bracketContainer.getChildren().add(empty);
            return;
        }

        // Group by round
        Map<Integer, List<MatchDto>> roundMap = new TreeMap<>();
        for (MatchDto m : matches) {
            roundMap.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        }

        for (Map.Entry<Integer, List<MatchDto>> entry : roundMap.entrySet()) {
            int roundNum = entry.getKey();
            List<MatchDto> roundMatches = entry.getValue();

            VBox roundCol = new VBox(20);
            roundCol.setMinWidth(260);
            roundCol.setAlignment(Pos.CENTER);

            String rTitle = roundMatches.get(0).getRoundName() != null ? roundMatches.get(0).getRoundName() : "Round " + roundNum;
            Label colHeader = new Label(rTitle.toUpperCase());
            colHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00f0ff; -fx-padding: 0 0 10 0;");
            roundCol.getChildren().add(colHeader);

            for (MatchDto m : roundMatches) {
                roundCol.getChildren().add(createMatchCard(m));
            }

            bracketContainer.getChildren().add(roundCol);
        }
    }

    private VBox createMatchCard(MatchDto m) {
        VBox card = new VBox(6);
        card.getStyleClass().add("bracket-match-node");
        if ("LIVE".equals(m.getStatus())) {
            card.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5; -fx-background-color: #1a1414;");
        }

        // Top info
        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label numLbl = new Label("#" + m.getMatchNumber() + " " + (m.getMapName() != null ? m.getMapName() : ""));
        numLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #a1a1aa; -fx-font-weight: bold;");
        HBox.setHgrow(numLbl, Priority.ALWAYS);

        Label statusBadge = new Label(m.getStatus());
        if ("LIVE".equals(m.getStatus())) {
            statusBadge.getStyleClass().addAll("badge-pill", "badge-live-fx");
        } else if ("COMPLETE".equals(m.getStatus())) {
            statusBadge.getStyleClass().addAll("badge-pill", "badge-green-fx");
        } else {
            statusBadge.getStyleClass().addAll("badge-pill", "badge-purple-fx");
        }
        top.getChildren().addAll(numLbl, statusBadge);

        // Team A
        HBox teamARow = createTeamRow(m.getTeamAName(), m.getScoreA(), m.getKillsA(), m.getTeamAId() != null && m.getTeamAId().equals(m.getWinnerId()));
        // Team B
        HBox teamBRow = createTeamRow(m.getTeamBName(), m.getScoreB(), m.getKillsB(), m.getTeamBId() != null && m.getTeamBId().equals(m.getWinnerId()));

        card.getChildren().addAll(top, teamARow, teamBRow);

        // Quick action: click to enter or update score
        if (!m.isBye()) {
            card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
            card.setOnMouseClicked(e -> promptEnterScore(m));
        }

        return card;
    }

    private HBox createTeamRow(String name, int score, int kills, boolean isWinner) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 4 6; -fx-background-radius: 2;" + (isWinner ? "-fx-background-color: rgba(234, 88, 12, 0.15);" : ""));

        Label nameLbl = new Label((isWinner ? "[W] " : "") + (name != null ? name : "TBD"));
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isWinner ? "#ea580c;" : "#ffffff;"));
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        String scoreStr = String.valueOf(score);
        if (kills > 0) scoreStr += " (" + kills + "k)";
        Label scoreLbl = new Label(scoreStr);
        scoreLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isWinner ? "#ea580c;" : "#a1a1aa;"));

        row.getChildren().addAll(nameLbl, scoreLbl);
        return row;
    }

    private void promptEnterScore(MatchDto match) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("RECORD MATCH RESULT");
        dialog.setHeaderText("Match #" + match.getMatchNumber() + ": " + match.getTeamAName() + " vs " + match.getTeamBName());

        ButtonType btnLive = new ButtonType("Set LIVE", ButtonBar.ButtonData.OTHER);
        ButtonType btnSave = new ButtonType("Confirm & Advance Winner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLive, btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtScoreA = new TextField(String.valueOf(match.getScoreA()));
        TextField txtScoreB = new TextField(String.valueOf(match.getScoreB()));
        TextField txtKillsA = new TextField(String.valueOf(match.getKillsA() > 0 ? match.getKillsA() : match.getScoreA() * 4 + 2));
        TextField txtKillsB = new TextField(String.valueOf(match.getKillsB() > 0 ? match.getKillsB() : match.getScoreB() * 4 + 1));
        TextField txtReason = new TextField("Regular time completion");

        grid.add(new Label(match.getTeamAName() + " Score:"), 0, 0);
        grid.add(txtScoreA, 1, 0);
        grid.add(new Label(match.getTeamBName() + " Score:"), 0, 1);
        grid.add(txtScoreB, 1, 1);
        grid.add(new Label(match.getTeamAName() + " Kills:"), 0, 2);
        grid.add(txtKillsA, 1, 2);
        grid.add(new Label(match.getTeamBName() + " Kills:"), 0, 3);
        grid.add(txtKillsB, 1, 3);
        grid.add(new Label("Reason / Audit Note:"), 0, 4);
        grid.add(txtReason, 1, 4);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(response -> {
            try {
                int sA = Integer.parseInt(txtScoreA.getText().trim());
                int sB = Integer.parseInt(txtScoreB.getText().trim());
                int kA = Integer.parseInt(txtKillsA.getText().trim());
                int kB = Integer.parseInt(txtKillsB.getText().trim());

                if (response == btnLive) {
                    boolean ok = apiClient.updateLiveMatch(match.getId(), sA, sB, kA, kB, "LIVE");
                    if (ok) {
                        CyberAlert.showInfo("Match LIVE", "Match is now marked as LIVE with score " + sA + "-" + sB + " [" + kA + "-" + kB + " kills]!");
                        loadBracket();
                    } else {
                        CyberAlert.showError("Error", "Failed to update match status to LIVE.");
                    }
                } else if (response == btnSave) {
                    if (sA == sB) {
                        CyberAlert.showError("Draw Not Allowed", "Single-elimination match cannot end in a draw.");
                        return;
                    }
                    boolean ok = apiClient.recordMatchScore(match.getId(), sA, sB, kA, kB, txtReason.getText());
                    if (ok) {
                        CyberAlert.showInfo("Score Recorded", "Match score updated and winner advanced in bracket!");
                        loadBracket();
                    } else {
                        CyberAlert.showError("Score Error", "Failed to record score.");
                    }
                }
            } catch (NumberFormatException ex) {
                CyberAlert.showError("Input Error", "Please enter valid numeric scores.");
            }
        });
    }

    private void showAlert(String msg) {
        CyberAlert.showInfo("Bracket Operations", msg);
    }
}
