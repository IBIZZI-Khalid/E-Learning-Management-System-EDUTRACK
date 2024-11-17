// package com.example.controllers;

// import com.example.MainApp;

// import javafx.scene.control.Button;
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;

// public class LoginPage {

//     private MainApp mainApp;
//     private VBox view;

//     public LoginPage(MainApp mainApp) {
//         this.mainApp = mainApp;
//         createView();
//     }

//     private void createView() {
//         TextField usernameField = new TextField();
//         usernameField.setPromptText("Username");

//         PasswordField passwordField = new PasswordField();
//         passwordField.setPromptText("Password");

//         Button loginButton = new Button("Login");
//         loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

//         view = new VBox(10);
//         view.getChildren().addAll(usernameField, passwordField, loginButton);
//     }

//     private void handleLogin(String username, String password) {
//         // Implement your login logic here
//         if (username.equals("student")) {
//             mainApp.showStudentDashboard();
//         } else if (username.equals("teacher")) {
//             mainApp.showTeacherDashboard();
//         }
//     }

//     public VBox getView() {
//         return view;
//     }
// }
