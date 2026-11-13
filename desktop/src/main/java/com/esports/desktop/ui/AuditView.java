package com.esports.desktop.ui;

import com.esports.desktop.client.ApiClient;
import com.esports.desktop.model.AuditLogDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class AuditView extends VBox {
    private final ApiClient apiClient;
    private final TableView<AuditLogDto> tableView;

    public AuditView(ApiClient apiClient) {
        this.apiClient = apiClient;
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("SECURITY AUDIT TRAIL & SYSTEM LOGS");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Immutable audit records capturing every tournament modification, score validation, and moderation ruling.");
        subtitle.getStyleClass().add("view-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Control bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnRefresh = new Button("↻ Refresh Logs");
        btnRefresh.getStyleClass().add("btn-outline");
        btnRefresh.setOnAction(e -> loadLogs());

        topBar.getChildren().add(btnRefresh);

        // Table
        tableView = new TableView<>();
        setupTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getChildren().addAll(headerBox, topBar, tableView);
        loadLogs();
    }

    private void setupTable() {
        TableColumn<AuditLogDto, String> colTime = new TableColumn<>("TIMESTAMP");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimestamp()));
        colTime.setPrefWidth(200);

        TableColumn<AuditLogDto, String> colActor = new TableColumn<>("OPERATOR / ACTOR");
        colActor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActor()));
        colActor.setPrefWidth(140);

        TableColumn<AuditLogDto, String> colAction = new TableColumn<>("ACTION");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        colAction.setPrefWidth(180);

        TableColumn<AuditLogDto, String> colType = new TableColumn<>("ENTITY TYPE");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEntityType()));
        colType.setPrefWidth(120);

        TableColumn<AuditLogDto, String> colId = new TableColumn<>("ENTITY ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEntityId()));
        colId.setPrefWidth(140);

        TableColumn<AuditLogDto, String> colReason = new TableColumn<>("DETAILS & AUDIT REASON");
        colReason.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReason()));
        colReason.setPrefWidth(320);

        tableView.getColumns().addAll(colTime, colActor, colAction, colType, colId, colReason);
    }

    private void loadLogs() {
        List<AuditLogDto> list = apiClient.getAuditLogs();
        tableView.setItems(FXCollections.observableArrayList(list));
    }
}
