package com.example.controllers;

import com.example.MainApp;
import com.example.models.User;

import javafx.geometry.Insets;
// import com.example.models.User;
// import com.example.controllers.MongoDBConnector;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.MongoDatabase;
// import org.bson.Document;
// import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
// import javafx.scene.paint.Color;

public class SignupPage {

    private MainApp mainApp;
    private VBox view;
    private ScrollPane mainScrollPane;

    public SignupPage(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {

        // Modify the view container to use the new styles
        view = new VBox(15);
        view.getStyleClass().addAll("auth-container", "auth-background-animated");
        view.setPrefWidth(Region.USE_COMPUTED_SIZE);
        view.setMaxWidth(600);
        view.setPrefHeight(Region.USE_COMPUTED_SIZE);
        view.setMaxHeight(800);

         // Add padding for better spacing
        view.setPadding(new Insets(20));

        // Create a card to hold authentication elements
        VBox authCard = new VBox(15);
        authCard.getStyleClass().add("auth-card");
        

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("auth-title");

        // Name Field
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.getStyleClass().add("auth-text-field");

        // Email Field
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("auth-text-field");

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-text-field");

        // Confirm Password Field
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.getStyleClass().add("auth-text-field");

        // Security Question Dropdown
        ComboBox<String> securityQuestionComboBox = new ComboBox<>();
        securityQuestionComboBox.getItems().addAll(
                "What was your first pet's name?",
                "In what city were you born?",
                "What is your mother's maiden name?");
        securityQuestionComboBox.setPromptText("Select Security Question");
        securityQuestionComboBox.getStyleClass().add("auth-combo-box");

        // Security Answer Field
        TextField securityAnswerField = new TextField();
        securityAnswerField.setPromptText("Security Answer");
        securityAnswerField.getStyleClass().add("auth-text-field");

        // Role ComboBox
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Teacher");
        roleComboBox.setPromptText("Select Role");
        roleComboBox.getStyleClass().add("auth-combo-box");

        

        // Signup Button
        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("auth-button");
        signupButton.setOnAction(e -> handleSignup(
                nameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                securityQuestionComboBox.getValue(),
                securityAnswerField.getText(),
                roleComboBox.getValue()));

        // Login Link
        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.getStyleClass().add("auth-hyperlink");
        loginLink.setOnAction(e -> mainApp.showLoginPage());

        view.getStylesheets().add(getClass().getResource("/css/authentication.css").toExternalForm());

        // Assemble the card
        authCard.getChildren().addAll(
                titleLabel,
                nameField,
                emailField,
                passwordField,
                confirmPasswordField,
                securityQuestionComboBox,
                securityAnswerField,
                roleComboBox,
                signupButton,
                loginLink);

        // Add the card to the main view
        view.getChildren().add(authCard);

        // Create ScrollPane to enable scrolling and adaptive sizing
        mainScrollPane = new ScrollPane(view);
        mainScrollPane.setFitToWidth(true);   // Adjust width automatically
        mainScrollPane.setFitToHeight(true);  // Allow vertical scrolling if needed
        
        // Set minimum and maximum constraints
        mainScrollPane.setMinWidth(350);      // Minimum reasonable width
        mainScrollPane.setMaxWidth(600);      // Maximum reasonable width
        mainScrollPane.setPrefWidth(450);     // Preferred default width
        
        // Vertical sizing
        mainScrollPane.setMinHeight(500);     // Minimum height
        mainScrollPane.setMaxHeight(800);     // Maximum height
        
        // Ensure smooth scrolling and no horizontal scrollbar
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private boolean validateInputs(String name, String email, String password,
            String confirmPassword, String securityQuestion,
            String securityAnswer, String role) {
        // Comprehensive input validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || securityQuestion == null ||
                securityAnswer.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Sign Up Error",
                    "Incomplete Information", "Please fill in all fields.");
            return false;
        }

        // Email domain validation
        if (!email.endsWith("@uiz.ac.ma")) {
            showAlert(Alert.AlertType.ERROR, "Email Error",
                    "Invalid Email", "Please use a university email.");
            return false;
        }

        // Password matching
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Error",
                    "Password Mismatch", "Passwords do not match.");
            return false;
        }

        // Password strength check
        if (password.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Password Error",
                    "Weak Password", "Password must be at least 8 characters long.");
            return false;
        }

        return true;
    }

    private void handleSignup(String name, String email, String confirmPassword,
            String password, String securityQuestion,
            String securityAnswer, String role) {
        // Validation
        if (validateInputs(name, email, password, confirmPassword,
                securityQuestion, securityAnswer, role)) {
            // Connect to MongoDB and insert the record
            try {
                // MongoDatabase database = MongoDBConnector.getDatabase();
                User newUser = new User(null, name, email, password,
                        securityQuestion, securityAnswer, role);

                // Register user using MongoDBConnector
                MongoDBConnector.registerUser(newUser, role);

                // Navigate to appropriate dashboard
                if ("Student".equals(role)) {
                    // we gotta find the email value so  that the welcome page is showen correctly
                    User foundUser = MongoDBConnector.findUserByEmail(email, role);
                    mainApp.showStudentDashboard(foundUser.getId());
                } else {
                    mainApp.showTeacherDashboard(email);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Sign Up Error", "Registration Failed",
                        "An error occurred during registration.");
                e.printStackTrace();
            }
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