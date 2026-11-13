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
        headerBox.getChildren().addAll(title, subtitle);

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

        Button btnExport = new Button("📥 Export CSV");
        btnExport.getStyleClass().add("btn-cyan");
        btnExport.setOnAction(e -> exportCsv());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> loadData());

        actionsBar.getChildren().addAll(btnCreate, btnRegOpen, btnCheckIn, btnInProgress, btnComplete, btnExport, btnRefresh);

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

    private void exportCsv() {
        TournamentDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a tournament from the table first.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080/api/tournaments/" + selected.getId() + "/export/csv"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        TextField txtGame = new TextField();
        txtGame.setPromptText("e.g. Valorant / Free Fire / BGMI");

        ComboBox<String> cmbMode = new ComboBox<>();
        cmbMode.getItems().addAll("SINGLE_ELIMINATION_TDM", "SINGLE_ELIMINATION_CLASH", "BATTLE_ROYALE");
        cmbMode.setValue("SINGLE_ELIMINATION_TDM");

        TextField txtMax = new TextField("8");
        TextArea txtRules = new TextArea("Standard fair play rules. Anti-cheat screen recording mandatory.");
        txtRules.setPrefRowCount(3);

        grid.add(new Label("Tournament Name:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Game:"), 0, 1);
        grid.add(txtGame, 1, 1);
        grid.add(new Label("Format / Mode:"), 0, 2);
        grid.add(cmbMode, 1, 2);
        grid.add(new Label("Max Teams:"), 0, 3);
        grid.add(txtMax, 1, 3);
        grid.add(new Label("Rules & Notes:"), 0, 4);
        grid.add(txtRules, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCreateType) {
                TournamentDto t = new TournamentDto();
                t.setName(txtName.getText().isBlank() ? "Untitled Tournament" : txtName.getText());
                t.setGame(txtGame.getText().isBlank() ? "Competitive Esports" : txtGame.getText());
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
            } else {
                showAlert("Failed to create tournament.");
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nexus Arena Organizer");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
