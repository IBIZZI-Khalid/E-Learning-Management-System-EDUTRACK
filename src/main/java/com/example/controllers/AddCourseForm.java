package com.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
// import org.bson.Document;

import com.example.services.CourseService;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

// import javax.print.DocFlavor.STRING;

public class AddCourseForm extends Stage {
    public AddCourseForm(MongoDatabase database, CourseService courseService, String teacherEmail) {
        // Create a StackPane instead of BorderPane for better centering
        StackPane mainLayout = new StackPane();
        mainLayout.setStyle("-fx-background-color: #f4f4f9;");

        // Create a VBox for the form content with improved spacing and padding
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40));
        formContainer.setMaxWidth(600); // Limit max width for better readability
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Create scrollpane to handle potential overflow
        ScrollPane scrollPane = new ScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Use StackPane to center the ScrollPane
        StackPane.setAlignment(scrollPane, Pos.CENTER);
        mainLayout.getChildren().add(scrollPane);

        // Module title label
        Label moduleLabel = new Label("Teacher's email : " + teacherEmail);
        moduleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Title input
        Label titleLabel = new Label("Title :");
        TextField titleField = new TextField();
        titleField.setPromptText("Entrez le titre du cours");
        titleField.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10px; -fx-background-radius: 5;");

        // Subtitle input
        Label subtitleLabel = new Label("Description");
        TextField subtitleField = new TextField();
        subtitleField.setPromptText("Entrez le sous-titre du cours");
        subtitleField.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10px; -fx-background-radius: 5;");

        // Date input
        Label dateLabel = new Label("Date :");
        TextField dateField = new TextField();
        dateField.setPromptText("Date du cours (dd/mm/aaaa)");
        dateField.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10px; -fx-background-radius: 5;");

        // PDF file selection
        Label pdfLabel = new Label("PDF file :");
        Label pdfPathLabel = new Label("Aucun fichier choisi");
        pdfPathLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: grey;");

        // PDF selection button
        Button pdfButton = new Button("Choose a PDF");
        pdfButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 5;");
        final String[] pdfPath = { null };
        pdfButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showOpenDialog(this);
            if (file != null) {
                pdfPath[0] = file.getAbsolutePath();
                pdfPathLabel.setText(pdfPath[0]);
                pdfPathLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
            }
        });
        // Add checkbox for "Open Access"
        CheckBox openAccessCheckbox = new CheckBox("Make this course open to all students");
        openAccessCheckbox.setSelected(false); // Default to restricted

        // Action buttons
        Button saveButton = new Button("Add Cours");
        saveButton.setStyle(
                "-fx-background-color: #2ecc71; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 5;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 5;");

        // Button box for horizontal arrangement
        HBox buttonBox = new HBox(20, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add save/action logic
        saveButton.setOnAction(e -> {
            try {
                String title = titleField.getText();
                String subtitle = subtitleField.getText();
                String date = dateField.getText();
                boolean isOpenAccess = openAccessCheckbox.isSelected();

                // Input validation
                if (title.isEmpty() || subtitle.isEmpty() || date.isEmpty() || pdfPath[0] == null) {
                    showAlert("Erreur", "Tous les champs sont obligatoires.");
                    return;
                }

                // Date validation
                if (!isValidDate(date)) {
                    showAlert("Erreur", "Le format de la date est incorrect. Utilisez dd/mm/yyyy.");
                    return;
                }

                // Create the course and display success message
                String courseId = courseService.createCourse(title, subtitle, teacherEmail, new String[]{pdfPath[0]}, isOpenAccess);                showSuccess("Course Created", "Course has been successfully created with ID: " + courseId);
            } catch (RuntimeException ex) {
                showAlert("Error creating course", ex.getMessage());
            }

        });

        cancelButton.setOnAction(e -> this.close());

        // Add all components to the form container
        formContainer.getChildren().addAll(
                moduleLabel,
                titleLabel, titleField,
                subtitleLabel, subtitleField,
                dateLabel, dateField,
                pdfLabel, pdfButton, pdfPathLabel,
                buttonBox,openAccessCheckbox);

        // Create full-screen scene
        Scene scene = new Scene(mainLayout);

        // Maximize the window
        // this.setMaximized(true);

        this.setScene(scene);
        this.setTitle("Ajouter un Cours");
    }

    // Date validation method remains the same
    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setLenient(false);
            Date parsedDate = dateFormat.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Alert method remains the same
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}