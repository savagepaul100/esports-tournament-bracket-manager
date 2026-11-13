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

import java.util.*;

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
        Label title = new Label("TEAMS & ROSTER MANAGEMENT");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Password-protected team registrations, roster editing, player removal, and check-in tracking.");
        subtitle.getStyleClass().add("view-subtitle");

        HBox guidanceBox = new HBox(10);
        guidanceBox.setAlignment(Pos.CENTER_LEFT);
        guidanceBox.setStyle("-fx-background-color: #18181b; -fx-border-color: #ea580c; -fx-border-width: 1 1 1 3; " +
                "-fx-border-radius: 2; -fx-background-radius: 2; -fx-padding: 8 14; -fx-margin: 0 0 10 0;");
        Label guideIcon = new Label("💡");
        guideIcon.setStyle("-fx-font-size: 15px;");
        VBox guideText = new VBox(2);
        Label guideTitle = new Label("TEAM SECURITY & ROSTER MANAGEMENT GUIDE");
        guideTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");
        Label guideDesc = new Label("Each team requires a security password. Organizers or captains must enter the team password once per reload to edit details.");
        guideDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        guideText.getChildren().addAll(guideTitle, guideDesc);
        guidanceBox.getChildren().addAll(guideIcon, guideText);

        headerBox.getChildren().addAll(title, subtitle, guidanceBox);

        // Filter / Controls bar
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Tournament:");
        lblSelect.setStyle("-fx-font-weight: bold; -fx-text-fill: #a1a1aa;");

        cmbTournament = new ComboBox<>();
        cmbTournament.setPrefWidth(260);
        cmbTournament.setOnAction(e -> loadTeams());

        Button btnRegister = new Button("+ Register Team");
        btnRegister.getStyleClass().add("btn-primary");
        btnRegister.setOnAction(e -> showRegisterDialog());

        Button btnEdit = new Button("✏️ Modify Team");
        btnEdit.getStyleClass().add("btn-cyan");
        btnEdit.setOnAction(e -> showEditTeamDialog());

        Button btnRemovePlayer = new Button("👤 Remove Player");
        btnRemovePlayer.getStyleClass().add("btn-outline");
        btnRemovePlayer.setOnAction(e -> showRemovePlayerDialog());

        Button btnDeleteTeam = new Button("🗑️ Delete Team");
        btnDeleteTeam.getStyleClass().add("btn-danger");
        btnDeleteTeam.setOnAction(e -> deleteSelectedTeam());

        Button btnCheckIn = new Button("Toggle Check-In");
        btnCheckIn.getStyleClass().add("btn-outline");
        btnCheckIn.setOnAction(e -> toggleCheckIn());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> {
            loadTournaments();
            loadTeams();
        });

        topBar.getChildren().addAll(lblSelect, cmbTournament, btnRegister, btnEdit, btnRemovePlayer, btnDeleteTeam, btnCheckIn, btnRefresh);

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
        colName.setPrefWidth(170);

        TableColumn<TeamDto, String> colTag = new TableColumn<>("TAG");
        colTag.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTag()));
        colTag.setPrefWidth(75);

        TableColumn<TeamDto, String> colAuth = new TableColumn<>("SESSION AUTH");
        colAuth.setCellValueFactory(c -> {
            boolean unlocked = apiClient.isTeamUnlockedThisSession(c.getValue().getId());
            return new SimpleStringProperty(unlocked ? "🔓 UNLOCKED" : "🔒 PROTECTED");
        });
        colAuth.setPrefWidth(110);

        TableColumn<TeamDto, String> colCaptain = new TableColumn<>("CAPTAIN EMAIL");
        colCaptain.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCaptainEmail() != null ? c.getValue().getCaptainEmail() : "-"));
        colCaptain.setPrefWidth(170);

        TableColumn<TeamDto, String> colRoster = new TableColumn<>("ROSTER PLAYERS");
        colRoster.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getPlayerNames())));
        colRoster.setPrefWidth(280);

        TableColumn<TeamDto, String> colSeed = new TableColumn<>("SEED");
        colSeed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSeed() != null ? "#" + c.getValue().getSeed() : "-"));
        colSeed.setPrefWidth(65);

        TableColumn<TeamDto, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistrationStatus()));
        colStatus.setPrefWidth(110);

        TableColumn<TeamDto, String> colCheckIn = new TableColumn<>("CHECK-IN");
        colCheckIn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isCheckedIn() ? "READY" : "NO"));
        colCheckIn.setPrefWidth(85);

        tableView.getColumns().addAll(colName, colTag, colAuth, colCaptain, colRoster, colSeed, colStatus, colCheckIn);
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

    /**
     * Prompts for team password if not already unlocked during this session.
     */
    private boolean ensureTeamAuthorized(TeamDto team) {
        if (apiClient.isTeamUnlockedThisSession(team.getId())) {
            return true;
        }

        PasswordField pf = new PasswordField();
        pf.setPromptText("Enter team security password");

        Dialog<String> authDialog = new Dialog<>();
        authDialog.setTitle("TEAM AUTHORIZATION REQUIRED");
        authDialog.setHeaderText("Enter team password for '" + team.getName() + "' (Required once per session):");

        ButtonType btnUnlock = new ButtonType("Unlock Team 🔓", ButtonBar.ButtonData.OK_DONE);
        authDialog.getDialogPane().getButtonTypes().addAll(btnUnlock, ButtonType.CANCEL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.getChildren().addAll(new Label("Password:"), pf);
        authDialog.getDialogPane().setContent(box);
        CyberAlert.style(authDialog);

        authDialog.setResultConverter(b -> (b == btnUnlock) ? pf.getText().trim() : null);

        Optional<String> res = authDialog.showAndWait();
        if (res.isPresent()) {
            boolean valid = apiClient.unlockTeamWithPassword(team.getId(), res.get());
            if (valid) {
                CyberAlert.showInfo("Access Granted", "Team '" + team.getName() + "' is now unlocked for this session.");
                tableView.refresh();
                return true;
            } else {
                CyberAlert.showError("Access Denied", "Incorrect password for team '" + team.getName() + "'.");
                return false;
            }
        }
        return false;
    }

    private void showRegisterDialog() {
        TournamentDto selected = cmbTournament.getValue();
        if (selected == null) {
            CyberAlert.showInfo("Team Registration", "Please select a tournament first.");
            return;
        }

        Dialog<TeamDto> dialog = new Dialog<>();
        dialog.setTitle("REGISTER NEW TEAM");
        dialog.setHeaderText("Create team roster with mandatory strong password protection.");

        ButtonType btnSave = new ButtonType("Register Team", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField();
        txtName.setPromptText("e.g. Cloud9 / Team Secret");

        TextField txtTag = new TextField();
        txtTag.setPromptText("e.g. C9");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("captain@team.gg");

        TextArea txtRoster = new TextArea();
        txtRoster.setPromptText("Comma-separated player gamer tags, e.g. player1, player2, player3");
        txtRoster.setPrefRowCount(3);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("At least 6 characters");

        TextField txtSeed = new TextField("1");

        grid.add(new Label("Team Name:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Tag:"), 0, 1);
        grid.add(txtTag, 1, 1);
        grid.add(new Label("Captain Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Roster (Players):"), 0, 3);
        grid.add(txtRoster, 1, 3);
        grid.add(new Label("Team Password:"), 0, 4);
        grid.add(txtPass, 1, 4);
        grid.add(new Label("Initial Seed:"), 0, 5);
        grid.add(txtSeed, 1, 5);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.setResultConverter(b -> {
            if (b == btnSave) {
                if (txtName.getText().isBlank()) {
                    CyberAlert.showError("Validation Error", "Team name is required.");
                    return null;
                }
                if (txtPass.getText().trim().length() < 4) {
                    CyberAlert.showError("Security Error", "Team password must be at least 4 characters long.");
                    return null;
                }
                TeamDto t = new TeamDto();
                t.setTournamentId(selected.getId());
                t.setName(txtName.getText().trim());
                t.setTag(txtTag.getText().trim());
                t.setCaptainEmail(txtEmail.getText().trim());
                t.setPassword(txtPass.getText().trim());
                t.setCheckedIn(true);
                t.setRegistrationStatus("APPROVED");

                try {
                    t.setSeed(Integer.parseInt(txtSeed.getText().trim()));
                } catch (NumberFormatException ignored) {
                    t.setSeed(1);
                }

                String[] players = txtRoster.getText().split("[,\\n]+");
                List<String> pList = new ArrayList<>();
                for (String p : players) {
                    if (!p.trim().isEmpty()) pList.add(p.trim());
                }
                t.setPlayerNames(pList);
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(team -> {
            boolean ok = apiClient.registerTeam(team);
            if (ok) {
                CyberAlert.showInfo("Team Registered", "Team '" + team.getName() + "' registered to Cloud Firestore and authorized for this session!");
                loadTeams();
            } else {
                CyberAlert.showError("Registration Error", "Failed to register team.");
            }
        });
    }

    private void showEditTeamDialog() {
        TeamDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Modify Team", "Please select a team from the table first.");
            return;
        }

        if (!ensureTeamAuthorized(selected)) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("MODIFY TEAM DETAILS");
        dialog.setHeaderText("Authorized modification for '" + selected.getName() + "'");

        ButtonType btnSave = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(selected.getName());
        TextField txtTag = new TextField(selected.getTag());
        TextField txtEmail = new TextField(selected.getCaptainEmail() != null ? selected.getCaptainEmail() : "");
        TextArea txtRoster = new TextArea(String.join(", ", selected.getPlayerNames()));
        txtRoster.setPrefRowCount(3);
        TextField txtSeed = new TextField(selected.getSeed() != null ? String.valueOf(selected.getSeed()) : "1");

        grid.add(new Label("Team Name:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Tag:"), 0, 1);
        grid.add(txtTag, 1, 1);
        grid.add(new Label("Captain Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Roster (Players):"), 0, 3);
        grid.add(txtRoster, 1, 3);
        grid.add(new Label("Seed:"), 0, 4);
        grid.add(txtSeed, 1, 4);

        dialog.getDialogPane().setContent(grid);
        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(b -> {
            if (b == btnSave) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", txtName.getText().trim());
                updates.put("tag", txtTag.getText().trim());
                updates.put("captainEmail", txtEmail.getText().trim());

                String[] players = txtRoster.getText().split("[,\\n]+");
                List<String> pList = new ArrayList<>();
                for (String p : players) {
                    if (!p.trim().isEmpty()) pList.add(p.trim());
                }
                updates.put("playerNames", pList);

                try {
                    updates.put("seed", Integer.parseInt(txtSeed.getText().trim()));
                } catch (NumberFormatException ignored) {}

                boolean ok = apiClient.updateTeam(selected.getId(), updates);
                if (ok) {
                    CyberAlert.showInfo("Team Updated", "Changes to team '" + selected.getName() + "' saved to Cloud Firestore!");
                    loadTeams();
                } else {
                    CyberAlert.showError("Update Error", "Failed to update team details.");
                }
            }
        });
    }

    private void showRemovePlayerDialog() {
        TeamDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Remove Player", "Please select a team from the table first.");
            return;
        }

        if (selected.getPlayerNames().isEmpty()) {
            CyberAlert.showInfo("Roster Empty", "This team has no registered players.");
            return;
        }

        if (!ensureTeamAuthorized(selected)) {
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getPlayerNames().get(0), selected.getPlayerNames());
        dialog.setTitle("REMOVE PLAYER FROM ROSTER");
        dialog.setHeaderText("Remove player from team '" + selected.getName() + "':");
        dialog.setContentText("Select Player:");

        CyberAlert.style(dialog);

        dialog.showAndWait().ifPresent(player -> {
            boolean confirm = CyberAlert.showConfirm("Confirm Removal",
                    "Remove player '" + player + "' from " + selected.getName() + "?",
                    "The player will be removed from the team's official roster in Cloud Firestore.");
            if (confirm) {
                boolean ok = apiClient.removePlayerFromTeam(selected.getId(), player);
                if (ok) {
                    CyberAlert.showInfo("Player Removed", "Player '" + player + "' removed from roster.");
                    loadTeams();
                } else {
                    CyberAlert.showError("Removal Error", "Failed to remove player.");
                }
            }
        });
    }

    private void deleteSelectedTeam() {
        TeamDto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CyberAlert.showInfo("Delete Team", "Please select a team from the table first.");
            return;
        }

        if (!ensureTeamAuthorized(selected)) {
            return;
        }

        boolean confirm = CyberAlert.showConfirm("Delete Team",
                "Permanently delete team '" + selected.getName() + "'?",
                "This will remove the team and its roster records from Cloud Firestore.");
        if (confirm) {
            boolean ok = apiClient.deleteTeam(selected.getId());
            if (ok) {
                CyberAlert.showInfo("Team Deleted", "Team '" + selected.getName() + "' deleted from Cloud Firestore.");
                loadTeams();
            } else {
                CyberAlert.showError("Delete Error", "Failed to delete team.");
            }
        }
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
}
