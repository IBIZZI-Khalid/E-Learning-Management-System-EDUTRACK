package com.example;

import com.example.controllers.MongoDBConnector;
import com.example.controllers.SignupPage;
import com.example.controllers.StudentDashboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // Connect to MongoDB
        MongoDBConnector.connect("mongodb://localhost:27017");

        // Show Signup Page initially
        showSignupPage();
    }

    public void showSignupPage() {
        SignupPage signupPage = new SignupPage(this);
        Scene scene = new Scene(signupPage.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up");
        primaryStage.show();
    }

    public void showStudentDashboard() {
        StudentDashboard dashboard = new StudentDashboard(this);
        Scene scene = new Scene(dashboard.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Dashboard");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
