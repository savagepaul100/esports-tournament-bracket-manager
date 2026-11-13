package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.MatchDto;
import com.esports.desktop.model.TournamentDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchesView extends VBox {
    private final ApiClient apiClient;
    private final ComboBox<TournamentDto> cmbTournament;
    private final TableView<MatchDto> tableView;

    public MatchesView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("MATCH OPERATIONS & LIVE SCORING ENGINE");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Real-time round scores, kill tracking, manual match overrides, and bracket finalization.");
        subtitle.getStyleClass().add("view-subtitle");

        HBox guidanceBox = new HBox(10);
        guidanceBox.setAlignment(Pos.CENTER_LEFT);
        guidanceBox.setStyle("-fx-background-color: #18181b; -fx-border-color: #ea580c; -fx-border-width: 1 1 1 3; " +
                "-fx-border-radius: 2; -fx-background-radius: 2; -fx-padding: 8 14; -fx-margin: 0 0 10 0;");
        Label guideIcon = new Label("💡");
        guideIcon.setStyle("-fx-font-size: 15px;");
        VBox guideText = new VBox(2);
        Label guideTitle = new Label("LIVE MATCH CONTROL ROOM GUIDE");
        guideTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");
        Label guideDesc = new Label("Select a match to update rounds and total kills live, adjust room IPs/passwords, or finalize results for cloud bracket progression.");
        guideDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        guideText.getChildren().addAll(guideTitle, guideDesc);
        guidanceBox.getChildren().addAll(guideIcon, guideText);

        headerBox.getChildren().addAll(title, subtitle, guidanceBox);

        // Control bar
        HBox topBar = new HBox(8);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #a1a1aa;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(260);
        cmbTournament.setOnAction(e -> loadMatches());

        Button btnSetLive = new Button("🔴 Set Match LIVE");
        btnSetLive.getStyleClass().add("btn-danger");
        btnSetLive.setOnAction(e -> showSetLiveDialog());

        Button btnScore = new Button("🎮 Finalize Result");
        btnScore.getStyleClass().add("btn-primary");
        btnScore.setOnAction(e -> showScoreDialog(false));

        Button btnEditMatch = new Button("✏️ Modify Details");
        btnEditMatch.getStyleClass().add("btn-cyan");
        btnEditMatch.setOnAction(e -> showEditMatchDialog());

        Button btnDeleteMatch = new Button("🗑️ Delete Match");
        btnDeleteMatch.getStyleClass().add("btn-danger");
        btnDeleteMatch.setOnAction(e -> deleteSelectedMatch());

        Button btnSchedule = new Button("🕒 Room & Map");
        btnSchedule.getStyleClass().add("btn-outline");
        btnSchedule.setOnAction(e -> showScheduleDialog());

        Button btnSeed = new Button("⚡ Seed Prototype");
        btnSeed.getStyleClass().add("btn-success");
        btnSeed.setOnAction(e -> seedData());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadMatches();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnSetLive, btnScore, btnEditMatch, btnDeleteMatch, btnSchedule, btnSeed, btnRefresh);

        // Table
        tableView = new TableView<>();
        setupTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getChildren().addAll(headerBox, topBar, tableView);
        loadTournaments();
    }

    private void setupTable() {
        TableColumn<MatchDto, String> colRound = new TableColumn<>("ROUND");
        colRound.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoundName()));
        colRound.setPrefWidth(115);

        TableColumn<MatchDto, String> colNum = new TableColumn<>("MATCH #");
        colNum.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getMatchNumber()));
        colNum.setPrefWidth(70);

        TableColumn<MatchDto, String> colTeamA = new TableColumn<>("TEAM A");
        colTeamA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamAName()));
        colTeamA.setPrefWidth(140);

        TableColumn<MatchDto, String> colScore = new TableColumn<>("SCORE (ROUNDS)");
        colScore.setCellValueFactory(c -> {
            MatchDto m = c.getValue();
            if ("LIVE".equals(m.getStatus())) {
                return new SimpleStringProperty("🔥 " + m.getScoreA() + " - " + m.getScoreB());
            } else if ("COMPLETE".equals(m.getStatus())) {
                return new SimpleStringProperty(m.getScoreA() + " - " + m.getScoreB());
            } else {
                return new SimpleStringProperty("vs");
            }
        });
        colScore.setPrefWidth(110);

        TableColumn<MatchDto, String> colKills = new TableColumn<>("KILLS (A - B)");
        colKills.setCellValueFactory(c -> {
            MatchDto m = c.getValue();
            return new SimpleStringProperty(m.getKillsA() + " - " + m.getKillsB());
        });
        colKills.setPrefWidth(95);

        TableColumn<MatchDto, String> colTeamB = new TableColumn<>("TEAM B");
        colTeamB.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamBName()));
        colTeamB.setPrefWidth(140);

        TableColumn<MatchDto, String> colWinner = new TableColumn<>("WINNER");
        colWinner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWinnerName() != null ? c.getValue().getWinnerName() : "-"));
        colWinner.setPrefWidth(130);

        TableColumn<MatchDto, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> {
            String s = c.getValue().getStatus();
            return new SimpleStringProperty("LIVE".equals(s) ? "🔴 LIVE NOW" : s);
        });
        colStatus.setPrefWidth(110);

        TableColumn<MatchDto, String> colRoom = new TableColumn<>("ROOM ID");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomId() != null ? c.getValue().getRoomId() : "Pending"));
        colRoom.setPrefWidth(115);

        TableColumn<MatchDto, String> colPass = new TableColumn<>("PASSWORD");
        colPass.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomPassword() != null ? c.getValue().getRoomPassword() : "••••"));
        colPass.setPrefWidth(85);

        TableColumn<MatchDto, String> colMap = new TableColumn<>("MAP");
        colMap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMapName() != null ? c.getValue().getMapName() : "-"));
        colMap.setPrefWidth(85);

        TableColumn<MatchDto, String> colTime = new TableColumn<>("SCHEDULED TIME");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScheduledTime() != null ? c.getValue().getScheduledTime() : "-"));
        colTime.setPrefWidth(150);

        tableView.getColumns().addAll(colRound, colNum, colTeamA, colScore, colKills, colTeamB, colWinner, colStatus, colRoom, colPass, colMap, colTime);
    }

    private void loadTournaments() {
        List<TournamentDto> list = apiClient.getTournaments();
        cmbTournament.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty() && cmbTournament.getValue() == null) {
            cmbTournament.setValue(list.get(0));
            loadMatches();
        }
    }

    private void loadMatches() {
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) return;
        List<MatchDto> matches = apiClient.getMatches(selected.getId());
        tableView.setItems(FXCollections.observableArrayList(matches));
    }

    private void seedData() {
        boolean confirm = CyberAlert.showConfirm("Seed Prototype Data",
                "Populate Cloud Firestore with multi-game championships, live ongoing matches, and kills?",
                "This configures VCT Masters (PRX 11-12 EG LIVE), CS2 Cologne Major (NaVi vs FaZe LIVE), Dota 2, and Apex Legends.");
        if (confirm) {
            boolean ok = apiClient.seedPrototypeData();
            if (ok) {
                CyberAlert.showInfo("Seed Complete", "Cloud Firestore populated with multi-game esports data and real-time live match scores!");
                loadTournaments();
                loadMatches();
            } else {
                CyberAlert.showError("Seed Error", "Failed to seed prototype data. Check Firebase connectivity.");
            }
        }
    }

    private void showSetLiveDialog() {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a match from the table first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("SET MATCH AS LIVE IN-PROGRESS");
        dialog.setHeaderText("Set Match #" + selected.getMatchNumber() + " (" + selected.getTeamAName() + " vs " + selected.getTeamBName() + ") as LIVE");

        ButtonType btnLive = new ButtonType("Go LIVE 🔴", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLive, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtScoreA = new TextField(String.valueOf(selected.getScoreA()));
        TextField txtScoreB = new TextField(String.valueOf(selected.getScoreB()));
        TextField txtKillsA = new TextField(String.valueOf(selected.getKillsA() > 0 ? selected.getKillsA() : selected.getScoreA() * 4 + 2));
        TextField txtKillsB = new TextField(String.valueOf(selected.getKillsB() > 0 ? selected.getKillsB() : selected.getScoreB() * 4 + 1));

        grid.add(new Label(selected.getTeamAName() + " Score (Rounds):"), 0, 0);
        grid.add(txtScoreA, 1, 0);
        grid.add(new Label(selected.getTeamBName() + " Score (Rounds):"), 0, 1);
        grid.add(txtScoreB, 1, 1);
        grid.add(new Label(selected.getTeamAName() + " Kills / Frags:"), 0, 2);
        grid.add(txtKillsA, 1, 2);
        grid.add(new Label(selected.getTeamBName() + " Kills / Frags:"), 0, 3);
        grid.add(txtKillsB, 1, 3);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnLive) {
                try {
                    int sA = Integer.parseInt(txtScoreA.getText().trim());
                    int sB = Integer.parseInt(txtScoreB.getText().trim());
                    int kA = Integer.parseInt(txtKillsA.getText().trim());
                    int kB = Integer.parseInt(txtKillsB.getText().trim());

                    boolean ok = apiClient.updateLiveMatch(selected.getId(), sA, sB, kA, kB, "LIVE");
                    if (ok) {
                        CyberAlert.showInfo("Match LIVE", "Match is now marked as LIVE with score " + sA + "-" + sB + " [" + kA + "-" + kB + " Kills]!");
                        loadMatches();
                    } else {
                        CyberAlert.showError("Error", "Could not set match to LIVE.");
                    }
                } catch (NumberFormatException ex) {
                    CyberAlert.showError("Input Error", "Scores and kills must be valid numbers.");
                }
            }
        });
    }

    private void showScoreDialog(boolean isCorrection) {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a match from the table first.");
            return;
        }

        if (selected.isBye()) {
            showAlert("This match is an automatic BYE progression.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isCorrection ? "CORRECT MATCH RESULT (AUDITED)" : "CONFIRM & FINALIZE MATCH RESULT");
        dialog.setHeaderText("Match #" + selected.getMatchNumber() + ": " + selected.getTeamAName() + " vs " + selected.getTeamBName());

        ButtonType btnConfirm = new ButtonType(isCorrection ? "Save Correction" : "Finalize Score & Advance Winner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirm, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtScoreA = new TextField(String.valueOf(selected.getScoreA()));
        TextField txtScoreB = new TextField(String.valueOf(selected.getScoreB()));
        TextField txtKillsA = new TextField(String.valueOf(selected.getKillsA() > 0 ? selected.getKillsA() : selected.getScoreA() * 4 + 4));
        TextField txtKillsB = new TextField(String.valueOf(selected.getKillsB() > 0 ? selected.getKillsB() : selected.getScoreB() * 4 + 2));
        TextField txtReason = new TextField(isCorrection ? "Official referee correction after dispute" : "Final match score verified");

        grid.add(new Label(selected.getTeamAName() + " Score (Rounds):"), 0, 0);
        grid.add(txtScoreA, 1, 0);
        grid.add(new Label(selected.getTeamBName() + " Score (Rounds):"), 0, 1);
        grid.add(txtScoreB, 1, 1);
        grid.add(new Label(selected.getTeamAName() + " Total Kills:"), 0, 2);
        grid.add(txtKillsA, 1, 2);
        grid.add(new Label(selected.getTeamBName() + " Total Kills:"), 0, 3);
        grid.add(txtKillsB, 1, 3);
        grid.add(new Label("Audit Reason:"), 0, 4);
        grid.add(txtReason, 1, 4);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnConfirm) {
                try {
                    int sA = Integer.parseInt(txtScoreA.getText().trim());
                    int sB = Integer.parseInt(txtScoreB.getText().trim());
                    int kA = Integer.parseInt(txtKillsA.getText().trim());
                    int kB = Integer.parseInt(txtKillsB.getText().trim());

                    if (sA == sB) {
                        CyberAlert.showError("Draw Not Allowed", "Single-elimination match cannot end in a draw.");
                        return;
                    }

                    boolean ok = apiClient.recordMatchScore(selected.getId(), sA, sB, kA, kB, txtReason.getText());
                    if (ok) {
                        CyberAlert.showInfo("Score Recorded", "Match finalized! Winner advanced in Cloud Firestore bracket.");
                        loadMatches();
                    } else {
                        CyberAlert.showError("Score Error", "Failed to record score.");
                    }
                } catch (NumberFormatException ex) {
                    CyberAlert.showError("Input Error", "Scores and kills must be valid numbers.");
                }
            }
        });
    }

    private void showEditMatchDialog() {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a match from the table first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("MANUALLY MODIFY MATCH DETAILS");
        dialog.setHeaderText("Directly edit live score, kills, status, and teams in Cloud Firestore");

        ButtonType btnSave = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTeamA = new TextField(selected.getTeamAName());
        TextField txtTeamB = new TextField(selected.getTeamBName());
        TextField txtScoreA = new TextField(String.valueOf(selected.getScoreA()));
        TextField txtScoreB = new TextField(String.valueOf(selected.getScoreB()));
        TextField txtKillsA = new TextField(String.valueOf(selected.getKillsA()));
        TextField txtKillsB = new TextField(String.valueOf(selected.getKillsB()));

        ComboBox<String> cmbStatus = new ComboBox<>();
        cmbStatus.getItems().addAll("UPCOMING", "LIVE", "COMPLETE", "CANCELLED");
        cmbStatus.setValue(selected.getStatus() != null ? selected.getStatus() : "UPCOMING");

        TextField txtRoom = new TextField(selected.getRoomId() != null ? selected.getRoomId() : "");
        TextField txtPass = new TextField(selected.getRoomPassword() != null ? selected.getRoomPassword() : "");
        TextField txtMap = new TextField(selected.getMapName() != null ? selected.getMapName() : "");
        TextField txtTime = new TextField(selected.getScheduledTime() != null ? selected.getScheduledTime() : "");

        grid.add(new Label("Team A Name:"), 0, 0);
        grid.add(txtTeamA, 1, 0);
        grid.add(new Label("Team B Name:"), 0, 1);
        grid.add(txtTeamB, 1, 1);
        grid.add(new Label("Score A (Rounds):"), 0, 2);
        grid.add(txtScoreA, 1, 2);
        grid.add(new Label("Score B (Rounds):"), 0, 3);
        grid.add(txtScoreB, 1, 3);
        grid.add(new Label("Kills A:"), 0, 4);
        grid.add(txtKillsA, 1, 4);
        grid.add(new Label("Kills B:"), 0, 5);
        grid.add(txtKillsB, 1, 5);
        grid.add(new Label("Match Status:"), 0, 6);
        grid.add(cmbStatus, 1, 6);
        grid.add(new Label("Room ID:"), 0, 7);
        grid.add(txtRoom, 1, 7);
        grid.add(new Label("Room Password:"), 0, 8);
        grid.add(txtPass, 1, 8);
        grid.add(new Label("Map Name:"), 0, 9);
        grid.add(txtMap, 1, 9);
        grid.add(new Label("Scheduled Time:"), 0, 10);
        grid.add(txtTime, 1, 10);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnSave) {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("teamAName", txtTeamA.getText().trim());
                    updates.put("teamBName", txtTeamB.getText().trim());
                    updates.put("scoreA", Integer.parseInt(txtScoreA.getText().trim()));
                    updates.put("scoreB", Integer.parseInt(txtScoreB.getText().trim()));
                    updates.put("killsA", Integer.parseInt(txtKillsA.getText().trim()));
                    updates.put("killsB", Integer.parseInt(txtKillsB.getText().trim()));
                    updates.put("status", cmbStatus.getValue());
                    updates.put("roomId", txtRoom.getText().trim());
                    updates.put("roomPassword", txtPass.getText().trim());
                    updates.put("mapName", txtMap.getText().trim());
                    updates.put("scheduledTime", txtTime.getText().trim());

                    boolean ok = apiClient.updateMatchDetails(selected.getId(), updates);
                    if (ok) {
                        CyberAlert.showInfo("Match Updated", "Match details synchronized to Cloud Firestore!");
                        loadMatches();
                    } else {
                        CyberAlert.showError("Edit Error", "Failed to update match details.");
                    }
                } catch (Exception ex) {
                    CyberAlert.showError("Input Error", "Please ensure scores and kills are valid numbers.");
                }
            }
        });
    }

    private void deleteSelectedMatch() {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a match from the table first.");
            return;
        }

        boolean confirm = CyberAlert.showConfirm("Delete Match",
                "Delete Match #" + selected.getMatchNumber() + " (" + selected.getTeamAName() + " vs " + selected.getTeamBName() + ")?",
                "This match will be permanently deleted from Cloud Firestore.");
        if (confirm) {
            boolean ok = apiClient.deleteMatch(selected.getId());
            if (ok) {
                CyberAlert.showInfo("Match Deleted", "Match deleted from Cloud Firestore.");
                loadMatches();
            } else {
                CyberAlert.showError("Delete Error", "Could not delete match.");
            }
        }
    }

    private void showScheduleDialog() {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a match from the table first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("SCHEDULE & ROOM CREDENTIALS");
        dialog.setHeaderText("Configure game lobby details for Match #" + selected.getMatchNumber());

        ButtonType btnSave = new ButtonType("Save Details", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTime = new TextField(selected.getScheduledTime() != null ? selected.getScheduledTime() : "20:00 IST");
        TextField txtRoom = new TextField(selected.getRoomId() != null ? selected.getRoomId() : "NX-ROOM-" + (100 + selected.getMatchNumber()));
        TextField txtPass = new TextField(selected.getRoomPassword() != null ? selected.getRoomPassword() : "PASS" + (100 + selected.getMatchNumber()));
        TextField txtMap = new TextField(selected.getMapName() != null ? selected.getMapName() : "Ascent");

        grid.add(new Label("Scheduled Time:"), 0, 0);
        grid.add(txtTime, 1, 0);
        grid.add(new Label("Game Room ID:"), 0, 1);
        grid.add(txtRoom, 1, 1);
        grid.add(new Label("Room Password:"), 0, 2);
        grid.add(txtPass, 1, 2);
        grid.add(new Label("Map Name:"), 0, 3);
        grid.add(txtMap, 1, 3);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnSave) {
                boolean ok = apiClient.updateMatchSchedule(selected.getId(), txtTime.getText(), txtRoom.getText(), txtPass.getText(), txtMap.getText());
                if (ok) {
                    CyberAlert.showInfo("Schedule Saved", "Lobby room credentials published to tournament participants & web portal.");
                    loadMatches();
                } else {
                    CyberAlert.showError("Schedule Error", "Failed to update match schedule.");
                }
            }
        });
    }

    private void showAlert(String msg) {
        CyberAlert.showInfo("Match Operations", msg);
    }
}
