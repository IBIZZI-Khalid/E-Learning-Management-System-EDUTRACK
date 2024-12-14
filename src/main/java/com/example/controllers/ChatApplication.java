package com.example.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatApplication extends Application {
    private TabPane chatTabPane;
    private ListView<String> userList;
    private Map<String, TextArea> privateChatAreas = new HashMap<>();
    private TextArea messageField; // Agrandir TextArea
    private String currentUser;
    private String currentRole;



    public ChatApplication(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Chat Éducatif - " + currentUser);
            BorderPane mainLayout = createMainLayout();
            Scene scene = new Scene(mainLayout, 1000, 600);
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.show();

            MongoDBConnector.connect( "mongodb://localhost:27017" ); // Connexion à la base de données
            loadUsers();        // Chargement des utilisateurs
            loadMessages();     // Chargement des messages existants
        } catch (Exception e) {
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
        Label userListLabel = new Label("Professeurs");
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

            if (selectedTab.getText().equals("Chat Général")) {
                targetChatArea = (TextArea) selectedTab.getContent();
                sendGroupMessage(message);
            } else {
                targetChatArea = privateChatAreas.get(selectedTab.getText());
                sendPrivateMessage(selectedTab.getText(), message);
            }

            targetChatArea.appendText("Moi: " + message + "\n");
            messageField.clear();
        }
    }

    private void sendGroupMessage(String message) {
        MongoDBConnector.saveGroupMessage(currentUser, message);
    }

    private void sendPrivateMessage(String recipient, String message) {
        MongoDBConnector.savePrivateMessage(currentUser, recipient, message);
    }

    private void loadUsers() {
        MongoCollection<Document> usersCollection = MongoDBConnector.getTeachersCollection();
        if (usersCollection != null) {
            MongoCursor<Document> cursor = usersCollection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String fullname = doc.getString("name");  // Get the name of the teacher
                String userType = doc.getString("type");  // Get the user type

                // Check if the current role matches the user type or if it's "teachers"
                if (currentRole.equals("teachers") || userType.equals("Teacher")) {
                    userList.getItems().add(fullname);
                }
            }
        } else {
            showErrorDialog("Erreur de connexion", "Impossible de charger les utilisateurs", "La collection des utilisateurs est null.");
        }
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
                            new Document("sender", username).append("recipient", currentUser)
                    ))).iterator();

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
