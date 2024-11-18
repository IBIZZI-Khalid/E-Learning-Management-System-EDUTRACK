package com.example.views.components;
import com.example.models.Course;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class CourseCard extends VBox {
    public CourseCard(String title, String description, double progress) {
        setSpacing(5);
        setPadding(new Insets(10));
        getStyleClass().add("course-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefWidth(200);

        getChildren().addAll(titleLabel, descriptionLabel, progressBar);
    }
}