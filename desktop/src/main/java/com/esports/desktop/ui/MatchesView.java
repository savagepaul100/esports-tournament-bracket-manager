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

import java.util.List;

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
        Label title = new Label("MATCH OPERATIONS & SCORING");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Distribute game room codes, enter verified scores, and correct results with audit tracking.");
        subtitle.getStyleClass().add("view-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Control bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Select Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(320);
        cmbTournament.setOnAction(e -> loadMatches());

        Button btnScore = new Button("🎮 Enter Score");
        btnScore.getStyleClass().add("btn-primary");
        btnScore.setOnAction(e -> showScoreDialog(false));

        Button btnCorrect = new Button("✏️ Correct Result");
        btnCorrect.getStyleClass().add("btn-danger");
        btnCorrect.setOnAction(e -> showScoreDialog(true));

        Button btnSchedule = new Button("🕒 Room & Schedule");
        btnSchedule.getStyleClass().add("btn-cyan");
        btnSchedule.setOnAction(e -> showScheduleDialog());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadMatches();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnScore, btnCorrect, btnSchedule, btnRefresh);

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
        colRound.setPrefWidth(120);

        TableColumn<MatchDto, String> colNum = new TableColumn<>("MATCH #");
        colNum.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getMatchNumber()));
        colNum.setPrefWidth(80);

        TableColumn<MatchDto, String> colTeamA = new TableColumn<>("TEAM A");
        colTeamA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamAName()));
        colTeamA.setPrefWidth(160);

        TableColumn<MatchDto, String> colScore = new TableColumn<>("SCORE");
        colScore.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScoreA() + " - " + c.getValue().getScoreB()));
        colScore.setPrefWidth(90);

        TableColumn<MatchDto, String> colTeamB = new TableColumn<>("TEAM B");
        colTeamB.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamBName()));
        colTeamB.setPrefWidth(160);

        TableColumn<MatchDto, String> colWinner = new TableColumn<>("WINNER");
        colWinner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWinnerName() != null ? c.getValue().getWinnerName() : "-"));
        colWinner.setPrefWidth(150);

        TableColumn<MatchDto, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(120);

        TableColumn<MatchDto, String> colRoom = new TableColumn<>("ROOM ID");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomId() != null ? c.getValue().getRoomId() : "Pending"));
        colRoom.setPrefWidth(120);

        TableColumn<MatchDto, String> colPass = new TableColumn<>("PASSWORD");
        colPass.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomPassword() != null ? c.getValue().getRoomPassword() : "••••"));
        colPass.setPrefWidth(100);

        TableColumn<MatchDto, String> colMap = new TableColumn<>("MAP");
        colMap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMapName() != null ? c.getValue().getMapName() : "Ascent"));
        colMap.setPrefWidth(100);

        tableView.getColumns().addAll(colRound, colNum, colTeamA, colScore, colTeamB, colWinner, colStatus, colRoom, colPass, colMap);
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
        dialog.setTitle(isCorrection ? "CORRECT MATCH RESULT (AUDITED)" : "CONFIRM MATCH RESULT");
        dialog.setHeaderText("Match #" + selected.getMatchNumber() + ": " + selected.getTeamAName() + " vs " + selected.getTeamBName());

        ButtonType btnConfirm = new ButtonType(isCorrection ? "Save Correction" : "Confirm Score", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirm, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtScoreA = new TextField(String.valueOf(selected.getScoreA()));
        TextField txtScoreB = new TextField(String.valueOf(selected.getScoreB()));
        TextField txtReason = new TextField(isCorrection ? "Official referee correction after dispute" : "Final match score verified");

        grid.add(new Label(selected.getTeamAName() + " Score:"), 0, 0);
        grid.add(txtScoreA, 1, 0);
        grid.add(new Label(selected.getTeamBName() + " Score:"), 0, 1);
        grid.add(txtScoreB, 1, 1);
        grid.add(new Label(isCorrection ? "Required Audit Reason:" : "Audit Note:"), 0, 2);
        grid.add(txtReason, 1, 2);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnConfirm) {
                try {
                    int sA = Integer.parseInt(txtScoreA.getText());
                    int sB = Integer.parseInt(txtScoreB.getText());
                    if (sA == sB) {
                        CyberAlert.showError("Draw Not Allowed", "Single-elimination match cannot end in a draw.");
                        return;
                    }
                    boolean ok = apiClient.recordMatchScore(selected.getId(), sA, sB, txtReason.getText());
                    if (ok) {
                        CyberAlert.showInfo("Score Recorded", "Match score updated and synchronized with Cloud Firestore!");
                        loadMatches();
                    } else {
                        CyberAlert.showError("Score Error", "Failed to record score.");
                    }
                } catch (NumberFormatException ex) {
                    CyberAlert.showError("Input Error", "Scores must be valid numbers.");
                }
            }
        });
    }

    private void showScheduleDialog() {
        MatchDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Match Operations", "Please select a match from the table first.");
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
