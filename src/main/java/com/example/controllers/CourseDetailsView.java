package com.example.controllers;

import com.example.models.Course;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CourseDetailsView extends VBox {

    private final Label titleLabel;
    private final Label descriptionLabel;
    private final Label accessLabel;
    private final ProgressBar progressBar;
    private final Button openPdfButton;

    public CourseDetailsView() {
        // Apply the custom style class
        getStyleClass().add("course-details-container");
        // Load the CSS file
        String cssPath = getClass().getResource("/CSS/coursedetails.css").toExternalForm();
        getStylesheets().add(cssPath);
        
        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.TOP_LEFT);

        // Title
        titleLabel = new Label("Title");
        titleLabel.getStyleClass().add("course-details-title");

        // Description
        descriptionLabel = new Label("Description");
        descriptionLabel.getStyleClass().add("course-details-description");
        descriptionLabel.setWrapText(true);

        // Access Label (Open/Restricted)
        accessLabel = new Label("Access: Unknown");
        accessLabel.getStyleClass().add("course-details-access-label");

        // Progress Bar
        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("course-details-progress-bar");
        progressBar.setPrefWidth(200);

        // PDF Open Button
        openPdfButton = new Button("Open Course PDF");
        openPdfButton.getStyleClass().add("course-details-pdf-button");
        
        // Action section for button
        HBox actionSection = new HBox(openPdfButton);
        actionSection.getStyleClass().add("course-details-action-section");

        // Add components to the VBox
        getChildren().addAll(titleLabel, descriptionLabel, accessLabel, progressBar, actionSection);

        // Placeholder for PDF open action (to be implemented)
        openPdfButton.setOnAction(event -> {
            // TODO: Implement PDF opening logic
            System.out.println("Attempting to open course PDF");
        });
    }

    public void updateCourseDetails(Course course) {
        titleLabel.setText(course.getTitle());
        descriptionLabel.setText(course.getDescription());
        accessLabel.setText("Access: " + (course.isOpenAccess() ? "Open" : "Restricted"));
        progressBar.setProgress(course.getProgressPercentage() / 100);
    }
}