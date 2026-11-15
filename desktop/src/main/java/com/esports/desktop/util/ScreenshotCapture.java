package com.esports.desktop.util;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ScreenshotCapture extends Application {
    @Override
    public void start(Stage stage) {
        ApiClient client = new ApiClient();
        String css = getClass().getResource("/styles/gamer-theme.css") != null
                ? getClass().getResource("/styles/gamer-theme.css").toExternalForm() : null;

        new Thread(() -> {
            try {
                Thread.sleep(800);
                captureView(stage, new DashboardView(client, () -> {}), "target/screenshots/screenshot_dashboard.png", css);
                captureView(stage, new TournamentsView(client), "target/screenshots/screenshot_tournaments.png", css);
                captureView(stage, new BracketView(client), "target/screenshots/screenshot_bracket.png", css);
                captureView(stage, new MatchesView(client), "target/screenshots/screenshot_matches.png", css);
                captureView(stage, new TeamsView(client), "target/screenshots/screenshot_teams.png", css);
                captureView(stage, new DisputesView(client), "target/screenshots/screenshot_disputes.png", css);
                captureView(stage, new AuditView(client), "target/screenshots/screenshot_audit.png", css);
                System.out.println("ALL_SCREENSHOTS_CAPTURED_SUCCESSFULLY");
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.exit();
                System.exit(1);
            }
        }).start();
    }

    private void captureView(Stage stage, Node view, String filename, String css) throws Exception {
        final Object lock = new Object();
        Platform.runLater(() -> {
            StackPane root = new StackPane(view);
            Scene scene = new Scene(root, 1280, 800);
            if (css != null) scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.setTitle("Capturing: " + filename);
            stage.show();
            synchronized (lock) {
                lock.notify();
            }
        });

        synchronized (lock) {
            lock.wait(3000);
        }
        Thread.sleep(600);

        final Object saveLock = new Object();
        Platform.runLater(() -> {
            try {
                WritableImage img = stage.getScene().snapshot(null);
                int width = (int) img.getWidth();
                int height = (int) img.getHeight();
                BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bi.setRGB(x, y, img.getPixelReader().getArgb(x, y));
                    }
                }
                File out = new File(filename);
                out.getParentFile().mkdirs();
                ImageIO.write(bi, "png", out);
                System.out.println("Captured: " + filename);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                synchronized (saveLock) {
                    saveLock.notify();
                }
            }
        });

        synchronized (saveLock) {
            saveLock.wait(3000);
        }
        Thread.sleep(400);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
