package com.example.controllers;

import com.example.MainApp;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class StudentDashboard {

    private MainApp mainApp;
    private VBox view;

    public StudentDashboard(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        view = new VBox(10);
        view.setPadding(new Insets(10));

        Label welcomeLabel = new Label("Welcome, [Student Name]");

        // Example of course progress
        VBox courseBox = new VBox(10);
        Label courseLabel = new Label("Course: Introduction to Java");
        ProgressBar progressBar = new ProgressBar(0.75); // 75% completion
        Button viewCourseButton = new Button("View Course");
        viewCourseButton.setOnAction(e -> viewCourse());

        courseBox.getChildren().addAll(courseLabel, progressBar, viewCourseButton);

        Button groupChatButton = new Button("Group Chat");
        groupChatButton.setOnAction(e -> openGroupChat());

        Button profileButton = new Button("Profile & Settings");
        profileButton.setOnAction(e -> openProfileSettings());

        view.getChildren().addAll(welcomeLabel, courseBox, groupChatButton, profileButton);
    }

    private void viewCourse() {
        // Implement logic to view course details
    }

    private void openGroupChat() {
        // Implement logic to open group chat
    }

    private void openProfileSettings() {
        // Implement logic to open profile settings
    }

    public VBox getView() {
        return view;
    }
}
