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
import java.io.File;
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

        // Check & auto-start backend if needed
        ensureBackendRunning();

        // Build Top Bar
        HBox topBar = buildTopBar();
        rootLayout.setTop(topBar);

        // Build Sidebar
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);
        rootLayout.setCenter(contentHolder);

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
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-header-bar");

        Label appBadge = new Label("ORGANIZER COMMAND SUITE");
        appBadge.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        lblConnectionStatus = new Label("● CHECKING ENGINE...");
        lblConnectionStatus.getStyleClass().addAll("badge-pill", "badge-amber-fx");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCloudPortal = new Button("🌐 Live Cloud Portal (nexusarena-esports.web.app)");
        btnCloudPortal.getStyleClass().add("btn-cyan");
        btnCloudPortal.setOnAction(e -> openUrl("https://nexusarena-esports.web.app"));

        Button btnLocalPortal = new Button("💻 Local Web");
        btnLocalPortal.getStyleClass().add("btn-outline");
        btnLocalPortal.setOnAction(e -> openUrl("http://localhost:8080"));

        Button btnSync = new Button("↻ Sync Data");
        btnSync.getStyleClass().add("btn-outline");
        btnSync.setOnAction(e -> {
            checkConnectionStatus();
            switchView(currentViewKey);
        });

        topBar.getChildren().addAll(appBadge, lblConnectionStatus, spacer, btnCloudPortal, btnLocalPortal, btnSync);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        // Brand Logo
        HBox brandBox = new HBox(8);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setStyle("-fx-padding: 0 0 16 6;");
        Label icon = new Label("⚡");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: #00f0ff;");
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
        profileBox.setStyle("-fx-background-color: #0e1424; -fx-padding: 14; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10;");
        
        Label userGamerTag = new Label("⚡ " + apiClient.getCurrentUser().getGamerTag());
        userGamerTag.setStyle("-fx-font-weight: 800; -fx-text-fill: #00ff9d; -fx-font-size: 13px;");
        
        Label userRole = new Label("ROLE: ORGANIZER / ADMIN");
        userRole.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        Label cloudStatus = new Label("🔥 Cloud: Google Firestore");
        cloudStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #00f0ff;");

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
        // Highlight active nav button
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

    private void ensureBackendRunning() {
        Thread thread = new Thread(() -> {
            if (apiClient.isBackendAlive()) {
                updateConnectionUi(true);
                return;
            }

            // Attempt to launch backend jar
            try {
                File jar = findBackendJar();
                if (jar != null && jar.exists()) {
                    Platform.runLater(() -> {
                        if (lblConnectionStatus != null) {
                            lblConnectionStatus.setText("⏳ STARTING BACKEND ENGINE...");
                            lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-amber-fx");
                        }
                    });

                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", jar.getAbsolutePath());
                    pb.directory(jar.getParentFile());
                    pb.start();

                    // Poll for up to 15 seconds
                    for (int i = 0; i < 15; i++) {
                        Thread.sleep(1000);
                        if (apiClient.isBackendAlive()) {
                            updateConnectionUi(true);
                            Platform.runLater(() -> switchView(currentViewKey));
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            updateConnectionUi(apiClient.isBackendAlive());
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void checkConnectionStatus() {
        new Thread(() -> {
            boolean alive = apiClient.isBackendAlive();
            updateConnectionUi(alive);
        }).start();
    }

    private void updateConnectionUi(boolean isAlive) {
        Platform.runLater(() -> {
            if (lblConnectionStatus == null) return;
            if (isAlive) {
                lblConnectionStatus.setText("● ENGINE & FIRESTORE ONLINE (Port 8080)");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-green-fx");
            } else {
                lblConnectionStatus.setText("⚠️ BACKEND DISCONNECTED (Click Sync to Retry)");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-live-fx");
            }
        });
    }

    private File findBackendJar() {
        String[] possiblePaths = {
                "backend-1.0.0.jar",
                "backend/target/backend-1.0.0.jar",
                "../backend/target/backend-1.0.0.jar",
                "target/backend-1.0.0.jar"
        };
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) return f;
        }
        return null;
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
