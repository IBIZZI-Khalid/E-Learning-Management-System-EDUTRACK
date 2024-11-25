package com.example.models;

public class Student {
    private String id;
    private String name;
    private String email;
    private String password;

    public Student(String id, String name, String email) {
        this.id = id;    
        this.name = name;
        this.email = email;
        // this.password = password;
    }

    public String getName() {
        return name;
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
