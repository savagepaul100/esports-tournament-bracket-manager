package com.esports.desktop;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.TournamentDto;
import com.esports.desktop.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
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

public class EsportsDesktopApplication extends Application {
    private ApiClient apiClient;
    private BorderPane rootLayout;
    private StackPane contentHolder;
    private Label lblConnectionStatus;
    private ComboBox<TournamentDto> cmbActiveTournament;
    private Label lblStatTeams;
    private Label lblStatMatches;
    private Label lblStatPrize;
    private Label lblStatStatus;
    private String currentViewKey = "tournaments";
    private final Map<String, Button> navButtons = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.apiClient = new ApiClient();

        rootLayout = new BorderPane();
        contentHolder = new StackPane();

        // Header combining Top Navigation Bar + Hero Banner (identical to website!)
        VBox headerArea = new VBox();
        HBox topNavbar = buildTopNavbar();
        HBox heroBanner = buildHeroBanner();
        headerArea.getChildren().addAll(topNavbar, heroBanner);
        rootLayout.setTop(headerArea);

        // Center Content Area
        rootLayout.setCenter(contentHolder);

        // Footer Bar
        HBox footerBar = buildFooterBar();
        rootLayout.setBottom(footerBar);

        // Update connection status
        updateConnectionUi(apiClient.isConnected());

        // Refresh tournaments for hero banner
        refreshTournamentsDropdown();

        // Initial View: Tournaments (matching website default tab!)
        switchView("tournaments");

        Scene scene = new Scene(rootLayout, 1360, 860);

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
                    "Ensure internet connection is active."));
        }
    }

    private HBox buildTopNavbar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setMinHeight(48);
        topBar.setMaxHeight(48);

        // Brand Logo
        HBox brandBox = new HBox(6);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("⚡");
        icon.setStyle("-fx-font-size: 18px; -fx-text-fill: #dc2626;");
        Label brand = new Label("NEXUS ");
        brand.getStyleClass().add("brand-label");
        Label brandAccent = new Label("ARENA");
        brandAccent.getStyleClass().addAll("brand-label", "brand-accent");
        brandBox.getChildren().addAll(icon, brand, brandAccent);

        // Live Pill
        Label livePill = new Label("● LIVE");
        livePill.getStyleClass().addAll("badge-pill", "badge-live-fx");

        // Horizontal Navigation Tabs (Identical to Website)
        HBox navGroup = new HBox(4);
        navGroup.setAlignment(Pos.CENTER_LEFT);
        navGroup.getChildren().addAll(
                createTopNavButton("Tournaments", "tournaments"),
                createTopNavButton("Bracket", "bracket"),
                createTopNavButton("Standings", "dashboard"),
                createTopNavButton("Matches", "matches"),
                createTopNavButton("Teams", "teams"),
                createTopNavButton("Disputes", "disputes"),
                createTopNavButton("Audit", "audit")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right Actions
        Button btnSync = new Button("↻ Sync");
        btnSync.getStyleClass().add("btn-outline");
        btnSync.setOnAction(e -> {
            updateConnectionUi(apiClient.isConnected());
            refreshTournamentsDropdown();
            switchView(currentViewKey);
        });

        Button btnPortal = new Button("🌐 Live Web Portal");
        btnPortal.getStyleClass().add("btn-cyan");
        btnPortal.setOnAction(e -> openUrl("https://nexusarena-esports.web.app"));

        lblConnectionStatus = new Label("● FIREBASE ONLINE");
        lblConnectionStatus.getStyleClass().addAll("badge-pill", "badge-green-fx");

        Label userBadge = new Label("⚡ " + apiClient.getCurrentUser().getGamerTag() + " (ORGANIZER)");
        userBadge.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px;");

        topBar.getChildren().addAll(brandBox, livePill, navGroup, spacer, btnSync, btnPortal, lblConnectionStatus, userBadge);
        return topBar;
    }

    private HBox buildHeroBanner() {
        HBox hero = new HBox(16);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("hero-banner-bar");
        hero.setMinHeight(52);
        hero.setMaxHeight(52);

        Label lblPicker = new Label("ACTIVE TOURNAMENT:");
        lblPicker.setStyle("-fx-font-weight: 800; -fx-text-fill: #ea580c; -fx-font-size: 11px; -fx-letter-spacing: 0.5px;");

        cmbActiveTournament = new ComboBox<>();
        cmbActiveTournament.setPrefWidth(320);
        cmbActiveTournament.setOnAction(e -> updateHeroStats(cmbActiveTournament.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatTeams = createStatPill("TEAMS", "8");
        lblStatMatches = createStatPill("MATCHES", "7");
        lblStatPrize = createStatPill("PRIZE", "$50,000");
        lblStatStatus = createStatPill("STATUS", "LIVE");

        hero.getChildren().addAll(lblPicker, cmbActiveTournament, spacer, lblStatTeams, lblStatMatches, lblStatPrize, lblStatStatus);
        return hero;
    }

    private Label createStatPill(String title, String val) {
        Label lbl = new Label(title + ": " + val);
        lbl.getStyleClass().add("hero-stat-pill");
        lbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        return lbl;
    }

    private void updateHeroStats(TournamentDto t) {
        if (t == null) return;
        Platform.runLater(() -> {
            lblStatTeams.setText("MAX TEAMS: " + t.getMaxParticipants());
            lblStatMatches.setText("MODE: " + (t.getMode() != null ? t.getMode() : "BRACKET"));
            lblStatPrize.setText("GAME: " + (t.getGame() != null ? t.getGame() : "VALORANT"));
            lblStatStatus.setText("STATUS: " + (t.getStatus() != null ? t.getStatus() : "LIVE"));
        });
    }

    private void refreshTournamentsDropdown() {
        new Thread(() -> {
            List<TournamentDto> list = apiClient.getTournaments();
            Platform.runLater(() -> {
                cmbActiveTournament.getItems().setAll(list);
                if (!list.isEmpty() && cmbActiveTournament.getValue() == null) {
                    cmbActiveTournament.setValue(list.get(0));
                    updateHeroStats(list.get(0));
                }
            });
        }).start();
    }

    private HBox buildFooterBar() {
        HBox footer = new HBox(14);
        footer.getStyleClass().add("footer-bar");
        footer.setMinHeight(28);
        footer.setMaxHeight(28);

        Label lblEngine = new Label("⚡ NEXUS ARENA ESPORTS ENGINE");
        lblEngine.setStyle("-fx-font-weight: 800; -fx-text-fill: #71717a; -fx-font-size: 10.5px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblDetails = new Label("100% Cloud-Synchronized (Firestore: nexusarena-esports) • Pure Java 21 Client");
        lblDetails.setStyle("-fx-text-fill: #52525b; -fx-font-size: 10px;");

        footer.getChildren().addAll(lblEngine, spacer, lblDetails);
        return footer;
    }

    private Button createTopNavButton(String text, String viewKey) {
        Button btn = new Button(text);
        btn.getStyleClass().add("top-nav-btn");
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

    private void updateConnectionUi(boolean isConnected) {
        Platform.runLater(() -> {
            if (lblConnectionStatus == null) return;
            if (isConnected) {
                lblConnectionStatus.setText("● FIREBASE ONLINE ✓");
                lblConnectionStatus.getStyleClass().setAll("badge-pill", "badge-green-fx");
            } else {
                lblConnectionStatus.setText("⚠️ FIREBASE OFFLINE");
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
