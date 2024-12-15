package com.example.controllers;

import com.example.MainApp;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class LogOut {

    private final MainApp mainApp;

    public LogOut(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void execute() {
        // Confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout Confirmation");
        confirmDialog.setHeaderText("Are you sure you want to logout?");
        confirmDialog.setContentText("Your unsaved work might be lost.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear any session-specific data
                clearUserSession();
                
                // Redirect to login page
                mainApp.showLoginPage();
            }
        });
    }

    private void clearUserSession() {
        // Add any additional session clearing logic here
        // For example, clearing cached user data, closing database connections, etc.
        System.out.println("User session cleared.");
    }
}