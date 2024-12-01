package com.example.controllers;

// import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.example.MainApp;
import com.example.models.Announcement;
import com.example.models.Course;
import com.example.services.AnnouncementService;
import com.example.services.CourseService;
import com.example.views.components.CourseCard;
import com.mongodb.client.MongoDatabase;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StudentDashboard {
    
    private MainApp mainApp;
    private final String studentId;  // Store student ID
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private CourseService courseService;
    private AnnouncementService announcementService;

    
    public StudentDashboard(MainApp mainApp, MongoDatabase database, String studentId) {
        if(database == null){
            throw new IllegalArgumentException("Database connection is required error in StudentDashboard line31");
        }
        this.mainApp = mainApp;
        this.studentId = studentId;
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
        Button progressBtn = new Button("My Progress");
        Button chatsBtn = new Button("Chats");
        Button announcementsBtn = new Button("Announcements");
        // Button logoutBtn = new Button("Logout");
        Button welcomeBtn = new Button("Welcome");

        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        progressBtn.setMaxWidth(Double.MAX_VALUE);
        chatsBtn.setMaxWidth(Double.MAX_VALUE);
        announcementsBtn.setMaxWidth(Double.MAX_VALUE);
        welcomeBtn.setMaxWidth(Double.MAX_VALUE);


        coursesBtn.setOnAction(e -> showCoursesView());
        progressBtn.setOnAction(e -> showProgressView());
        chatsBtn.setOnAction(e -> showChatsView());
        announcementsBtn.setOnAction(e -> showAnnouncementsView()); // Add action
        welcomeBtn.setOnAction(e -> showWelcomeView()); // Add action


        sidebar.getChildren().addAll(
            createUserProfile(),
            new Separator(),
            coursesBtn,
            progressBtn,
            chatsBtn,
            announcementsBtn,
            welcomeBtn
        );

        return sidebar;
    }

    private void showWelcomeView() {
        VBox welcomeView = new VBox(20);
        welcomeView.setPadding(new Insets(20));
    
        try {
            // Recent Courses Section
            Label recentCoursesTitle = new Label("Your Recent Courses");
            recentCoursesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    
            List<Course> courses = courseService.getCoursesForStudent(studentId);
            VBox coursesSection = new VBox(10);
            
            if (courses.isEmpty()) {
                coursesSection.getChildren().add(new Label("You are not enrolled in any courses yet."));
            } else {
                // Sort courses by progress (descending) or last accessed
                courses.sort((c1, c2) -> Double.compare(c2.getProgressPercentage(), c1.getProgressPercentage()));
                
                for (Course course : courses.subList(0, Math.min(3, courses.size()))) {
                    VBox courseCard = new VBox(5);
                    courseCard.getStyleClass().add("welcome-course-card");
    
                    Label courseTitle = new Label(course.getTitle());
                    courseTitle.setStyle("-fx-font-weight: bold;");
    
                    ProgressBar progressBar = new ProgressBar(course.getProgressPercentage() / 100.0);
                    Label progressLabel = new Label(String.format("Progress: %.1f%%", course.getProgressPercentage()));
    
                    courseCard.getChildren().addAll(courseTitle, progressBar, progressLabel);
                    coursesSection.getChildren().add(courseCard);
                }
            }
    
            // Recent Announcements Section
            Label announcementsTitle = new Label("Recent Announcements");
            announcementsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    
            VBox announcementsSection = new VBox(10);
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            
            if (announcements.isEmpty()) {
                announcementsSection.getChildren().add(new Label("No recent announcements."));
            } else {
                // Show most recent 3 announcements
                announcements.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));
                
                for (Announcement announcement : announcements.subList(0, Math.min(3, announcements.size()))) {
                    VBox announcementCard = new VBox(5);
                    announcementCard.getStyleClass().add("welcome-announcement-card");
    
                    Label titleLabel = new Label(announcement.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold;");
    
                    Label contentLabel = new Label(announcement.getContent());
                    contentLabel.setWrapText(true);
    
                    Label teacherLabel = new Label("Posted by: " + announcement.getTeacherEmail());
                    
                    announcementCard.getChildren().addAll(titleLabel, contentLabel, teacherLabel);
                    announcementsSection.getChildren().add(announcementCard);
                }
            }
    
            welcomeView.getChildren().addAll(
                new Label("Welcome, " + courseService.getStudentDetails(studentId).getString("name")),
                recentCoursesTitle,
                coursesSection,
                announcementsTitle,
                announcementsSection
            );
    
            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(welcomeView));
    
        } catch (RuntimeException e) {
            showError("Error loading welcome page", e.getMessage());
        }
    }


    private VBox createUserProfile() {
        VBox profile = new VBox(5);
        try{
            // fetching the user's data 
            Document studentData = courseService.getStudentDetails(studentId);
            

            String name = studentData.getString("name");
            String email = studentData.getString("email");
            
            // Displaying the user's data in labels

            Label nameLabel = new Label(name);
            Label emailLabel = new Label(email);

            nameLabel.getStyleClass().add("profile-name");
            emailLabel.getStyleClass().add("profile-email");

            profile.getChildren().addAll(nameLabel, emailLabel);
        }catch(RuntimeException e) {
            showError("Error loading profile", e.getMessage());
        }
        
        return profile;
    }


    private void showCoursesView() {
        VBox coursesView = new VBox(10);
        coursesView.setPadding(new Insets(20));
        
        try {
            List<Course> courses = courseService.getCoursesForStudent(studentId);
            if (courses.isEmpty()) {
                Label noCoursesLabel = new Label("No courses yet.");
                coursesView.getChildren().add(noCoursesLabel);
            } else {
                for (Course course : courses) {
                    coursesView.getChildren().add(createCourseCard(
                        course.getTitle(),
                        course.getDescription(),
                        course.getProgressPercentage(),
                        course.getId()
                    ));
                }
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(coursesView));
        } catch (RuntimeException e) {
            showError("Error loading courses", e.getMessage());
        }
    }

// Inside StudentDashboard.java
    private void showAnnouncementsView() {
        VBox announcementsView = new VBox(10);
        announcementsView.setPadding(new Insets(20));

        try {
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            // Fetch both general and course-specific announcements
                // List<Announcement> generalAnnouncements = announcementService.getGeneralAnnouncements();
                // List<Announcement> courseAnnouncements = announcementService.getAnnouncementsByCourse(studentId);

                // // Combine announcements
                // List<Announcement> allAnnouncements = new ArrayList<>();
                // allAnnouncements.addAll(generalAnnouncements);
                // allAnnouncements.addAll(courseAnnouncements);    
            
            if (announcements.isEmpty()) {
                Label noAnnouncementsLabel = new Label("No announcements at the moment.");
                announcementsView.getChildren().add(noAnnouncementsLabel);
            } else {
                for (Announcement announcement : announcements) {
                    VBox announcementCard = new VBox(5);
                    announcementCard.getStyleClass().add("announcement-card");

                    Label titleLabel = new Label(announcement.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold;");

                    Label contentLabel = new Label(announcement.getContent());
                    Label teacherLabel = new Label("Posted by: " + announcement.getTeacherEmail());
                    Label timestampLabel = new Label("Posted on: " + 
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(
                            new java.util.Date(announcement.getTimestamp())
                        )
                    );

                    announcementCard.getChildren().addAll(
                            titleLabel, 
                            contentLabel, 
                            teacherLabel, 
                            timestampLabel
                        );
                        announcementsView.getChildren().add(announcementCard);
                }
            }
            ScrollPane scrollPane = new ScrollPane(announcementsView);
            scrollPane.setFitToWidth(true);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(announcementsView));
        } catch (RuntimeException e) {
            showError("Error loading announcements", e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private CourseCard createCourseCard(String title, String description, double progress,String courseId) {
        CourseCard courseCard = new CourseCard(title, description, progress , courseId);
        
        // Add an event handler to initialize progress when the card is clicked
        courseCard.setOnMouseClicked(event -> {
            try {
                // Initialize course progress when card is clicked
                courseService.initializeCourseProgress(studentId, courseCard.getCourseId()); //courseId howa nit courseCard.getCourseId()
            } catch (Exception e) {
                showError("Course Initialization Error", e.getMessage());
            }
        });
        
        return courseCard;
    }

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
        view.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }

    public BorderPane getView() {
        return view;
    }

}