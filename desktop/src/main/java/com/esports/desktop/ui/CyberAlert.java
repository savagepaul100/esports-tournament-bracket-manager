package com.esports.desktop.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * CyberAlert utility to ensure all JavaFX dialogs, alerts, and confirmations
 * match the dark cyberpunk gamer theme of the website.
 */
public class CyberAlert {

    private static final String CSS_PATH = "/styles/gamer-theme.css";

    public static void style(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("cyber-dialog");
        try {
            String css = CyberAlert.class.getResource(CSS_PATH) != null
                    ? CyberAlert.class.getResource(CSS_PATH).toExternalForm()
                    : null;
            if (css != null && !pane.getStylesheets().contains(css)) {
                pane.getStylesheets().add(css);
            }
        } catch (Exception ignored) {}
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("NEXUS ARENA | " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        style(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NEXUS ARENA | " + title);
        alert.setHeaderText(null);
        alert.setContentText("⚠️ " + message);
        style(alert);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("NEXUS ARENA | " + title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        style(confirm);
        Optional<ButtonType> res = confirm.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
}
