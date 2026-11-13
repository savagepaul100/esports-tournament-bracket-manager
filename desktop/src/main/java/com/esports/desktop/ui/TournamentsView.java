package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.TournamentDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class TournamentsView extends VBox {
    private final ApiClient apiClient;
    private final TableView<TournamentDto> tableView;

    public TournamentsView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("TOURNAMENT MANAGEMENT");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Configure rules, formats, max teams, and advance tournament lifecycle stages.");
        subtitle.getStyleClass().add("view-subtitle");

        HBox guidanceBox = new HBox(10);
        guidanceBox.setAlignment(Pos.CENTER_LEFT);
        guidanceBox.setStyle("-fx-background-color: #18181b; -fx-border-color: #ea580c; -fx-border-width: 1 1 1 3; " +
                "-fx-border-radius: 2; -fx-background-radius: 2; -fx-padding: 8 14; -fx-margin: 0 0 10 0;");
        Label guideIcon = new Label("💡");
        guideIcon.setStyle("-fx-font-size: 15px;");
        VBox guideText = new VBox(2);
        Label guideTitle = new Label("TOURNAMENT SETUP & LIFECYCLE GUIDE");
        guideTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");
        Label guideDesc = new Label("Select any tournament to advance its status (Registration → Check-in → In Progress → Completed) or delete with one click.");
        guideDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        guideText.getChildren().addAll(guideTitle, guideDesc);
        guidanceBox.getChildren().addAll(guideIcon, guideText);

        headerBox.getChildren().addAll(title, subtitle, guidanceBox);

        // Actions Bar
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        Button btnCreate = new Button("+ Create Tournament");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setOnAction(e -> showCreateDialog());

        Button btnRegOpen = new Button("Open Registration");
        btnRegOpen.getStyleClass().add("btn-outline");
        btnRegOpen.setOnAction(e -> advanceStatus("REGISTRATION_OPEN"));

        Button btnCheckIn = new Button("Start Check-in");
        btnCheckIn.getStyleClass().add("btn-outline");
        btnCheckIn.setOnAction(e -> advanceStatus("CHECK_IN"));

        Button btnInProgress = new Button("Set In Progress");
        btnInProgress.getStyleClass().add("btn-outline");
        btnInProgress.setOnAction(e -> advanceStatus("IN_PROGRESS"));

        Button btnComplete = new Button("Mark Completed");
        btnComplete.getStyleClass().add("btn-outline");
        btnComplete.setOnAction(e -> advanceStatus("COMPLETED"));

        Button btnDelete = new Button("🗑️ Delete");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> deleteSelectedTournament());

        Button btnExport = new Button("📥 Export CSV");
        btnExport.getStyleClass().add("btn-cyan");
        btnExport.setOnAction(e -> exportCsv());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> loadData());

        actionsBar.getChildren().addAll(btnCreate, btnRegOpen, btnCheckIn, btnInProgress, btnComplete, btnDelete, btnExport, btnRefresh);

        // Table
        tableView = new TableView<>();
        setupTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getChildren().addAll(headerBox, actionsBar, tableView);
        loadData();
    }

    private void setupTable() {
        TableColumn<TournamentDto, String> colName = new TableColumn<>("TOURNAMENT NAME");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(240);

        TableColumn<TournamentDto, String> colGame = new TableColumn<>("GAME");
        colGame.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGame()));
        colGame.setPrefWidth(140);

        TableColumn<TournamentDto, String> colMode = new TableColumn<>("MODE / FORMAT");
        colMode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMode()));
        colMode.setPrefWidth(180);

        TableColumn<TournamentDto, String> colStatus = new TableColumn<>("LIFECYCLE STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(160);

        TableColumn<TournamentDto, String> colMax = new TableColumn<>("CAPACITY");
        colMax.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaxParticipants() + " Teams"));
        colMax.setPrefWidth(100);

        TableColumn<TournamentDto, String> colPub = new TableColumn<>("PUBLIC");
        colPub.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isPublished() ? "YES" : "NO"));
        colPub.setPrefWidth(90);

        tableView.getColumns().addAll(colName, colGame, colMode, colStatus, colMax, colPub);
    }

    private void loadData() {
        List<TournamentDto> list = apiClient.getTournaments();
        tableView.setItems(FXCollections.observableArrayList(list));
    }

    private void advanceStatus(String newStatus) {
        TournamentDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a tournament from the table first.");
            return;
        }

        boolean ok = apiClient.updateTournamentStatus(selected.getId(), newStatus);
        if (ok) {
            loadData();
        } else {
            showAlert("Failed to update status.");
        }
    }

    private void deleteSelectedTournament() {
        TournamentDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a tournament from the table first.");
            return;
        }

        boolean confirm = CyberAlert.showConfirm("Delete Tournament",
                "Are you sure you want to delete '" + selected.getName() + "'?",
                "This will permanently remove the tournament and its linked matches/teams from Cloud Firestore.");
        if (confirm) {
            boolean ok = apiClient.deleteTournament(selected.getId());
            if (ok) {
                CyberAlert.showInfo("Deleted", "Tournament '" + selected.getName() + "' deleted from Cloud Firestore.");
                loadData();
            } else {
                CyberAlert.showError("Delete Error", "Could not delete tournament.");
            }
        }
    }

    private void exportCsv() {
        TournamentDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a tournament from the table first.");
            return;
        }
        // Build CSV content in-memory from Firestore data
        StringBuilder sb = new StringBuilder();
        sb.append("Tournament ID,Name,Game,Mode,Status,Max Teams,Published\n");
        sb.append(String.join(",",
                selected.getId(),
                "\"" + selected.getName() + "\"",
                selected.getGame() != null ? selected.getGame() : "",
                selected.getMode() != null ? selected.getMode() : "",
                selected.getStatus() != null ? selected.getStatus() : "",
                String.valueOf(selected.getMaxParticipants()),
                selected.isPublished() ? "YES" : "NO"
        ));
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(sb.toString());
        clipboard.setContent(content);
        CyberAlert.showInfo("CSV Exported", "Tournament data copied to clipboard as CSV!");
    }

    private void showCreateDialog() {
        Dialog<TournamentDto> dialog = new Dialog<>();
        dialog.setTitle("CREATE NEW ESPORTS TOURNAMENT");
        dialog.setHeaderText("Configure tournament format, participant limit, and game mode.");

        ButtonType btnCreateType = new ButtonType("Create Tournament", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCreateType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtName = new TextField();
        txtName.setPromptText("e.g. VALORANT APEX CHALLENGERS");

        ComboBox<String> cmbGame = new ComboBox<>();
        cmbGame.getItems().addAll(
                "COUNTER-STRIKE 2",
                "VALORANT",
                "LEAGUE OF LEGENDS",
                "DOTA 2",
                "RAINBOW SIX SIEGE",
                "BGMI",
                "CALL OF DUTY: WARZONE",
                "ROCKET LEAGUE",
                "APEX LEGENDS"
        );
        cmbGame.setValue("COUNTER-STRIKE 2");
        cmbGame.setEditable(true);

        ComboBox<String> cmbMode = new ComboBox<>();
        cmbMode.getItems().addAll("SINGLE_ELIMINATION_TDM", "SINGLE_ELIMINATION_CLASH", "BATTLE_ROYALE");
        cmbMode.setValue("SINGLE_ELIMINATION_TDM");

        TextField txtMax = new TextField("8");
        TextArea txtRules = new TextArea("Standard competitive rules. Anti-cheat screen recording mandatory.");
        txtRules.setPrefRowCount(3);

        grid.add(new Label("Tournament Name:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Esports Game:"), 0, 1);
        grid.add(cmbGame, 1, 1);
        grid.add(new Label("Format / Mode:"), 0, 2);
        grid.add(cmbMode, 1, 2);
        grid.add(new Label("Max Teams:"), 0, 3);
        grid.add(txtMax, 1, 3);
        grid.add(new Label("Rules & Notes:"), 0, 4);
        grid.add(txtRules, 1, 4);

        dialog.getDialogPane().setContent(grid);

        CyberAlert.style(dialog);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCreateType) {
                TournamentDto t = new TournamentDto();
                t.setName(txtName.getText().isBlank() ? "Untitled Tournament" : txtName.getText());
                String selectedGame = cmbGame.getValue() != null && !cmbGame.getValue().isBlank() ? cmbGame.getValue() : "COUNTER-STRIKE 2";
                t.setGame(selectedGame);
                t.setMode(cmbMode.getValue());
                try {
                    t.setMaxParticipants(Integer.parseInt(txtMax.getText()));
                } catch (NumberFormatException ignored) {
                    t.setMaxParticipants(8);
                }
                t.setRules(txtRules.getText());
                t.setPublished(true);
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newTournament -> {
            TournamentDto created = apiClient.createTournament(newTournament);
            if (created != null) {
                loadData();
                CyberAlert.showInfo("Tournament Created", "Tournament '" + created.getName() + "' is now live and synchronized with Cloud Firestore!");
            } else {
                CyberAlert.showError("Creation Failed", "Could not create tournament. Verify backend engine connection.");
            }
        });
    }

    private void showAlert(String msg) {
        CyberAlert.showInfo("Tournament Operations", msg);
    }
}
