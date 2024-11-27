package com.example.controllers;

import com.example.MainApp;
import com.example.models.Course;
import com.example.models.Student;
import com.example.services.AnnouncementService;
import com.example.services.CourseService;
import com.example.views.components.CourseCard;
import com.mongodb.client.MongoDatabase;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

import org.bson.Document;

public class TeacherDashboard {

    private MainApp mainApp;
    private final String teacherEmail;
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private CourseService courseService;
    private AnnouncementService announcementService;

    public TeacherDashboard(MainApp mainApp, MongoDatabase database, String teacherEmail) {
        this.mainApp = mainApp;
        this.teacherEmail = teacherEmail;
        this.courseService = new CourseService(database);
        this.announcementService = new AnnouncementService(database);
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
        Button manageStudentsBtn = new Button("Manage Students");
        Button announcementsBtn = new Button("Announcements");

        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        manageStudentsBtn.setMaxWidth(Double.MAX_VALUE);
        announcementsBtn.setMaxWidth(Double.MAX_VALUE);

        coursesBtn.setOnAction(e -> showCoursesView());
        manageStudentsBtn.setOnAction(e -> showManageStudentsView());
        announcementsBtn.setOnAction(e -> showAnnouncementsView());

        sidebar.getChildren().addAll(
                createUserProfile(),
                new Separator(),
                coursesBtn,
                manageStudentsBtn,
                announcementsBtn);

        return sidebar;
    }

    private VBox createUserProfile() {
        VBox profile = new VBox(5);
        try {
            Document teacherData = courseService.getTeacherDetails(teacherEmail);
            String name = teacherData.getString("name");
            String email = teacherData.getString("email");

            Label nameLabel = new Label(name);
            Label emailLabel = new Label(email);

            nameLabel.getStyleClass().add("profile-name");
            emailLabel.getStyleClass().add("profile-email");

            profile.getChildren().addAll(nameLabel, emailLabel);

        } catch (RuntimeException e) {
            showError("Error loading profile", e.getMessage());
        }
        return profile;
    }

    private void showCoursesView() {
        VBox coursesView = new VBox(10);
        coursesView.setPadding(new Insets(20));
        try {
            List<Course> courses = courseService.getCoursesByTeacher(teacherEmail);
            // Fetch courses created by this teacher
            for (Course course : courses) {
                coursesView.getChildren().add(createCourseCard(
                        course.getTitle(),
                        course.getDescription(),
                        course.getProgressPercentage()));
            }

            Button createCourseBtn = new Button("Create New Course");
            createCourseBtn.setOnAction(e -> showCreateCourseView());
            coursesView.getChildren().add(createCourseBtn);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(coursesView));
        } catch (Exception e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }
    }

    private CourseCard createCourseCard(String title, String description, double progress) {
        return new CourseCard(title, description, progress);
    }

    private void showManageStudentsView() {
        VBox studentsView = new VBox(10);
        studentsView.setPadding(new Insets(20));

        try {
            List<Student> students = courseService.getStudentsForTeacherCourses(teacherEmail);
            for (Student student : students) {
                VBox studentCard = new VBox(5);
                studentCard.getStyleClass().add("student-card");

                Label nameLabel = new Label("Name: " + student.getName());
                Label emailLabel = new Label("Email: " + student.getEmail());

                studentCard.getChildren().addAll(nameLabel, emailLabel);
                studentsView.getChildren().add(studentCard);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(studentsView));
        } catch (RuntimeException e) {
            System.out.println("Error loading students" + e.getMessage());
        }
    }

    private void showCreateCourseView() {
        VBox createCourseView = new VBox(10);
        createCourseView.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Course Description");

        Button saveBtn = new Button("Save Course");
        saveBtn.setOnAction(e -> {
            try {
                String title = titleField.getText();
                String description = descriptionField.getText();

                if (title.isEmpty() || description.isEmpty()) {
                    showError("Validation Error", "Title and description are required");
                    return;
                }

                String courseId = courseService.createCourse(title, description, teacherEmail);
                showSuccess("Course Created", "Course has been successfully created with ID: " + courseId);
                showCoursesView(); // Refresh courses view
            } catch (RuntimeException ex) {
                showError("Error creating course", ex.getMessage());
            }
        });

        createCourseView.getChildren().addAll(
                new Label("Create New Course"),
                titleField,
                descriptionField,
                saveBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(createCourseView);
    }

    private void showAnnouncementsView() {
        VBox announcementsView = new VBox(10);
        announcementsView.setPadding(new Insets(20));

        TextArea announcementTitlefield = new TextArea();
        announcementTitlefield.setPromptText("Your announcement Title here ...");

        TextArea announcementField = new TextArea();
        announcementField.setPromptText("Write your announcement here...");

        Button postBtn = new Button("Post Announcement");
        postBtn.setOnAction(e -> {
            try {
                String aTitle = announcementTitlefield.getText();
                String announcement = announcementField.getText();

                if (announcement.isEmpty()) {
                    showError("Validation Error", "Announcement text is required");
                    return;
                }
                
                announcementService.postAnnouncement(aTitle , announcement, teacherEmail);
                showSuccess("Success", "Announcement posted successfully");
                announcementField.clear();
                announcementTitlefield.clear();

            } catch (RuntimeException ex) {
                showError("Error posting announcement", ex.getMessage());
            }
        });

        announcementsView.getChildren().addAll(
            new Label("Post Announcements"),
            announcementField,
            announcementTitlefield,
            postBtn
        );

        contentArea.getChildren().clear();
        contentArea.getChildren().add(announcementsView);
    }

    private void applyStyling() {
        view.getStyleClass().add("dashboard");
        sidebar.getStyleClass().add("sidebar");

        // Add CSS
        view.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }

    public BorderPane getView() {
        return view;
    }

    // Helper methods for showing dialogs
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
