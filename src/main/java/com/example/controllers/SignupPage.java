package com.example.controllers;

import com.example.MainApp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class SignupPage {

    private MainApp mainApp;
    private VBox view;

    public SignupPage(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Name Field
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Email Field
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Confirm Password Field
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");

        // Role ComboBox
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setPromptText("Select Role");
        roleComboBox.setStyle("-fx-background-radius: 5;");

        // Signup Button
        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white; -fx-background-radius: 5;");
        signupButton.setOnAction(e -> handleSignup(
            nameField.getText(), 
            emailField.getText(), 
            passwordField.getText(),
            confirmPasswordField.getText(), 
            roleComboBox.getValue()
        ));

        // Login Link
        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setOnAction(e -> mainApp.showLoginPage());

        // Layout
        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f0f4f8;");
        view.getChildren().addAll(
            titleLabel, 
            nameField, 
            emailField, 
            passwordField, 
            confirmPasswordField, 
            roleComboBox,
            signupButton,
            loginLink
        );
        view.setMaxWidth(300);
    }

    private void handleSignup(String name, String email, String password, String confirmPassword, String role) {
        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Sign Up Error", "Incomplete Information", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Sign Up Error", "Password Mismatch", "Passwords do not match. Please try again.");
            return;
        }

        // Connect to MongoDB and insert the record
        try {
            MongoDatabase database = MongoDBConnector.getDatabase();
            if ("Student".equals(role)) {
                MongoCollection<Document> studentsCollection = database.getCollection("students");

                Document studentDoc = new Document("name", name)
                        .append("email", email)
                        .append("password", password);

                studentsCollection.insertOne(studentDoc);

                Document insertedStudent = studentsCollection.find(new Document("email", email)).first();
                if (insertedStudent != null) {
                    String studentId = insertedStudent.getObjectId("_id").toHexString();
                    mainApp.showStudentDashboard(studentId);
                }

            } else if ("Teacher".equals(role)) {
                MongoCollection<Document> teachersCollection = database.getCollection("teachers");

                Document teacherDoc = new Document("name", name)
                        .append("email", email)
                        .append("password", password);

                teachersCollection.insertOne(teacherDoc);
                mainApp.showTeacherDashboard(email);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Sign Up Error", "Registration Failed", "An error occurred during registration.");
            e.printStackTrace();
        }
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