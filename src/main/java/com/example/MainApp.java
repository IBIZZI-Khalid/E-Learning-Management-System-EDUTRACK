package com.example;

import com.example.controllers.MongoDBConnector;
import com.example.controllers.SignupPage;
// import com.example.controllers.;
import com.example.controllers.LoginPage;
import com.example.controllers.StudentDashboard;
import com.example.controllers.TeacherDashboard;
import com.mongodb.client.MongoDatabase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;
    private StudentDashboard studentDashboard;
    private TeacherDashboard teacherDashboard;
    private MongoDatabase mongoDatabase;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        try {
            // Connect to MongoDB
            mongoDatabase = MongoDBConnector.connect("mongodb://localhost:27017");
            // Show Signup Page initially
            showLoginPage();
            // showSignupPage();

        } catch (Exception e) {
            System.out.println("Error occurred while connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();

        }
    }

    public void showSignupPage() {
        SignupPage signupPage = new SignupPage(this);
        Scene scene = new Scene(signupPage.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up");
        primaryStage.show();
    }

    public void showLoginPage() {
        LoginPage loginpage = new LoginPage(this);
        Scene scene = new Scene(loginpage.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public void showStudentDashboard(String studentId) {
        StudentDashboard studentDashboard = new StudentDashboard(
            this,
            mongoDatabase,  // Your MongoDB database instance
            studentId
        );        
        Scene scene = new Scene(studentDashboard.getView(), 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Dashboard");
        primaryStage.show();
    }

    public void showTeacherDashboard(String teacherEmail) {
        teacherDashboard = new TeacherDashboard(
            this, 
            mongoDatabase, 
            teacherEmail
        );
        Scene scene = new Scene(teacherDashboard.getView(), 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Teacher Dashboard");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
