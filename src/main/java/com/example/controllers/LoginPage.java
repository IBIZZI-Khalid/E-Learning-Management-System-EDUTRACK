package com.example.controllers;

import com.example.MainApp;
import com.example.models.User;

import javafx.concurrent.Task;
// import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
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
        view = new VBox(15);
        view.getStyleClass().addAll("auth-container", "auth-background-animated");

        // Ensure computed sizing for responsiveness
        view.setPrefWidth(Region.USE_COMPUTED_SIZE);
        view.setPrefHeight(Region.USE_COMPUTED_SIZE);
        view.setMaxWidth(Region.USE_COMPUTED_SIZE);
        view.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // Title
        Label titleLabel = new Label("Welcome Back");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);
        titleLabel.getStyleClass().add("auth-title");

        // Email Field
        TextField emailField = new TextField();
        emailField.setPromptText("Your University Email here");
        // emailField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");
        emailField.getStyleClass().add("auth-text-field");
        emailField.setPrefWidth(350);

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        // passwordField.setStyle("-fx-background-radius: 5; -fx-padding: 10px;");
        passwordField.getStyleClass().add("auth-text-field");

        // Role ComboBox
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setPromptText("Select Role");
        // roleComboBox.setStyle("-fx-background-radius: 5;");
        roleComboBox.getStyleClass().add("auth-combo-box");

        VBox authCard = new VBox(15);
        authCard.getStyleClass().add("auth-card");

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("auth-button");
        // loginButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;
        // -fx-background-radius: 5;");
        loginButton
                .setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText(), roleComboBox.getValue()));

        // Signup Link
        Hyperlink signupLink = new Hyperlink("Don't have an account? Sign Up");
        signupLink.getStyleClass().add("auth-hyperlink");
        signupLink.setOnAction(e -> mainApp.showSignupPage());

        view.getStylesheets().add(getClass().getResource("/css/authentication.css").toExternalForm());

        // Assemble the card
        authCard.getChildren().addAll(
                titleLabel,
                emailField,
                passwordField,
                roleComboBox,
                loginButton,
                signupLink);

        // Add the card to the main view
        view.getChildren().add(authCard);
        // view.setMaxWidth(600);
        // view.setMaxHeight(400);
    }

    private void handleLogin(String email, String password, String role) {
        if (role == null || email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Missing Information",
                    "Please fill in all fields and select a role.");
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    boolean isLoginSuccessful = MongoDBConnector.verifyLogin(email, password, role);

                    if (isLoginSuccessful) {
                        User user = MongoDBConnector.findUserByEmail(email, role);

                        javafx.application.Platform.runLater(() -> {
                            if (role.equals("Student")) {
                                mainApp.showStudentDashboard(user.getId());
                            } else {
                                mainApp.showTeacherDashboard(email);
                            }
                        });
                    } else {
                        throw new Exception("Invalid credentials");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> showAlert(AlertType.ERROR, "Login Error", "Login Failed",
                            "Invalid email or password. Please try again."));
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