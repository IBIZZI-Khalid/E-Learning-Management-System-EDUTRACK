package com.example.controllers;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class PdfUser extends Application {
    private Label title;
    private Label subtitle;
    private Label date;
    private String selectedPdfPath; // Variable pour stocker le chemin du PDF sélectionné

    @Override
    public void start(Stage primaryStage) {
        
        // Initialiser les labels
        title = new Label("Title");
        title.setFont(new Font(24));
        title.setTextFill(Color.BLACK);

        subtitle = new Label("Information about the title make\nso why asking about");
        subtitle.setTextFill(Color.GRAY);

        date = new Label("Yesterday");
        date.setTextFill(Color.GRAY);

        VBox centerSection = new VBox(20);
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(20));
        centerSection.setStyle("-fx-background-color: #F4F5F7;");

        Button viewPdfButton = new Button("Voir PDF");
        viewPdfButton.setStyle("-fx-background-color: #1A2238; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        viewPdfButton.setPrefWidth(200);
        viewPdfButton.setDisable(true); // Désactivé par défaut

        // Listener pour le bouton "Voir PDF"
        viewPdfButton.setOnAction(e -> {
            if (selectedPdfPath != null && !selectedPdfPath.isEmpty()) {
                PDFViewer.display(selectedPdfPath); // Ouvre le PDF sélectionné
            } else {
                showAlert("Aucun PDF sélectionné", "Veuillez d'abord sélectionner un cours.");
            }
        });

        // Activer le bouton "Voir PDF" après sélection d’un cours
        title.textProperty().addListener((observable, oldValue, newValue) -> {
            viewPdfButton.setDisable(false);
        });

        centerSection.getChildren().add(viewPdfButton);

        BorderPane root = new BorderPane();
        root.setCenter(centerSection);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Interface");
        primaryStage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
