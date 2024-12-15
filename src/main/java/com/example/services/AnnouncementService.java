package com.example.services;

import com.example.models.Announcement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class AnnouncementService {
    private final MongoCollection<Document> announcementCollection;

    public AnnouncementService(MongoDatabase database) {
        // Connect to the "announcements" collection
        this.announcementCollection = database.getCollection("announcements");
    }

    public void postAnnouncement(String Title, String announcement, String teacherEmail) {
        try {
            Document announcementDoc = new Document()
                    .append("title", Title)
                    .append("announcement", announcement)
                    .append("teacherEmail", teacherEmail)
                    .append("timestamp", System.currentTimeMillis());
            announcementCollection.insertOne(announcementDoc);
        } catch (Exception e) {
            throw new RuntimeException("Error in posting announcement method: " + e.getMessage(), e);
        }
    }

    // Fetch all announcements (both general and course-specific)
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        try (
                MongoCursor<Document> cursor = announcementCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Announcement announcement = mapDocumentToAnnouncement(doc);
                announcements.add(announcement);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all announcements: " + e.getMessage(), e);
        }
        return announcements;
    }

    public void deleteAnnouncement(String announcementId) {
        try {
            Bson filter = eq("_id", announcementId);
            announcementCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting announcement: " + e.getMessage(), e);
        }
    }

    // Fetch general announcements
    public List<Announcement> getGeneralAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        try (MongoCursor<Document> cursor = announcementCollection.find(eq("courseId", null)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Announcement announcement = mapDocumentToAnnouncement(doc);
                announcements.add(announcement);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching general announcements: " + e.getMessage(), e);
        }
        return announcements;
    }

    // Fetch announcements for a specific course
    public List<Announcement> getAnnouncementsByCourse(String courseId) {
        List<Announcement> announcements = new ArrayList<>();
        try (MongoCursor<Document> cursor = announcementCollection.find(eq("courseId", courseId)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Announcement announcement = mapDocumentToAnnouncement(doc);
                announcements.add(announcement);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching course-specific announcements: " + e.getMessage(), e);
        }
        return announcements;
    }

    // Create a new announcement
    public String createAnnouncement(String title, String content, String courseId, String teacherEmail) {
        try {
            Document announcementDoc = new Document()
                    .append("title", title)
                    .append("content", content)
                    .append("courseId", courseId)
                    .append("teacherEmail", teacherEmail)
                    .append("timestamp", System.currentTimeMillis());
            announcementCollection.insertOne(announcementDoc);
            return announcementDoc.getObjectId("_id").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating announcement: " + e.getMessage(), e);
        }
    }

    // Helper method to map a MongoDB document to an Announcement object
    private Announcement mapDocumentToAnnouncement(Document doc) {
        return new Announcement(
                doc.getObjectId("_id").toString(), // ID
                doc.getString("title"), // Title
                doc.getString("content"), // Content
                doc.getString("courseId"), // Course ID (null if general)
                doc.getString("teacherEmail"), // Teacher Email
                doc.getLong("timestamp") // Timestamp
        );
    }

}