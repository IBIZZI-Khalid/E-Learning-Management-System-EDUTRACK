package com.example.controllers;

import com.example.models.Course;
import com.example.models.Student;
import com.example.services.CourseService;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatApplication extends Application {
    private TabPane chatTabPane;
    private ListView<String> userList;
    private Map<String, TextArea> privateChatAreas = new HashMap<>();
    private TextArea messageField;
    private String currentUser;
    private String currentRole;
    private String teacherEmail;
    private String studentId;
    private CourseService courseService; 

    public enum UserType {
        TEACHER,
        STUDENT
    }

    public ChatApplication(String username, String role, String identifier, UserType userType, MongoDatabase database) {
        this.currentUser = username;
        this.currentRole = role;
        this.courseService = new CourseService(database); 
        
        if (userType == UserType.TEACHER) {
            this.teacherEmail = identifier;
            this.studentId = null;
        } else {
            this.studentId = identifier;
            this.teacherEmail = null;
        }
    }

    public static ChatApplication createForTeacher(String username, String role, String teacherEmail ,MongoDatabase database) {
        return new ChatApplication(username, role, teacherEmail, UserType.TEACHER , database);
    }

    public static ChatApplication createForStudent(String username, String role, String studentId,MongoDatabase database) {
        return new ChatApplication(username, role, studentId, UserType.STUDENT ,database);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("EDU-TRACK Chat , Welcome " + currentUser);
            BorderPane mainLayout = createMainLayout();
            Scene scene = new Scene(mainLayout, 1000, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
            MongoDBConnector.connect("mongodb://localhost:27017");
            loadMessages();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur de démarrage", "Impossible de lancer l'application", e.getMessage());
        }
    }

    private BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        chatTabPane = new TabPane();
        Tab groupChatTab = createGroupChatTab();
        chatTabPane.getTabs().add(groupChatTab);

        VBox userListContainer = createUserListContainer();
        HBox inputArea = createInputArea();

        VBox chatBox = new VBox(10);
        chatBox.getChildren().addAll(chatTabPane, inputArea);

        mainLayout.setCenter(chatBox);
        mainLayout.setRight(userListContainer);

        return mainLayout;
    }

    private Tab createGroupChatTab() {
        Tab groupChatTab = new Tab("Chat Général");
        groupChatTab.setClosable(false);

        TextArea groupChatArea = new TextArea();
        groupChatArea.setEditable(false);
        groupChatArea.setWrapText(true);
        groupChatArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-border-color: #0078d7; -fx-padding: 10px;");

        groupChatTab.setContent(groupChatArea);
        return groupChatTab;
    }

    private VBox createUserListContainer() {
        String chatTitle;
        if (currentRole.equals("Teacher")) {
            chatTitle = "Students";
        } else {
            chatTitle = "Professors";
        }
    
        Label userListLabel = new Label(chatTitle);
        userListLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Rectangle blueRectangle = new Rectangle(150, 10);
        blueRectangle.setFill(Color.BLUE);

        userList = createUserList();

        VBox userListContainer = new VBox(10);
        userListContainer.getChildren().addAll(userListLabel, blueRectangle, userList);
        userListContainer.setPadding(new Insets(10));
        userListContainer.setStyle("-fx-background-color: #e0f7fa; -fx-border-color: #0078d7; -fx-border-radius: 5px;");

        return userListContainer;
    }

    private ListView<String> createUserList() {
        ListView<String> userList = new ListView<>();
        userList.setPrefWidth(150);

        List<String> users = getUsersBasedOnCurrentUserType();
        userList.getItems().addAll(users);

        userList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedUser = userList.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    openPrivateChat(selectedUser);
                }
            }
        });

        return userList;
    }

    private List<String> getUsersBasedOnCurrentUserType() {
        try {
            if (currentRole.equals("Teacher")) {
                return getTeacherStudents();
            } else {
                return getStudentTeachers();
            }
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            return new ArrayList<>(); // Return empty list on error
        }
    }

    private List<String> getTeacherStudents() {
        // logic to get all the students :
            // List<String> students = new ArrayList<>();
            // MongoCollection<Document> SCollection = MongoDBConnector.getStudentsCollection();
            // if (SCollection != null) {
            //     MongoCursor<Document> cursor = SCollection.find().iterator();
            //     while (cursor.hasNext()) {
            //         Document doc = cursor.next();
            //         String fullname = doc.getString("name");
            //         students.add(fullname);
            //     }
            // }
            // return students;
        
        // logic to get students for this teacher :
         try {
            // Using CourseService to get students for this teacher
            List<Student> students = courseService.getStudentsForTeacherCourses(teacherEmail);
            
            // Convert Student objects to student names for display
            return students.stream()
                         .map(Student::getName)
                         .collect(Collectors.toList());
                         
        } catch (Exception e) {
            System.err.println("Error fetching teacher's students: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> getStudentTeachers() {
        try {
            System.out.println("Fetching teachers for student ID: " + studentId);
            List<Document> teacherDocs = courseService.getTeachersForStudentsEnroledCourses(studentId);
            System.out.println("Found " + teacherDocs.size() + " teachers");
            List<String> teacherNames = teacherDocs.stream()
            .map(doc -> doc.getString("name"))
            .distinct()
            .collect(Collectors.toList());
            
            System.out.println("Processed teacher names: " + String.join(", ", teacherNames));
            return teacherNames;
                
        } catch (Exception e) {
            System.err.println("Error fetching student's teachers: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for better debugging

            return new ArrayList<>();
        }
    }

    private HBox createInputArea() {
        messageField = new TextArea();
        messageField.setPromptText("Écrivez votre message ici...");
        messageField.setPrefHeight(80);
        messageField.setWrapText(true);
        messageField.setStyle("-fx-border-color: #0078d7; -fx-border-radius: 5px; -fx-padding: 10px;");

        Button sendButton = new Button("Envoyer");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox inputArea = new HBox(10);
        inputArea.getChildren().addAll(messageField, sendButton);
        inputArea.setPadding(new Insets(10));

        return inputArea;
    }

    private void openPrivateChat(String username) {
        for (Tab tab : chatTabPane.getTabs()) {
            if (tab.getText().equals(username)) {
                chatTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Tab privateTab = new Tab(username);
        TextArea privateChatArea = new TextArea();
        privateChatArea.setEditable(false);
        privateChatArea.setWrapText(true);
        privateChatArea.setStyle("-fx-control-inner-background: #fff5e6; -fx-border-color: #ff8c00;");

        privateTab.setContent(privateChatArea);
        privateChatAreas.put(username, privateChatArea);

        chatTabPane.getTabs().add(privateTab);
        chatTabPane.getSelectionModel().select(privateTab);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            Tab selectedTab = chatTabPane.getSelectionModel().getSelectedItem();
            TextArea targetChatArea;

            if (selectedTab.getText().equals("General Chat")) {
                targetChatArea = (TextArea) selectedTab.getContent();
                sendGroupMessage(message);
            } else {
                targetChatArea = privateChatAreas.get(selectedTab.getText());
                sendPrivateMessage(selectedTab.getText(), message);
            }

            targetChatArea.appendText("Me: " + message + "\n");
            messageField.clear();
        }
    }

    private void sendGroupMessage(String message) {
        MongoDBConnector.saveGroupMessage(currentUser, message);
    }

    private void sendPrivateMessage(String recipient, String message) {
        MongoDBConnector.savePrivateMessage(currentUser, recipient, message);
    }

    private void loadMessages() {
        MongoCursor<Document> groupMessagesCursor = MongoDBConnector.groupMessagesCollection.find().iterator();
        while (groupMessagesCursor.hasNext()) {
            Document doc = groupMessagesCursor.next();
            String sender = doc.getString("sender");
            String message = doc.getString("message");
            String timestamp = doc.getString("timestamp");

            Tab groupChatTab = chatTabPane.getTabs().get(0);
            TextArea groupChatArea = (TextArea) groupChatTab.getContent();

            // Styliser les messages des professeurs différemment
            if (sender.equals("Professeur")) {
                groupChatArea.appendText("[Professeur] " + sender + " (" + timestamp + "): " + message + "\n");
                groupChatArea.setStyle("-fx-background-color: #ffccf2;"); // Couleur pour les professeurs
            } else {
                groupChatArea.appendText(sender + " (" + timestamp + "): " + message + "\n");
            }
        }

        for (String username : userList.getItems()) {
            MongoCursor<Document> privateMessagesCursor = MongoDBConnector.privateMessagesCollection
                    .find(new Document("$or", Arrays.asList(
                            new Document("sender", currentUser).append("recipient", username),
                            new Document("sender", username).append("recipient", currentUser))))
                    .iterator();

            while (privateMessagesCursor.hasNext()) {
                Document doc = privateMessagesCursor.next();
                String sender = doc.getString("sender");
                String message = doc.getString("message");
                String timestamp = doc.getString("timestamp");

                openPrivateChat(username);
                TextArea privateChatArea = privateChatAreas.get(username);
                privateChatArea.appendText(sender + " (" + timestamp + "): " + message + "\n");
            }
        }
    }

    private void showErrorDialog(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
