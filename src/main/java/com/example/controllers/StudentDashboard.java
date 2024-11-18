package com.example.controllers;

import java.util.List;

import com.example.MainApp;
import com.example.models.Course;
import com.example.services.CourseService;
import com.example.views.components.CourseCard;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StudentDashboard {

    private MainApp mainApp;

    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private CourseService courseService;

    public StudentDashboard(MainApp mainApp) {
        this.mainApp = mainApp;
        this.courseService = new CourseService();
        createView();
    }

    private void createView() {
        view = new BorderPane();
        sidebar = createSidebar();
        contentArea = new StackPane();

        view.setLeft(sidebar);
        view.setCenter(contentArea);

        applyStyling();
        showCoursesView();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);

        Button coursesBtn = new Button("My Courses");
        Button progressBtn = new Button("My Progress");
        Button chatsBtn = new Button("Chats");

        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        progressBtn.setMaxWidth(Double.MAX_VALUE);
        chatsBtn.setMaxWidth(Double.MAX_VALUE);

        coursesBtn.setOnAction(e -> showCoursesView());
        progressBtn.setOnAction(e -> showProgressView());
        chatsBtn.setOnAction(e -> showChatsView());

        sidebar.getChildren().addAll(
                createUserProfile(),
                new Separator(),
                coursesBtn,
                progressBtn,
                chatsBtn);

        return sidebar;
    }

    private VBox createUserProfile() {
        VBox profile = new VBox(5);
        Label nameLabel = new Label("[Student Name]");
        Label emailLabel = new Label("[student@example.com]");

        nameLabel.getStyleClass().add("profile-name");
        emailLabel.getStyleClass().add("profile-email");

        profile.getChildren().addAll(nameLabel, emailLabel);
        return profile;
    }

    private void showCoursesView() {
        VBox coursesView = new VBox(10);
        coursesView.setPadding(new Insets(20));

        List<Course> courses = courseService.getEnrolledCourses();
        for (Course course : courses) {
            coursesView.getChildren().add(createCourseCard(
                    course.getTitle(),
                    course.getDescription(),
                    course.getProgressPercentage()));
        }

        contentArea.getChildren().clear();
        contentArea.getChildren().add(new ScrollPane(coursesView));
    }

    private CourseCard createCourseCard(String title, String description, double progress) {
        return new CourseCard(title, description, progress);
    }
    // private void createView() {
    // view = new VBox(10);
    // view.setPadding(new Insets(10));

    private void showProgressView() {
        // Implement logic to show student's progress
    }

    private void showChatsView() {
        // Implement logic to show student's chats
    }

    private void applyStyling() {
        view.getStyleClass().add("dashboard");
        sidebar.getStyleClass().add("sidebar");

        // Add CSS
        // view.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }

    public BorderPane getView() {
        return view;
    }
}