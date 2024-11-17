package com.example.controllers;

import com.example.MainApp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SignupPage {

    private MainApp mainApp;
    private VBox view;

    public SignupPage(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        Button signupButton = new Button("Sign Up");
        signupButton.setOnAction(e -> handleSignup(nameField.getText(), emailField.getText(), passwordField.getText(),
                confirmPasswordField.getText()));

        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(nameField, emailField, passwordField, confirmPasswordField, signupButton);
    }

    private void handleSignup(String name, String email, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            // Show error alert if passwords do not match
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Sign Up Error");
            alert.setHeaderText("Password Mismatch");
            alert.setContentText("Passwords do not match. Please try again.");
            alert.showAndWait();
        } else {
            // Connect to MongoDB and insert the student record
            MongoDatabase database = MongoDBConnector.getDatabase();
            MongoCollection<Document> studentsCollection = database.getCollection("Test");

            Document studentDoc = new Document("name", name)
                    .append("email", email)
                    .append("password", password);

            studentsCollection.insertOne(studentDoc);

            // Redirect to the student dashboard after successful sign-up
            mainApp.showStudentDashboard();
        }
    }

    public VBox getView() {
        return view;
    }
}
