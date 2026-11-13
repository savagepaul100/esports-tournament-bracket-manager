package com.esports.desktop.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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
        alert.setContentText("[ERROR] " + message);
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

    public static Optional<String> showPasswordInput(String title, String header, String prompt) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("NEXUS ARENA | " + title);
        dialog.setHeaderText(header);
        style(dialog);

        ButtonType btnOk = new ButtonType("Authorize", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Label lblPrompt = new Label(prompt);
        lblPrompt.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold;");
        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Enter tournament password (default: ADMIN)");
        pwd.setStyle("-fx-background-color: #18181b; -fx-text-fill: #ffffff; -fx-border-color: #3f3f46; -fx-padding: 8;");
        content.getChildren().addAll(lblPrompt, pwd);

        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(b -> (b == btnOk) ? pwd.getText() : null);

        Platform.runLater(pwd::requestFocus);
        return dialog.showAndWait();
    }
}
