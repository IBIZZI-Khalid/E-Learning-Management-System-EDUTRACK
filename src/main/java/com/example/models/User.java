package com.example.models;
import org.bson.Document;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private String type; // "student" or "teacher"

    public User(String id, String name, String email, String password ,String securityQuestion,String securityAnswer, String type) {
        this.id = id;    
        this.name = name;
        this.email = email;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.type=type;
    }

    public static User fromDocument(Document document) {
        return new User(
            document.getObjectId("_id").toHexString(),
            document.getString("name"),
            document.getString("email"),
            document.getString("password"),
            document.getString("securityQuestion"),
            document.getString("securityAnswer"),
            document.getString("type")
        );
    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getId() {
        return id;
    }
    public String getType() {
        return type;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public String getPassword() {
        return password;
    }
}

