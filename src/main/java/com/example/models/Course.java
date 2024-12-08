package com.example.models;

import org.bson.types.ObjectId;

public class Course {
    private String id;
    private String title;
    private String description;
    private double progressPercentage;
    private String teacherEmail;
    private boolean isOpenAccess;
    private String pdfPath;

    public Course(Object courseId, String title, String description, double progressPercentage, boolean isOpenAccess) {
        // Convert ObjectId to String if necessary
        this.id = courseId instanceof ObjectId
                ? ((ObjectId) courseId).toString()
                : courseId.toString();
        // this.id = id;
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

    public String getPdfPath() {
        return pdfPath;
    }
}