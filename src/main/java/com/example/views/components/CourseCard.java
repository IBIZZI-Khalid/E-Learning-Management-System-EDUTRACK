package com.example.views.components;
// import com.example.models.Course;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class CourseCard extends VBox {
    private String courseId;
    public CourseCard(String title, String description, double progress, String courseId) {
        setSpacing(5);
        setPadding(new Insets(10));
        getStyleClass().add("course-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);

        ProgressBar progressBar = new ProgressBar(progress/100.0);
        System.out.println("progressbar from coursecard in line 22:"+progressBar);

        progressBar.setPrefWidth(200);

        this.courseId = courseId; 

        getChildren().addAll(titleLabel, descriptionLabel, progressBar);
    }
    
    public String getCourseId(){
        return courseId;
    }

}