package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.ConductReportDto;
import com.esports.desktop.model.DisputeDto;
import com.esports.desktop.model.TournamentDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DisputesView extends VBox {
    private final ApiClient apiClient;
    private final ComboBox<TournamentDto> cmbTournament;
    private final TableView<DisputeDto> disputeTable;
    private final TableView<ConductReportDto> conductTable;

    public DisputesView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("DISPUTES & FAIR-PLAY INTEGRITY");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Review participant score disputes, investigate evidence clips, and issue tournament-level moderation decisions.");
        subtitle.getStyleClass().add("view-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Control bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(320);
        cmbTournament.setOnAction(e -> loadData());

        Button btnResolveDispute = new Button("⚖️ Resolve Dispute");
        btnResolveDispute.getStyleClass().add("btn-primary");
        btnResolveDispute.setOnAction(e -> showResolveDisputeDialog());

        Button btnModerate = new Button("🛡️ Moderate Player");
        btnModerate.getStyleClass().add("btn-danger");
        btnModerate.setOnAction(e -> showModerateDialog());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadData();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnResolveDispute, btnModerate, btnRefresh);

        // Dispute Table
        Label lblDisp = new Label("MATCH RESULT DISPUTES");
        lblDisp.setStyle("-fx-font-weight: bold; -fx-text-fill: #ea580c; -fx-font-size: 14px;");

        disputeTable = new TableView<>();
        setupDisputeTable();
        disputeTable.setPrefHeight(220);

        // Conduct Table
        Label lblCond = new Label("CONDUCT & FAIR-PLAY INCIDENT REPORTS");
        lblCond.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 14px;");

        conductTable = new TableView<>();
        setupConductTable();
        conductTable.setPrefHeight(220);

        getChildren().addAll(headerBox, topBar, lblDisp, disputeTable, lblCond, conductTable);
        loadTournaments();
    }

    private void setupDisputeTable() {
        TableColumn<DisputeDto, String> colId = new TableColumn<>("DISPUTE ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colId.setPrefWidth(100);

        TableColumn<DisputeDto, String> colMatch = new TableColumn<>("MATCH");
        colMatch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMatchId()));
        colMatch.setPrefWidth(120);

        TableColumn<DisputeDto, String> colTeam = new TableColumn<>("TEAM");
        colTeam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubmittedByTeamName()));
        colTeam.setPrefWidth(140);

        TableColumn<DisputeDto, String> colReason = new TableColumn<>("REASON");
        colReason.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReason()));
        colReason.setPrefWidth(260);

        TableColumn<DisputeDto, String> colEvidence = new TableColumn<>("EVIDENCE LINK");
        colEvidence.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEvidenceUrl()));
        colEvidence.setPrefWidth(180);

        TableColumn<DisputeDto, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(120);

        TableColumn<DisputeDto, String> colDec = new TableColumn<>("DECISION");
        colDec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDecision() != null ? c.getValue().getDecision() : "-"));
        colDec.setPrefWidth(150);

        disputeTable.getColumns().addAll(colId, colMatch, colTeam, colReason, colEvidence, colStatus, colDec);
    }

    private void setupConductTable() {
        TableColumn<ConductReportDto, String> colId = new TableColumn<>("REPORT ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colId.setPrefWidth(100);

        TableColumn<ConductReportDto, String> colPlayer = new TableColumn<>("REPORTED PLAYER");
        colPlayer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReportedPlayerName()));
        colPlayer.setPrefWidth(160);

        TableColumn<ConductReportDto, String> colTeam = new TableColumn<>("TEAM");
        colTeam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReportedTeamName()));
        colTeam.setPrefWidth(130);

        TableColumn<ConductReportDto, String> colCat = new TableColumn<>("CATEGORY");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colCat.setPrefWidth(160);

        TableColumn<ConductReportDto, String> colDesc = new TableColumn<>("DESCRIPTION");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colDesc.setPrefWidth(240);

        TableColumn<ConductReportDto, String> colAction = new TableColumn<>("ACTION TAKEN");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModerationAction() != null ? c.getValue().getModerationAction() : "NONE"));
        colAction.setPrefWidth(150);

        TableColumn<ConductReportDto, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(110);

        conductTable.getColumns().addAll(colId, colPlayer, colTeam, colCat, colDesc, colAction, colStatus);
    }

    private void loadTournaments() {
        List<TournamentDto> list = apiClient.getTournaments();
        cmbTournament.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty() && cmbTournament.getValue() == null) {
            cmbTournament.setValue(list.get(0));
            loadData();
        }
    }

    private void loadData() {
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) return;

        List<DisputeDto> disputes = apiClient.getDisputes(selected.getId());
        disputeTable.setItems(FXCollections.observableArrayList(disputes));

        List<ConductReportDto> reports = apiClient.getConductReports(selected.getId());
        conductTable.setItems(FXCollections.observableArrayList(reports));
    }

    private void showResolveDisputeDialog() {
        DisputeDto selected = disputeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a dispute from the upper table first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("RESOLVE MATCH DISPUTE");
        dialog.setHeaderText("Dispute #" + selected.getId() + " filed by " + selected.getSubmittedByTeamName());

        ButtonType btnSubmit = new ButtonType("Apply Resolution", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> cmbDecision = new ComboBox<>();
        cmbDecision.getItems().addAll("CONFIRMED_ORIGINAL_RESULT", "REVERSED_WINNER", "REPLAY_MATCH", "DISMISSED");
        cmbDecision.setValue("CONFIRMED_ORIGINAL_RESULT");

        TextArea txtNotes = new TextArea("Reviewed official spectator clip; result confirmed valid.");
        txtNotes.setPrefRowCount(3);

        grid.add(new Label("Organizer Ruling:"), 0, 0);
        grid.add(cmbDecision, 1, 0);
        grid.add(new Label("Ruling Explanation:"), 0, 1);
        grid.add(txtNotes, 1, 1);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnSubmit) {
                boolean ok = apiClient.resolveDispute(selected.getId(), cmbDecision.getValue(), txtNotes.getText());
                if (ok) {
                    CyberAlert.showInfo("Dispute Resolved", "Ruling applied and synchronized with Cloud Firestore.");
                    loadData();
                } else {
                    CyberAlert.showError("Resolution Error", "Failed to resolve dispute.");
                }
            }
        });
    }

    private void showModerateDialog() {
        ConductReportDto selected = conductTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Integrity & Moderation", "Please select an incident report from the lower table first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("TOURNAMENT MODERATION ACTION");
        dialog.setHeaderText("Report #" + selected.getId() + " against " + selected.getReportedPlayerName() + " (" + selected.getCategory() + ")");

        ButtonType btnSubmit = new ButtonType("Apply Moderation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> cmbAction = new ComboBox<>();
        cmbAction.getItems().addAll("WARNED", "SUSPENDED_FROM_TOURNAMENT", "REMOVED_FROM_TEAM", "BANNED_FROM_TOURNAMENT", "DISMISSED");
        cmbAction.setValue("WARNED");

        TextField txtReason = new TextField("Unsportsmanlike language in all-chat.");

        grid.add(new Label("Moderation Penalty:"), 0, 0);
        grid.add(cmbAction, 1, 0);
        grid.add(new Label("Official Reason:"), 0, 1);
        grid.add(txtReason, 1, 1);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(res -> {
            if (res == btnSubmit) {
                boolean ok = apiClient.resolveConductReport(selected.getId(), cmbAction.getValue(), txtReason.getText());
                if (ok) {
                    CyberAlert.showInfo("Moderation Applied", "Fair-play moderation action recorded into tournament audit trail.");
                    loadData();
                } else {
                    CyberAlert.showError("Moderation Error", "Failed to apply moderation action.");
                }
            }
        });
    }

    private void showAlert(String msg) {
        CyberAlert.showInfo("Disputes & Integrity", msg);
    }
}
