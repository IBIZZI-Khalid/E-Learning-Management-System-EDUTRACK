package com.example.controllers;

import com.example.MainApp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

        // making a choice (student or teacher) :
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setPromptText("Select Role");

        Button signupButton = new Button("Sign Up");
        signupButton.setOnAction(e -> handleSignup(nameField.getText(), emailField.getText(), passwordField.getText(),
                confirmPasswordField.getText(), roleComboBox.getValue()));

        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(nameField, emailField, passwordField, confirmPasswordField, roleComboBox,
                signupButton);
    }

    private void handleSignup(String name, String email, String password, String confirmPassword, String role) {
        if (!password.equals(confirmPassword)) {
            // Show error alert if passwords do not match
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Sign Up Error");
            alert.setHeaderText("Password Mismatch");
            alert.setContentText("Passwords do not match. Please try again.");
            alert.showAndWait();
        } else {
            // Connect to MongoDB and insert the student record
            try {
                MongoDatabase database = MongoDBConnector.getDatabase();
                if ("Student".equals(role)) {
                    MongoCollection<Document> studentsCollection = database.getCollection("students");

                    Document studentDoc = new Document("name", name)
                            .append("email", email)
                            .append("password", password);

                    studentsCollection.insertOne(studentDoc);

                    // goin in to the db again to get the student's id
                    Document insertedStudent = studentsCollection.find(new Document("email", email)).first();
                    if (insertedStudent != null) {
                        String studentId = insertedStudent.getObjectId("_id").toHexString();

                        // Redirect to the student dashboard
                        System.out.println("Redirecting to the student dashboard...");
                        mainApp.showStudentDashboard(studentId);
                        System.out.println("Redirected! Data added!");
                    }

                } else if ("Teacher".equals(role)) {
                    MongoCollection<Document> teachersCollection = database.getCollection("teachers");

                    Document teacherDoc = new Document("name", name)
                            .append("email", email)
                            .append("password", password);

                    teachersCollection.insertOne(teacherDoc);

                    // Redirect to the student dashboard after successful sign-up
                    System.out.println("redirecting to the teachersdash ...");
                    mainApp.showTeacherDashboard(email);
                    System.out.println("redirected ! data added!");

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error while adding data to db!");
            }
        }
    }

    public VBox getView() {
        return view;
    }
}
