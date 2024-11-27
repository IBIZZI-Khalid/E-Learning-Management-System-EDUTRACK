package com.example.models;

public class Announcement {
    private String id;
    private String title;
    private String content;
    private String courseId; // Optional, null if general
    private String teacherEmail; // Creator's email
    private long timestamp;

    public Announcement(String id, String title, String content, String courseId, String teacherEmail, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.teacherEmail = teacherEmail;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
