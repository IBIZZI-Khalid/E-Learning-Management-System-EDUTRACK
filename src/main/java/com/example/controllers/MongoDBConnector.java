package com.example.controllers;

import com.example.models.User;
import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

public class MongoDBConnector {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    // private static Student student;
    // private static Teacher teacher;
    public static MongoCollection<Document> groupMessagesCollection;
    public static MongoCollection<Document> privateMessagesCollection;
    private static MongoCollection<Document> studentsCollection;
    private static MongoCollection<Document> teachersCollection;
    private static MongoCollection<Document> coursCollection;

    // Flexible connection method supporting multiple collections
    public static MongoDatabase connect(String s) {
        try {
            String connectionString = "mongodb://localhost:27017/EduTrack";
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("EduTrack");

            // Initialize both students and teachers collections
            studentsCollection = database.getCollection("students");
            teachersCollection = database.getCollection("teachers");
            coursCollection = database.getCollection("courses");
            groupMessagesCollection = database.getCollection("group_messages");
            privateMessagesCollection = database.getCollection("private_messages");

            System.out.println("Connected to MongoDB successfully!");
            return database;
        } catch (Exception e) {
            System.err.println("MongoDB Connection Error: " + e.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static MongoCollection<Document> getStudentsCollection() {
        return studentsCollection;
    }

    public static MongoCollection<Document> getTeachersCollection() {
        return teachersCollection;
    }

    public MongoCollection<Document> getCoursCollection() {
        return coursCollection;
    }

    public static void savePrivateMessage(String sender, String recipient, String message) {
        if (privateMessagesCollection == null) {
            System.out.println("MongoDB private messages collection not initialized");
            return;
        }

        // Création du document pour le message privé
        Document messageDocument = new Document("sender", sender)
                .append("recipient", recipient)
                .append("message", message)
                .append("timestamp", LocalDateTime.now().toString());

        // Tentative d'insertion du message
        try {
            privateMessagesCollection.insertOne(messageDocument);
            System.out.println("Private message saved successfully!");
        } catch (Exception e) {
            System.out.println("Error saving private message: " + e.getMessage());
        }
    }

    public static void saveGroupMessage(String sender, String message) {
        if (groupMessagesCollection == null) {
            System.out.println("MongoDB group messages collection not initialized");
            return;
        }

        // Création du document pour le message
        Document messageDocument = new Document("sender", sender)
                .append("message", message)
                .append("timestamp", LocalDateTime.now().toString());

        // Tentative d'insertion du message
        try {
            groupMessagesCollection.insertOne(messageDocument);
            System.out.println("Group message saved successfully!");
        } catch (Exception e) {
            System.out.println("Error saving group message: " + e.getMessage());
        }
    }

    // User registration method with role-based collection insertion
    public static void registerUser(User user, String role) {
        String salt = BCrypt.gensalt(12);
        String hashedPassword = BCrypt.hashpw(user.getPassword(), salt);

        Document userDocument = new Document(
                "name", user.getName())
                .append("email", user.getEmail())
                .append("securityQuestion", user.getSecurityQuestion())
                .append("securityAnswer", user.getSecurityAnswer())
                .append("type", role)
                .append("password", hashedPassword);

        // Choose collection based on role
        if ("Student".equals(role)) {

            studentsCollection.insertOne(userDocument);
            System.out.println("Student registered successfully!");
        } else if ("Teacher".equals(role)) {
            teachersCollection.insertOne(userDocument);
            System.out.println("Teacher registered successfully!");
        } else {
            throw new IllegalArgumentException("Invalid user role");
        }
    }

    // Login verification with role-based collection checking
    public static boolean verifyLogin(String email, String password, String role) {
        MongoCollection<Document> collection = "Student".equals(role) ? studentsCollection : teachersCollection;

        // Document query = new Document("email", email);
        Document result = collection.find(new Document("email", email)).first();

        if (result != null) {
            String storedHashedPassword = result.getString("password");
            String hashedPassword = BCrypt.hashpw(password, storedHashedPassword);
            return storedHashedPassword.equals(hashedPassword);
        }
        return false;
    }

    // Find user by email in the appropriate collection
    public static User findUserByEmail(String email, String role) {
        MongoCollection<Document> collection = "Student".equals(role) ? studentsCollection : teachersCollection;
        return User.fromDocument(collection.find(new Document("email", email)).first());
    }

    public static boolean verifySecurityAnswer(String email, String question, String answer, String role) {
        User user = findUserByEmail(email, role);
        if (user != null && user.getSecurityQuestion().equals(question)
                && user.getSecurityAnswer().equalsIgnoreCase(answer)) {
            return true;
        }
        return false;
    }

    // Password reset method for the specific role's collection
    public static void resetPassword(String email, String newPassword, String role) {
        MongoCollection<Document> collection = "Student".equals(role) ? studentsCollection : teachersCollection;

        String salt = BCrypt.gensalt(12);
        String hashedPassword = BCrypt.hashpw(newPassword, salt);

        UpdateResult result = collection.updateOne(
                new Document("email", email),
                new Document("$set", new Document("password", hashedPassword)));

        // return result.getModifiedCount() > 0;
        if (result.getModifiedCount() > 0) {
            System.out.println("Password reset successfully for email: " + email);
        } else {
            System.out.println("No user found with email: " + email);
        }
    }

    // Close database connection
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}