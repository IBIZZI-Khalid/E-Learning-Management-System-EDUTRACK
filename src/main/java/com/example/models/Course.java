package com.example.models;

public class Course {
    private String id;
    private String title;
    private String description;
    private double progressPercentage;
    private String teacherEmail ;
    private boolean isOpenAccess;


    public Course(String id ,String title, String description, double progressPercentage , boolean isOpenAccess) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progressPercentage = progressPercentage;
        this.isOpenAccess = isOpenAccess;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    public String getTeacherEmail() {
        return teacherEmail;
    }
    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }
    public boolean isOpenAccess() {
        return isOpenAccess;
    }
}