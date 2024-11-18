package com.example;

import com.example.controllers.MongoDBConnector;
import com.example.controllers.SignupPage;
import com.example.controllers.StudentDashboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;
    private StudentDashboard studentDashboard ;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        try{
            // Connect to MongoDB
            MongoDBConnector.connect("mongodb://localhost:27017");
            // Show Signup Page initially
            showSignupPage();

        }catch(Exception e){
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

    public void showStudentDashboard() {
        // if (studentDashboard == null) {                                              // we first check if the studentDashboard instance has been created. 
            studentDashboard = new StudentDashboard(this);                           // If not, we create a new instance and store it in the studentDashboard field.
        // } 
        Scene scene = new Scene(studentDashboard.getView(), 1024, 768); // We then create a new Scene with the StudentDashboard view 
        primaryStage.setScene(scene);                                                //and set it as the primary stage's scene.
        primaryStage.setTitle("Student Dashboard");
        primaryStage.show();
    }
    
    
    
    public static void main(String[] args) {
        launch(args);
    }
}
