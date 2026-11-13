package com.esports.desktop;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class EsportsDesktopApplication extends Application {
    private ApiClient apiClient;
    private BorderPane rootLayout;
    private StackPane contentHolder;
    private Label lblConnectionStatus;
    private String currentViewKey = "dashboard";
    private final Map<String, Button> navButtons = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.apiClient = new ApiClient();

        rootLayout = new BorderPane();
        contentHolder = new StackPane();

        // Build Top Bar
        HBox topBar = buildTopBar();
        rootLayout.setTop(topBar);

        // Build Sidebar
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);
        rootLayout.setCenter(contentHolder);

        // Update connection status
        updateConnectionUi(apiClient.isConnected());

        // Initial View: Dashboard
        switchView("dashboard");

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
        primaryStage.setMinWidth(1120);
        primaryStage.setMinHeight(720);
        primaryStage.show();

        if (!apiClient.isConnected()) {
            Platform.runLater(() -> CyberAlert.showError("Firebase Connection Failed",
                    "Could not connect to Cloud Firestore.\n" +
                    "Error: " + apiClient.getConnectionError() + "\n\n" +
                    "Ensure firebase-service-account.json is bundled correctly."));
        }
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setMinHeight(48);
        topBar.setMaxHeight(48);

        Label appBadge = new Label("ORGANIZER COMMAND SUITE");
        appBadge.setStyle("-fx-font-weight: 800; -fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        lblConnectionStatus = new Label("● CONNECTING...");
        lblConnectionStatus.getStyleClass().addAll("badge-pill", "badge-amber-fx");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCloudPortal = new Button("🌐 Live Portal (nexusarena-esports.web.app)");
        btnCloudPortal.getStyleClass().add("btn-cyan");
        btnCloudPortal.setOnAction(e -> openUrl("https://nexusarena-esports.web.app"));

        Button btnSync = new Button("↻ Sync Data");
        btnSync.getStyleClass().add("btn-outline");
        btnSync.setOnAction(e -> {
            updateConnectionUi(apiClient.isConnected());
            switchView(currentViewKey);
        });

        topBar.getChildren().addAll(appBadge, lblConnectionStatus, spacer, btnCloudPortal, btnSync);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");

        // Brand Logo
        HBox brandBox = new HBox(8);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setStyle("-fx-padding: 0 0 16 6;");
        Label icon = new Label("⚡");
        icon.setStyle("-fx-font-size: 22px; -fx-text-fill: #dc2626;");
        Label brand = new Label("NEXUS ");
        brand.getStyleClass().add("brand-label");
        Label brandAccent = new Label("ARENA");
        brandAccent.getStyleClass().addAll("brand-label", "brand-accent");
        brandBox.getChildren().addAll(icon, brand, brandAccent);

        VBox navGroup = new VBox(4);
        navGroup.getChildren().addAll(
                createNavButton("📊  Command Dashboard", "dashboard"),
                createNavButton("🏆  Tournament Setup", "tournaments"),
                createNavButton("👥  Teams & Rosters", "teams"),
                createNavButton("⚡  Interactive Bracket", "bracket"),
                createNavButton("🎮  Match Operations", "matches"),
                createNavButton("🛡️  Disputes & Integrity", "disputes"),
                createNavButton("📜  Audit Log Trail", "audit")
        );
        VBox.setVgrow(navGroup, Priority.ALWAYS);

        // Organizer Profile Box
        VBox profileBox = new VBox(6);
        profileBox.setStyle("-fx-background-color: #18181b; -fx-padding: 12; -fx-background-radius: 2; " +
                "-fx-border-color: #27272a; -fx-border-radius: 2; -fx-border-width: 1;");

        Label userGamerTag = new Label("⚡ " + apiClient.getCurrentUser().getGamerTag());
        userGamerTag.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 13px;");

        Label userRole = new Label("ROLE: " + apiClient.getCurrentUser().getRole());
        userRole.setStyle("-fx-font-size: 10px; -fx-text-fill: #a1a1aa; -fx-font-weight: bold;");

        Label cloudStatus = new Label("🔥 Cloud Firestore Online");
        cloudStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        profileBox.getChildren().addAll(userGamerTag, userRole, cloudStatus);

        sidebar.getChildren().addAll(brandBox, navGroup, profileBox);
        return sidebar;
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
                entry.getValue().getStyleClass().add("active");
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

    private void updateConnectionUi(boolean isConnected) {
        Platform.runLater(() -> {
            if (lblConnectionStatus == null) return;
            if (isConnected) {
                lblConnectionStatus.setText("● FIREBASE CONNECTED ✓");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-green-fx");
            } else {
                lblConnectionStatus.setText("⚠️ FIREBASE DISCONNECTED");
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
