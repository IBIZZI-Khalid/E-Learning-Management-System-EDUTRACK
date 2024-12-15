package com.example.controllers;

import com.example.models.Course;
import com.example.services.CourseService;
import com.mongodb.client.MongoDatabase;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CourseDetailsView extends VBox {

    private final Label titleLabel;
    private final Label descriptionLabel;
    private final Label accessLabel;
    private final ProgressBar progressBar;
    private final Button openPdfButton;
    private String pdfPath;
    private String studentId;
    private String courseId;
    private MongoDatabase database;

    private final QuizApp quizApp = new QuizApp();
    private CourseService courseService;

    public CourseDetailsView(MongoDatabase database, String studentId) {

        this.database = database;
        this.studentId = studentId;
        this.courseService = new CourseService(database);

        // setupUI();

        // Apply the custom style class
        getStyleClass().add("course-details-container");
        // Load the css file
        String cssPath = getClass().getResource("/css/coursedetails.css").toExternalForm();
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

        // openPdfButton.setDisable(pdfPath == null || pdfPath.isEmpty());
        // Placeholder for PDF open action (to be implemented)
        openPdfButton.setOnAction(event -> {
            if (pdfPath != null && !pdfPath.isEmpty()) {
                PDFViewer pdfViewer = new PDFViewer();
                pdfViewer.display(
                        pdfPath,
                        studentId,
                        courseId,
                        database,
                        extractedText -> {
                            System.out.println("Generating automatic quiz...");
                            quizApp.generateQuizFromPDF(pdfPath, studentId, courseId, database);
                        });
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Not Available");
                alert.setHeaderText(null);
                alert.setContentText("No PDF file is associated with this course.");
                alert.showAndWait();
            }
        });

    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    // Setter methods
    public void setDatabase(MongoDatabase database) {
        this.database = database;
        this.courseService = new CourseService(database);
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void updateCourseDetails(Course course) {
        this.courseId = course.getId();

        titleLabel.setText(course.getTitle());
        descriptionLabel.setText(course.getDescription());
        accessLabel.setText("Access: " + (course.isOpenAccess() ? "Open" : "Restricted"));

        // Update progress bar with current course progress
        String courseId = course.getId();
        double progressValue = (courseService.getStudentCourseProgressPercentage(studentId, courseId) / 100);
        System.out.println("progresspercentage from coursedetailsview.updatecoursedetails :" + progressValue);
        System.out.println("Course Details Progress Calculation:");
        System.out.println("Raw Progress: " + progressValue);
        progressBar.setProgress(progressValue);

        // Set PDF path for opening
        this.pdfPath = course.getPdfPath();
        System.out.println("_________________________Course PDF Path: " + course.getPdfPath());

    }
}