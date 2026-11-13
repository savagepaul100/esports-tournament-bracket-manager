package com.esports.desktop;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.ui.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class EsportsDesktopApplication extends Application {
    private ApiClient apiClient;
    private BorderPane rootLayout;
    private StackPane contentHolder;
    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.apiClient = new ApiClient();

        rootLayout = new BorderPane();
        contentHolder = new StackPane();

        // Build Sidebar
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);
        rootLayout.setCenter(contentHolder);

        // Initial View: Dashboard
        switchView("dashboard");

        Scene scene = new Scene(rootLayout, 1320, 820);
        // Load Gamer Theme CSS
        String css = getClass().getResource("/styles/gamer-theme.css") != null
                ? getClass().getResource("/styles/gamer-theme.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("NEXUS ARENA | E-Sports Tournament & Bracket Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");

        // Brand Logo
        HBox brandBox = new HBox(6);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("⚡");
        icon.setStyle("-fx-font-size: 20px; -fx-text-fill: #00f0ff;");
        Label brand = new Label("NEXUS ");
        brand.getStyleClass().add("brand-label");
        Label brandAccent = new Label("ARENA");
        brandAccent.getStyleClass().addAll("brand-label", "brand-accent");
        brandBox.getChildren().addAll(icon, brand, brandAccent);

        VBox navGroup = new VBox(4);
        navGroup.getChildren().addAll(
                createNavButton("📊  Dashboard", "dashboard"),
                createNavButton("🏆  Tournaments", "tournaments"),
                createNavButton("👥  Teams & Check-in", "teams"),
                createNavButton("⚡  Live Bracket", "bracket"),
                createNavButton("🎮  Match Operations", "matches"),
                createNavButton("🛡️  Disputes & Integrity", "disputes"),
                createNavButton("📜  Audit Trail", "audit")
        );
        VBox.setVgrow(navGroup, Priority.ALWAYS);

        // Organizer Profile Box
        VBox profileBox = new VBox(4);
        profileBox.setStyle("-fx-background-color: #131929; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 8;");
        Label userGamerTag = new Label(apiClient.getCurrentUser().getGamerTag());
        userGamerTag.setStyle("-fx-font-weight: bold; -fx-text-fill: #00ff9d; -fx-font-size: 13px;");
        Label userRole = new Label("ROLE: " + apiClient.getCurrentUser().getRole());
        userRole.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        profileBox.getChildren().addAll(userGamerTag, userRole);

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

    public static void main(String[] args) {
        launch(args);
    }
}
