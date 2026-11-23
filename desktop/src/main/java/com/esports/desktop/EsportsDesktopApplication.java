package com.esports.desktop;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.TournamentDto;
import com.esports.desktop.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nexus Arena Desktop Application.
 * Features an industrial side-panel navigation layout matching the web portal,
 * non-blocking asynchronous cloud data syncing, and an integrated in-app updater.
 */
public class EsportsDesktopApplication extends Application {
    private ApiClient apiClient;
    private BorderPane rootLayout;
    private StackPane contentHolder;
    private Label lblConnectionStatus;
    private ComboBox<TournamentDto> cmbActiveTournament;
    private String currentViewKey = "tournaments";
    private final Map<String, Button> navButtons = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.apiClient = new ApiClient();

        rootLayout = new BorderPane();
        contentHolder = new StackPane();

        // 1. Minimal Top Header Bar (Only essential actions)
        HBox topBar = buildTopBar();
        rootLayout.setTop(topBar);

        // 2. Clean Left Side Panel (Full navigation & update checker)
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);

        // 3. Center Content Area
        rootLayout.setCenter(contentHolder);

        // 4. Minimal Footer Bar
        HBox footer = buildFooter();
        rootLayout.setBottom(footer);

        // Refresh tournaments dropdown for active picker
        refreshTournamentsDropdown();

        // Default initial view: Tournaments
        switchView("tournaments");

        Scene scene = new Scene(rootLayout, 1340, 840);

        // Load Gamer Theme CSS
        String css = getClass().getResource("/styles/gamer-theme.css") != null
                ? getClass().getResource("/styles/gamer-theme.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        // Set Application Icons (Multiple resolutions for crisp titlebar, taskbar, Alt-Tab)
        try {
            int[] iconSizes = {256, 128, 64, 32, 16};
            for (int s : iconSizes) {
                InputStream iconStream = getClass().getResourceAsStream("/icons/app-logo-" + s + ".png");
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                }
            }
        } catch (Exception ex) {
            System.err.println("Notice: Could not load stage icons: " + ex.getMessage());
        }

        primaryStage.setTitle("NEXUS ARENA | E-Sports Tournament & Bracket Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.show();

        // Register reactive connection listener for 100% smooth cloud sync without false error popups
        apiClient.addConnectionListener(state -> {
            Platform.runLater(() -> {
                if (state == ApiClient.ConnectionState.ONLINE) {
                    updateConnectionUi(true);
                    refreshTournamentsDropdown();
                    switchView(currentViewKey);
                } else if (state == ApiClient.ConnectionState.CONNECTING) {
                    if (lblConnectionStatus != null) {
                        lblConnectionStatus.setText("SYNCING CLOUD...");
                        lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-live-fx");
                    }
                } else {
                    updateConnectionUi(false);
                }
            });
        });
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setMinHeight(48);
        topBar.setMaxHeight(48);

        Label lblPicker = new Label("ACTIVE EVENT:");
        lblPicker.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");

        cmbActiveTournament = new ComboBox<>();
        cmbActiveTournament.setPrefWidth(280);
        cmbActiveTournament.setPromptText("Select active tournament...");
        cmbActiveTournament.setOnAction(e -> {
            TournamentDto selected = cmbActiveTournament.getValue();
            if (selected != null) {
                switchView(currentViewKey);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSync = new Button("Sync Data");
        btnSync.getStyleClass().add("btn-outline");
        btnSync.setOnAction(e -> {
            btnSync.setDisable(true);
            btnSync.setText("Syncing...");
            apiClient.refreshAllAsync(() -> {
                btnSync.setDisable(false);
                btnSync.setText("Sync Data");
                updateConnectionUi(apiClient.isConnected());
                refreshTournamentsDropdown();
                switchView(currentViewKey);
                CyberAlert.showInfo("Cloud Sync", "All tournament data, live rounds, and rosters synchronized with Cloud Firestore.");
            });
        });

        Button btnPortal = new Button("Live Web Portal");
        btnPortal.getStyleClass().add("btn-cyan");
        btnPortal.setOnAction(e -> openUrl("https://nexusarena-esports.web.app"));

        lblConnectionStatus = new Label("CLOUD ONLINE");
        lblConnectionStatus.getStyleClass().addAll("badge-pill", "badge-green-fx");

        Label userBadge = new Label(apiClient.getCurrentUser().getGamerTag() + " (ORGANIZER)");
        userBadge.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");

        topBar.getChildren().addAll(lblPicker, cmbActiveTournament, spacer, btnSync, btnPortal, lblConnectionStatus, userBadge);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(220);
        sidebar.getStyleClass().add("sidebar");

        // Brand with Official Professional Logo
        HBox brandBox = new HBox(8);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setStyle("-fx-padding: 4 0 12 4;");
        try {
            InputStream logoStream = getClass().getResourceAsStream("/icons/app-logo-64.png");
            if (logoStream != null) {
                ImageView logoView = new ImageView(new Image(logoStream));
                logoView.setFitWidth(30);
                logoView.setFitHeight(30);
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
                brandBox.getChildren().add(logoView);
            }
        } catch (Exception ignored) {}

        Label brand = new Label("NEXUS ");
        brand.getStyleClass().add("brand-label");
        Label brandAccent = new Label("ARENA");
        brandAccent.getStyleClass().addAll("brand-label", "brand-accent");
        brandBox.getChildren().addAll(brand, brandAccent);

        // Navigation Group (Vertical side panel)
        VBox navGroup = new VBox(4);
        navGroup.getChildren().addAll(
                createNavButton("Tournaments", "tournaments"),
                createNavButton("Bracket Tree", "bracket"),
                createNavButton("Standings", "dashboard"),
                createNavButton("Live Matches", "matches"),
                createNavButton("Team Rosters", "teams"),
                createNavButton("Disputes", "disputes"),
                createNavButton("Audit Trail", "audit")
        );
        VBox.setVgrow(navGroup, Priority.ALWAYS);

        // Sidebar Footer: Version and In-App Update Checker
        VBox updateBox = new VBox(8);
        updateBox.setStyle("-fx-background-color: #18181b; -fx-padding: 12; -fx-background-radius: 2; " +
                "-fx-border-color: #27272a; -fx-border-radius: 2; -fx-border-width: 1;");

        Label lblVer = new Label("Nexus Arena v" + ApiClient.CURRENT_VERSION);
        lblVer.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");

        Label lblCloudSync = new Label("Firestore Synced");
        lblCloudSync.setStyle("-fx-font-size: 10px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        Button btnUpdate = new Button("Check for Updates");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.getStyleClass().add("btn-outline");
        btnUpdate.setStyle("-fx-font-size: 10.5px; -fx-padding: 5 8;");
        btnUpdate.setOnAction(e -> handleCheckUpdates(btnUpdate));

        updateBox.getChildren().addAll(lblVer, lblCloudSync, btnUpdate);

        sidebar.getChildren().addAll(brandBox, navGroup, updateBox);
        return sidebar;
    }

    private void handleCheckUpdates(Button btnUpdate) {
        btnUpdate.setDisable(true);
        btnUpdate.setText("Checking cloud...");
        apiClient.checkForUpdatesAsync(res -> {
            btnUpdate.setDisable(false);
            btnUpdate.setText("Check for Updates");
            showUpdateDialog(res);
        });
    }

    private void showUpdateDialog(ApiClient.UpdateCheckResult res) {
        Stage updateStage = new Stage();
        updateStage.initModality(Modality.APPLICATION_MODAL);
        updateStage.setTitle("NEXUS ARENA | Automated Cloud Updater");

        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/app-logo-64.png");
            if (iconStream != null) updateStage.getIcons().add(new Image(iconStream));
        } catch (Exception ignored) {}

        VBox root = new VBox(14);
        root.setStyle("-fx-background-color: #0d0e15; -fx-padding: 24; -fx-border-color: #dc2626; -fx-border-width: 1.5;");
        root.setPrefWidth(540);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/app-logo-64.png");
            if (iconStream != null) {
                ImageView logo = new ImageView(new Image(iconStream));
                logo.setFitWidth(38);
                logo.setFitHeight(38);
                logo.setPreserveRatio(true);
                header.getChildren().add(logo);
            }
        } catch (Exception ignored) {}

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("NEXUS ARENA AUTO-UPDATER");
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");
        Label lblSub = new Label("Direct In-App Automatic Cloud Installation");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #ea580c; -fx-font-weight: bold;");
        titleBox.getChildren().addAll(lblTitle, lblSub);
        header.getChildren().add(titleBox);

        // Version info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(6);
        infoGrid.setStyle("-fx-background-color: #181926; -fx-padding: 12; -fx-background-radius: 4; -fx-border-color: #27273a; -fx-border-width: 1;");

        Label lblCurKey = new Label("Current Version:");
        lblCurKey.setStyle("-fx-font-weight: bold; -fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        Label lblCurVal = new Label("v" + ApiClient.CURRENT_VERSION);
        lblCurVal.setStyle("-fx-font-weight: 900; -fx-text-fill: #ffffff; -fx-font-size: 11px;");

        Label lblLatKey = new Label("Cloud Latest:");
        lblLatKey.setStyle("-fx-font-weight: bold; -fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        Label lblLatVal = new Label("v" + res.latestVersion);
        lblLatVal.setStyle("-fx-font-weight: 900; -fx-text-fill: #22c55e; -fx-font-size: 11px;");

        Label lblNotesKey = new Label("Release Notes:");
        lblNotesKey.setStyle("-fx-font-weight: bold; -fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        Label lblNotesVal = new Label(res.releaseNotes != null ? res.releaseNotes : "Security and performance improvements.");
        lblNotesVal.setWrapText(true);
        lblNotesVal.setMaxWidth(380);
        lblNotesVal.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 10.5px;");

        infoGrid.add(lblCurKey, 0, 0);
        infoGrid.add(lblCurVal, 1, 0);
        infoGrid.add(lblLatKey, 0, 1);
        infoGrid.add(lblLatVal, 1, 1);
        infoGrid.add(lblNotesKey, 0, 2);
        infoGrid.add(lblNotesVal, 1, 2);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #dc2626;");
        progressBar.setVisible(false);

        Label lblStatus = new Label(res.updateAvailable
                ? "New build ready for 1-click automatic download and install."
                : "Your desktop client is running the latest version.");
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #f3f4f6; -fx-font-weight: 600;");

        // Action Buttons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAction = new Button(res.updateAvailable ? "Download & Install Automatically" : "Force Reinstall / Update");
        btnAction.getStyleClass().add("btn-primary");
        btnAction.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().add("btn-outline");
        btnClose.setOnAction(e -> updateStage.close());

        btnAction.setOnAction(e -> {
            btnAction.setDisable(true);
            btnClose.setDisable(true);
            progressBar.setVisible(true);
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            String targetUrl = res.jarUrl != null ? res.jarUrl : "https://nexusarena-esports.web.app/download/NexusArena.jar";

            apiClient.downloadAndInstallUpdateAsync(
                    targetUrl,
                    progress -> progressBar.setProgress(progress),
                    status -> lblStatus.setText(status),
                    error -> {
                        progressBar.setVisible(false);
                        btnAction.setDisable(false);
                        btnClose.setDisable(false);
                        lblStatus.setText("Update Notice: " + error);
                        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
            );
        });

        btnBox.getChildren().addAll(btnClose, btnAction);
        root.getChildren().addAll(header, infoGrid, lblStatus, progressBar, btnBox);

        Scene scene = new Scene(root);
        String css = getClass().getResource("/styles/gamer-theme.css") != null
                ? getClass().getResource("/styles/gamer-theme.css").toExternalForm() : null;
        if (css != null) scene.getStylesheets().add(css);

        updateStage.setScene(scene);
        updateStage.setResizable(false);
        updateStage.show();
    }

    private Button createNavButton(String text, String viewKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> switchView(viewKey));
        navButtons.put(viewKey, btn);
        return btn;
    }

    private void switchView(String viewKey) {
        this.currentViewKey = viewKey;
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewKey)) {
                if (!entry.getValue().getStyleClass().contains("active")) {
                    entry.getValue().getStyleClass().add("active");
                }
            } else {
                entry.getValue().getStyleClass().remove("active");
            }
        }

        Node view = switch (viewKey) {
            case "dashboard" -> new DashboardView(apiClient, () -> switchView("tournaments"));
            case "tournaments" -> new TournamentsView(apiClient);
            case "teams" -> new TeamsView(apiClient);
            case "bracket" -> new BracketView(apiClient);
            case "matches" -> new MatchesView(apiClient);
            case "disputes" -> new DisputesView(apiClient);
            case "audit" -> new AuditView(apiClient);
            default -> new Label("Unknown View");
        };

        contentHolder.getChildren().clear();
        contentHolder.getChildren().add(view);
    }

    private void refreshTournamentsDropdown() {
        apiClient.refreshAllAsync(() -> {
            List<TournamentDto> list = apiClient.getTournaments();
            cmbActiveTournament.getItems().setAll(list);
            if (!list.isEmpty() && cmbActiveTournament.getValue() == null) {
                cmbActiveTournament.setValue(list.get(0));
            }
        });
    }

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.getStyleClass().add("footer-bar");
        footer.setMinHeight(26);
        footer.setMaxHeight(26);

        Label lblEngine = new Label("NEXUS ARENA ESPORTS ENGINE");
        lblEngine.setStyle("-fx-font-weight: 800; -fx-text-fill: #71717a; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblDetails = new Label("100% Cloud-Synchronized (Google Firestore: nexusarena-esports) - Java 21");
        lblDetails.setStyle("-fx-text-fill: #52525b; -fx-font-size: 9.5px;");

        footer.getChildren().addAll(lblEngine, spacer, lblDetails);
        return footer;
    }

    private void updateConnectionUi(boolean isConnected) {
        Platform.runLater(() -> {
            if (lblConnectionStatus == null) return;
            if (isConnected) {
                lblConnectionStatus.setText("CLOUD ONLINE");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-green-fx");
            } else {
                lblConnectionStatus.setText("CACHE MODE");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-live-fx");
            }
        });
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            CyberAlert.showInfo("Open Browser", "Please navigate to: " + url);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
