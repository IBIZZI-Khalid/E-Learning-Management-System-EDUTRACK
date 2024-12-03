package com.example.controllers;

import com.example.MainApp;
import com.example.models.Announcement;
import com.example.models.Course;
import com.example.models.Student;
import com.example.services.AnnouncementService;
import com.example.services.CourseService;
import com.example.views.components.CourseCard;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
    private final MongoCollection<Document> studentCollection;

    public TeacherDashboard(MainApp mainApp, MongoDatabase database, String teacherEmail) {
        this.mainApp = mainApp;
        this.teacherEmail = teacherEmail;
        this.courseService = new CourseService(database);
        this.announcementService = new AnnouncementService(database);
        this.studentCollection = database.getCollection("students");

        createView();
    }

    private void createView() {
        view = new BorderPane();
        sidebar = createSidebar();
        contentArea = new StackPane();

        view.setLeft(sidebar);
        view.setCenter(contentArea);

        applyStyling();
        showWelcomeView();
    }

    private void addStudentToCourseView() {
        VBox addStudentView = new VBox(10);
        addStudentView.setPadding(new Insets(20));

        TextField studentEmailField = new TextField();
        studentEmailField.setPromptText("Student Email");

        TextField courseIdField = new TextField();
        courseIdField.setPromptText("Course ID");

        Button addButton = new Button("Add Student");
        addButton.setOnAction(e -> {
            try {
                String studentEmail = studentEmailField.getText();
                String courseId = courseIdField.getText();

                Document student = studentCollection.find(Filters.eq("email", studentEmail)).first();
                if (student != null) {
                    String studentId = student.getObjectId("_id").toString();
                    courseService.addStudentToCourse(studentId, courseId);
                    showSuccess("Success", "Student added to course.");
                } else {
                    showError("Error", "Student not found.");
                }
            } catch (Exception ex) {
                showError("Error adding student", ex.getMessage());
            }
        });

        addStudentView.getChildren().addAll(studentEmailField, courseIdField, addButton);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(addStudentView);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);

        Button welcomeBtn = new Button("Home");
        Button coursesBtn = new Button("My Courses");
        Button manageStudentsBtn = new Button("Manage Students");
        Button announcementsBtn = new Button("Announcements");
        Button addStudentBtn = new Button("Add A Student");
        Button logoutBtn = new Button("Logout");

        welcomeBtn.setMaxWidth(Double.MAX_VALUE);
        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        manageStudentsBtn.setMaxWidth(Double.MAX_VALUE);
        announcementsBtn.setMaxWidth(Double.MAX_VALUE);
        addStudentBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        welcomeBtn.setOnAction(e -> showWelcomeView());
        coursesBtn.setOnAction(e -> showCoursesView());
        manageStudentsBtn.setOnAction(e -> showManageStudentsView());
        announcementsBtn.setOnAction(e -> showAnnouncementsView());
        addStudentBtn.setOnAction(e -> addStudentToCourseView());
        logoutBtn.setOnAction(e -> new LogOut(mainApp).execute());

        sidebar.getChildren().addAll(
                createUserProfile(),
                new Separator(),
                welcomeBtn,
                coursesBtn,
                manageStudentsBtn,
                announcementsBtn,
                addStudentBtn,
                logoutBtn);

        return sidebar;
    }

    private void showWelcomeView() {
        VBox welcomeView = new VBox(20);
        welcomeView.getStyleClass().add("welcome-container");

        try {
            Label mainWelcomeHeader = new Label(
                    "Welcome, " + courseService.getTeacherDetails(teacherEmail).getString("name"));
            mainWelcomeHeader.getStyleClass().add("label-header");

            // Course Progress Section
            Label courseProgressTitle = new Label("Course Insights");
            courseProgressTitle.getStyleClass().add("welcome-header-text");

            HBox courseProgressSection = new HBox(10);
            courseProgressSection.getStyleClass().add("welcome-card-container");

            // Ensure the HBox expands to fit its children
            courseProgressSection.setMinWidth(Region.USE_PREF_SIZE);
            courseProgressSection.setPrefWidth(Region.USE_COMPUTED_SIZE);
            courseProgressSection.setMaxWidth(Region.USE_COMPUTED_SIZE);

            List<Course> courses = courseService.getCoursesByTeacher(teacherEmail);

            if (courses.isEmpty()) {
                courseProgressSection.getChildren().add(new Label("You have not created any courses yet."));
            } else {
                for (Course course : courses) {
                    VBox courseCard = new VBox(5);
                    courseCard.getStyleClass().add("welcome-card");
                    courseCard.setPrefWidth(250); // Set fixed width for course cards
                    courseCard.setMaxWidth(250);

                    Label courseTitle = new Label(course.getTitle());
                    courseTitle.getStyleClass().add("welcome-card-title");

                    // Calculate average student progress
                    double avgProgress = courseService.getAverageStudentProgressForCourse(course.getId());
                    ProgressBar progressBar = new ProgressBar(avgProgress / 100.0);
                    progressBar.getStyleClass().add("welcome-progress-bar");

                    Label progressLabel = new Label(String.format("Average Student Progress: %.1f%%", avgProgress));
                    progressLabel.getStyleClass().add("welcome-progress-label");

                    courseCard.getChildren().addAll(courseTitle, progressBar, progressLabel);
                    courseProgressSection.getChildren().add(courseCard);
                }
            }

            ScrollPane coursesScrollPane = new ScrollPane(courseProgressSection);
            coursesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            coursesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            coursesScrollPane.setFitToHeight(true);
            coursesScrollPane.setFitToWidth(false); // Prevent resizing to fit width
            coursesScrollPane.setPannable(true); // Allow panning

            // Recent Announcements Section
            Label announcementsTitle = new Label("Recent Announcements");
            announcementsTitle.getStyleClass().add("welcome-header-text");

            HBox announcementsSection = new HBox(10);
            announcementsSection.getStyleClass().add("welcome-announcement-card-container");
            announcementsSection.setMinWidth(Region.USE_PREF_SIZE);
            announcementsSection.setPrefWidth(Region.USE_COMPUTED_SIZE);
            announcementsSection.setMaxWidth(Region.USE_COMPUTED_SIZE);

            List<Announcement> announcements = announcementService.getAllAnnouncements();

            if (announcements.isEmpty()) {
                announcementsSection.getChildren().add(new Label("No recent announcements."));
            } else {
                // Show most recent 5 announcements
                announcements.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));

                for (Announcement announcement : announcements.subList(0, Math.min(5, announcements.size()))) {
                    VBox announcementCard = new VBox(5);
                    announcementCard.getStyleClass().add("welcome-announcement-card");

                    Label titleLabel = new Label(announcement.getTitle());
                    titleLabel.getStyleClass().add("welcome-announcement-card-title");

                    Label contentLabel = new Label(announcement.getContent());
                    contentLabel.getStyleClass().add("welcome-announcement-card-content");

                    announcementCard.getChildren().addAll(titleLabel, contentLabel);
                    announcementsSection.getChildren().add(announcementCard);
                }
            }

            ScrollPane announcementsScrollPane = new ScrollPane(announcementsSection);
            announcementsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            announcementsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            announcementsScrollPane.setFitToHeight(true);
            announcementsScrollPane.setFitToWidth(false); // Prevent resizing to fit width
            announcementsScrollPane.setPannable(true);

            // Add sections to welcomeView
            welcomeView.getChildren().addAll(
                    mainWelcomeHeader,
                    courseProgressTitle,
                    coursesScrollPane,
                    announcementsTitle,
                    announcementsScrollPane);

            // Clear and add welcomeView to content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(welcomeView);

        } catch (RuntimeException e) {
            showError("Error loading welcome page", e.getMessage());
        }
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
            Button createCourseBtn = new Button("Create New Course");
            createCourseBtn.setOnAction(e -> showCreateCourseView());
            coursesView.getChildren().add(createCourseBtn);

            List<Course> courses = courseService.getCoursesByTeacher(teacherEmail);
            // Fetch courses created by this teacher
            for (Course course : courses) {
                coursesView.getChildren().add(createCourseCard(
                        course.getTitle(),
                        course.getDescription(),
                        course.getProgressPercentage(),
                        course.getId()

                ));
            }

            

            contentArea.getChildren().clear();
            contentArea.getChildren().add(new ScrollPane(coursesView));
        } catch (Exception e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }
    }

    private CourseCard createCourseCard(String title, String description, double progress, String courseId) {
        return new CourseCard(title, description, progress, courseId);
    }
    private void showManageStudentsView() {
        VBox studentsView = new VBox(15); // Main container for student cards
        studentsView.setPadding(new Insets(20));
        studentsView.getStyleClass().add("students-container");
    
        try {
            List<Student> students = courseService.getStudentsForTeacherCourses(teacherEmail);
    
            if (students.isEmpty()) {
                Label noStudentsLabel = new Label("No students enrolled in your courses.");
                noStudentsLabel.getStyleClass().add("no-students-label");
                studentsView.getChildren().add(noStudentsLabel);
            } else {
                for (Student student : students) {
                    VBox studentCard = new VBox(10);
                    studentCard.getStyleClass().add("student-card");
    
                    Label nameLabel = new Label("Name: " + student.getName());
                    nameLabel.getStyleClass().add("student-card-name");
    
                    Label emailLabel = new Label("Email: " + student.getEmail());
                    emailLabel.getStyleClass().add("student-card-email");
    
                    studentCard.getChildren().addAll(nameLabel, emailLabel);
                    studentsView.getChildren().add(studentCard);
                }
            }
    
            // Wrap the studentsView in a ScrollPane for scrolling
            ScrollPane scrollPane = new ScrollPane(studentsView);
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    
            // Set the scroll pane into the content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);
        } catch (RuntimeException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }
    
    private void showCreateCourseView() {
        VBox createCourseView = new VBox(10);
        createCourseView.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Course Description");

        // Add checkbox for "Open Access"
        CheckBox openAccessCheckbox = new CheckBox("Make this course open to all students");
        openAccessCheckbox.setSelected(false); // Default to restricted

        Button saveBtn = new Button("Save Course");
        saveBtn.setOnAction(e -> {
            try {
                String title = titleField.getText();
                String description = descriptionField.getText();
                boolean isOpenAccess = openAccessCheckbox.isSelected();

                if (title.isEmpty() || description.isEmpty()) {
                    showError("Validation Error", "Title and description are required");
                    return;
                }

                String courseId = courseService.createCourse(title, description, teacherEmail, isOpenAccess);
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
                openAccessCheckbox,
                saveBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(createCourseView);
    }

    private void showAnnouncementsView() {
        VBox mainView = new VBox(20); // Main container for the entire announcements view
        mainView.setPadding(new Insets(20));
        mainView.getStyleClass().add("announcements-container");
    
        try {
            // Section for posting new announcements
            VBox postAnnouncementSection = new VBox(10); // Container for posting fields
            postAnnouncementSection.getStyleClass().add("post-announcement-section");

            Label postit =  new Label("Post a New Announcement");
            postit.getStyleClass().add("welcome-header-text"); //dyal lwelcome view but who cares ... it works 

            TextArea announcementTitleField = new TextArea();
            announcementTitleField.setPromptText("Your announcement title here...");
            announcementTitleField.setPrefHeight(50);
    
            TextArea announcementField = new TextArea();
            announcementField.setPromptText("Write your announcement here...");
            announcementField.setPrefHeight(100);
    
            Button postBtn = new Button("Post Announcement");
            postBtn.setOnAction(e -> {
                try {
                    String aTitle = announcementTitleField.getText();
                    String announcementContent = announcementField.getText();
    
                    if (announcementContent.isEmpty()) {
                        showError("Validation Error", "Announcement text is required");
                        return;
                    }
    
                    announcementService.postAnnouncement(aTitle, announcementContent, teacherEmail);
                    showSuccess("Success", "Announcement posted successfully");
                    announcementField.clear();
                    announcementTitleField.clear();
    
                    // Refresh the announcements view
                    showAnnouncementsView();
                } catch (RuntimeException ex) {
                    showError("Error posting announcement", ex.getMessage());
                }
            });
    
            postAnnouncementSection.getChildren().addAll(
                postit,
                announcementTitleField,
                announcementField,
                postBtn
            );
    
            // Section for displaying existing announcements
            VBox announcementsView = new VBox(10); // Container for announcements list
            announcementsView.getStyleClass().add("announcements-list");
    
            List<Announcement> announcements = announcementService.getAllAnnouncements();
    
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
                                    new java.util.Date(announcement.getTimestamp())));
    
                    announcementCard.getChildren().addAll(
                            titleLabel,
                            contentLabel,
                            teacherLabel,
                            timestampLabel);
                    announcementsView.getChildren().add(announcementCard);
                }
            }
    
            // Wrap announcementsView in a ScrollPane for vertical scrolling
            ScrollPane scrollPane = new ScrollPane(announcementsView);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    
            // Add posting section and scrollable announcements list to mainView
            mainView.getChildren().addAll(postAnnouncementSection, scrollPane);
    
            // Set mainView in content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(mainView);
    
        } catch (Exception e) {
            showError("Error loading announcements page: ", e.getMessage());
        }
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
