package com.example.controllers;
import com.example.MainApp; 
import com.mongodb.client.MongoCollection; 
import com.mongodb.client.MongoDatabase; 
import org.bson.Document;

import javafx.concurrent.Task;
import javafx.geometry.Insets; 
import javafx.scene.control.Alert; 
import javafx.scene.control.Alert.AlertType; 
import javafx.scene.control.Button; 
import javafx.scene.control.ComboBox; 
import javafx.scene.control.PasswordField; 
import javafx.scene.control.TextField; 
import javafx.scene.layout.VBox;

public class LoginPage {

    private MainApp mainApp;
    private VBox view;

    public LoginPage(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // making a choice (student or teacher) :
        ComboBox<String> roleComboBox = new ComboBox<>(); 
        roleComboBox.getItems().addAll("Student", "Teacher"); 
        roleComboBox.setPromptText("Select Role");
        


        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText(), roleComboBox.getValue()));

        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(emailField, passwordField,roleComboBox, loginButton);
    }

    private void handleLogin(String email, String password, String role) {
        if (role == null || email.isEmpty() || password.isEmpty()) { // Show error alert if any fields are empty 
            Alert alert = new Alert(AlertType.ERROR); 
            alert.setTitle("Login Error"); 
            alert.setHeaderText("Missing Information"); 
            alert.setContentText("Please fill in all fields and select a role."); 
            alert.showAndWait(); 
            return; 
        }
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception { 
                try { 
                    MongoDatabase database = MongoDBConnector.getDatabase(); 
                    MongoCollection<Document> collection = database.getCollection(role.equals("Student") ? "students" : "teachers"); 
                    Document userDoc = collection.find(new Document("email", email).append("password", password)).first(); 
                    if (userDoc != null) { 
                        // Print debug info 
                        System.out.println(role + " logged in successfully!"); 
                        // Redirect to the appropriate dashboard 
                        javafx.application.Platform.runLater(() -> { 
                            if (role.equals("Student")) { 
                                // if its a student we need their ID to call lmethod lli ghat3tina interface dyalo
                                String studentId = userDoc.getObjectId("_id").toHexString(); //to get the id of the logged student
                                mainApp.showStudentDashboard(studentId); 
                            } 
                            else { 
                                // if its a teacher we need their email to call lmethod lli ghat3tina interface dyalo
                                mainApp.showTeacherDashboard(email); 
                            } 
                        }); 
                    }else{
                            throw new Exception("Invalid credentials"); 
                        } 

                } catch (Exception ex) { 
                        ex.printStackTrace(); 
                        // Show error alert on failure 
                        javafx.application.Platform.runLater(() -> { 
                            Alert alert = new Alert(AlertType.ERROR); 
                            alert.setTitle("Login Error"); 
                            alert.setHeaderText("Login Failed"); 
                            alert.setContentText("Invalid email or password. Please try again."); 
                            alert.showAndWait(); 
                        }); 
                } return null; 
                }
            
        };
        new Thread(task).start();
    }

    public VBox getView() {
        return view;
    }
}
