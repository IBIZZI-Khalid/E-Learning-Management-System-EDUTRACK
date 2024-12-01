package com.example.controllers;

import com.example.MainApp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
// import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class LoginPage {

    private MainApp mainApp;
    private VBox view;

    public LoginPage(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        // Title
        Label titleLabel = new Label("Welcome Back");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Email Field
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Role ComboBox
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setPromptText("Select Role");
        roleComboBox.setStyle("-fx-background-radius: 5;");

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText(), roleComboBox.getValue()));

        // Signup Link
        Hyperlink signupLink = new Hyperlink("Don't have an account? Sign Up");
        signupLink.setOnAction(e -> mainApp.showSignupPage());

        // Layout
        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f0f4f8;");
        view.getChildren().addAll(
            titleLabel, 
            emailField, 
            passwordField, 
            roleComboBox, 
            loginButton, 
            signupLink
        );
        view.setMaxWidth(300);
    }

    private void handleLogin(String email, String password, String role) {
        if (role == null || email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Missing Information", "Please fill in all fields and select a role.");
            return;
        }
        
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
                try {
                    MongoDatabase database = MongoDBConnector.getDatabase();
                    MongoCollection<Document> collection = database.getCollection(role.equals("Student") ? "students" : "teachers");
                    Document userDoc = collection.find(new Document("email", email).append("password", password)).first();
                    
                    if (userDoc != null) {
                        javafx.application.Platform.runLater(() -> {
                            if (role.equals("Student")) {
                                String studentId = userDoc.getObjectId("_id").toHexString();
                                mainApp.showStudentDashboard(studentId);
                            } else {
                                mainApp.showTeacherDashboard(email);
                            }
                        });
                    } else {
                        throw new Exception("Invalid credentials");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> 
                        showAlert(AlertType.ERROR, "Login Error", "Login Failed", "Invalid email or password. Please try again.")
                    );
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}