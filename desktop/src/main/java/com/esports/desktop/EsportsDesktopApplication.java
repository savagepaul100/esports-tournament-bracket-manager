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
import javafx.stage.Stage;

import java.awt.Desktop;
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

        // Update connection status
        updateConnectionUi(apiClient.isConnected());

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

        primaryStage.setTitle("NEXUS ARENA | E-Sports Tournament & Bracket Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.show();

        if (!apiClient.isConnected()) {
            Platform.runLater(() -> CyberAlert.showError("Cloud Sync Status",
                    "Could not connect to Cloud Firestore.\n" +
                    "Error: " + apiClient.getConnectionError() + "\n\n" +
                    "Running with local high-speed prototype cache."));
        }
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

        // Brand
        HBox brandBox = new HBox(6);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setStyle("-fx-padding: 4 0 12 4;");
        Label icon = new Label("//");
        icon.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #dc2626;");
        Label brand = new Label("NEXUS ");
        brand.getStyleClass().add("brand-label");
        Label brandAccent = new Label("ARENA");
        brandAccent.getStyleClass().addAll("brand-label", "brand-accent");
        brandBox.getChildren().addAll(icon, brand, brandAccent);

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

            if (res.updateAvailable) {
                boolean downloadNow = CyberAlert.showConfirm("Update Available (" + res.latestVersion + ")",
                        res.message + "\n\nRelease Notes: " + res.releaseNotes,
                        "Would you like to open the website to download the latest Windows package now?");
                if (downloadNow) {
                    openUrl("https://nexusarena-esports.web.app");
                }
            } else {
                CyberAlert.showInfo("Nexus Arena Up to Date",
                        "You are currently running the latest version (v" + ApiClient.CURRENT_VERSION + ").\n" +
                        "All cloud synchronization protocols are active.");
            }
        });
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
