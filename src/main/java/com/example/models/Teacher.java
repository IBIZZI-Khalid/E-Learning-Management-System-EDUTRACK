package com.example.models;
import org.bson.Document;

public class Teacher {

    private String id;
    private String name;
    private String email;
    private String password;
    private String securityQuestion;
    private String securityAnswer;

    public Teacher(String id, String name, String email,String securityQuestion,String securityAnswer, String password) {
        this.id = id;    
        this.name = name;
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.password = password;
    }
    public static Teacher getfromDocument(Document document) {
        return new Teacher(
                document.getObjectId("_id").toHexString(),
                document.getString("name"),
                document.getString("email"),
                document.getString("securityQuestion"),
                document.getString("securityAnswer"),
                document.getString("password")
        );
    }

    public String getName() {
        return name;
    }
    
    public String getSecurityAnswer() {
        return securityAnswer;
    }
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    public String getId() {
        return id;
    }
}


