package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.TeamDto;
import com.esports.desktop.model.TournamentDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class TeamsView extends VBox {
    private final ApiClient apiClient;
    private final ComboBox<TournamentDto> cmbTournament;
    private final TableView<TeamDto> tableView;

    public TeamsView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("TEAMS & REGISTRATION MANAGEMENT");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Approve or reject team applications, inspect rosters, and track check-in readiness.");
        subtitle.getStyleClass().add("view-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Filter / Controls bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Select Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(320);
        cmbTournament.setOnAction(e -> loadTeams());

        Button btnApprove = new Button("✓ Approve Registration");
        btnApprove.getStyleClass().add("btn-success");
        btnApprove.setOnAction(e -> updateStatus("APPROVED"));

        Button btnReject = new Button("✗ Reject Registration");
        btnReject.getStyleClass().add("btn-danger");
        btnReject.setOnAction(e -> updateStatus("REJECTED"));

        Button btnCheckIn = new Button("Toggle Check-In");
        btnCheckIn.getStyleClass().add("btn-cyan");
        btnCheckIn.setOnAction(e -> toggleCheckIn());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadTeams();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnApprove, btnReject, btnCheckIn, btnRefresh);

        // Table
        tableView = new TableView<>();
        setupTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getChildren().addAll(headerBox, topBar, tableView);
        loadTournaments();
    }

    private void setupTable() {
        TableColumn<TeamDto, String> colName = new TableColumn<>("TEAM NAME");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(180);

        TableColumn<TeamDto, String> colTag = new TableColumn<>("TAG");
        colTag.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTag()));
        colTag.setPrefWidth(80);

        TableColumn<TeamDto, String> colCaptain = new TableColumn<>("CAPTAIN");
        colCaptain.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCaptainEmail() != null ? c.getValue().getCaptainEmail() : c.getValue().getCaptainId()));
        colCaptain.setPrefWidth(180);

        TableColumn<TeamDto, String> colRoster = new TableColumn<>("ROSTER PLAYERS");
        colRoster.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getPlayerNames())));
        colRoster.setPrefWidth(300);

        TableColumn<TeamDto, String> colSeed = new TableColumn<>("SEED");
        colSeed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSeed() != null ? "#" + c.getValue().getSeed() : "-"));
        colSeed.setPrefWidth(70);

        TableColumn<TeamDto, String> colStatus = new TableColumn<>("REGISTRATION");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistrationStatus()));
        colStatus.setPrefWidth(140);

        TableColumn<TeamDto, String> colCheckIn = new TableColumn<>("CHECKED IN");
        colCheckIn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isCheckedIn() ? "READY" : "NO"));
        colCheckIn.setPrefWidth(100);

        tableView.getColumns().addAll(colName, colTag, colCaptain, colRoster, colSeed, colStatus, colCheckIn);
    }

    private void loadTournaments() {
        List<TournamentDto> list = apiClient.getTournaments();
        cmbTournament.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty() && cmbTournament.getValue() == null) {
            cmbTournament.setValue(list.get(0));
            loadTeams();
        }
    }

    private void loadTeams() {
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) return;
        List<TeamDto> teams = apiClient.getTeams(selected.getId());
        tableView.setItems(FXCollections.observableArrayList(teams));
    }

    private void updateStatus(String status) {
        TeamDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a team from the table first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Decision by organizer");
        dialog.setTitle("Registration Decision");
        dialog.setHeaderText("Set registration status to " + status + " for " + selected.getName());
        dialog.setContentText("Reason or notes for captain:");

        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(reason -> {
            boolean ok = apiClient.updateTeamStatus(selected.getId(), status, reason);
            if (ok) {
                CyberAlert.showInfo("Status Updated", "Team status updated to " + status + " and synced to Cloud Firestore.");
                loadTeams();
            } else {
                CyberAlert.showError("Update Error", "Failed to update team registration status.");
            }
        });
    }

    private void toggleCheckIn() {
        TeamDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Teams", "Please select a team from the table first.");
            return;
        }

        boolean newCheckIn = !selected.isCheckedIn();
        boolean ok = apiClient.setTeamCheckIn(selected.getId(), newCheckIn);
        if (ok) {
            CyberAlert.showInfo("Check-In Updated", selected.getName() + " check-in marked as: " + (newCheckIn ? "READY" : "PENDING"));
            loadTeams();
        } else {
            CyberAlert.showError("Update Error", "Failed to update check-in status.");
        }
    }

    private void showAlert(String msg) {
        CyberAlert.showInfo("Team Operations", msg);
    }
}
