package com.example.services;

import com.example.models.Course;
import com.example.models.Student;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
// import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class CourseService {
    private final MongoCollection<Document> courseCollection;
    private final MongoCollection<Document> studentCollection;
    private final MongoCollection<Document> teacherCollection;

    public CourseService(MongoDatabase database) {
        this.courseCollection = database.getCollection("courses");
        this.studentCollection = database.getCollection("students");
        this.teacherCollection = database.getCollection("teachers");
    }

    // Fetch courses created by a teacher
    public List<Course> getCoursesByTeacher(String teacherEmail) {
        List<Course> courses = new ArrayList<>();
        // Fetch courses from the database
        MongoCursor<Document> cursor = courseCollection.find(Filters.eq("teacherEmail", teacherEmail)).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Course course = new Course(
                        doc.getObjectId("_id").toString(), // Course ID
                        doc.getString("title"), // Title
                        doc.getString("description"), // Description
                        doc.getDouble("progressPercentage") // Average progress of students (optional)
                                                            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                );
                course.setTeacherEmail(doc.getString(teacherEmail));
                courses.add(course);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching courses: " + e.getMessage(), e);
        } finally {
            cursor.close();
        }
        return courses;
    }

    // Fetch students for all courses created by a teacher
    public List<Student> getStudentsForTeacherCourses(String teacherEmail) {
        List<Student> students = new ArrayList<>();

        try {
            // Fetch course IDs for this teacher
            List<String> courseIds = new ArrayList<>();
            courseCollection.find(Filters.eq("teacherEmail", teacherEmail))
                    .forEach(doc -> courseIds.add(doc.getObjectId("_id").toString()));

            // Fetch students enrolled in these courses
            if (!courseIds.isEmpty()) {
                studentCollection.find(Filters.in("enrolledCourses", courseIds))
                        .forEach(doc -> {
                            Student student = new Student(
                                    doc.getObjectId("_id").toString(),
                                    doc.getString("name"),
                                    doc.getString("email"));
                            students.add(student);
                        });
            }
            return students;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching students: " + e.getMessage(), e);
        }
    }

    public String createCourse(String title, String description, String teacherEmail) {
        try {
            Document course = new Document()
                    .append("title", title)
                    .append("description", description)
                    .append("teacherEmail", teacherEmail)
                    .append("progressPercentage", 0.0); // Start with 0 progress
            courseCollection.insertOne(course);
            return course.getObjectId("_id").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating course: " + e.getMessage(), e);
        }
    }

    // Post an announcement
    

    public List<Course> getEnrolledCourses(String studentId) {
        List<Course> courses = new ArrayList<>();

        try {
            // First get the student's enrolled course IDs
            Document student = studentCollection.find(Filters.eq("_id", new ObjectId(studentId))).first();
            if (student != null && student.containsKey("enrolledCourses")) {
                List<String> enrolledCourseIds = student.getList("enrolledCourses", String.class);

                // Then fetch the actual courses
                courseCollection.find(Filters.in("_id", enrolledCourseIds))
                        .forEach(doc -> {
                            Course course = new Course(
                                    doc.getObjectId("_id").toString(),
                                    doc.getString("title"),
                                    doc.getString("description"),
                                    doc.getDouble("progressPercentage"));
                            courses.add(course);
                        });
            }
            return courses;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching enrolled courses: " + e.getMessage(), e);
        }
    }

    public Document getStudentDetails(String studentId) {
        try {
            Document student = studentCollection.find(Filters.eq("_id", new ObjectId(studentId))).first();
            if (student == null) {
                throw new RuntimeException("Student not found for ID: " + studentId);
            }
            return student;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching student details: " + e.getMessage(), e);
        }
    }

    public Document getTeacherDetails(String email) {
        try {
            Document teacher = teacherCollection.find(Filters.eq("email", new ObjectId(email))).first();
            if (teacher == null) {
                throw new RuntimeException("teacher not found for email: " + email);
            }
            return teacher;
        } catch (Exception e) {
            System.out.println("________________________________________________________________________________");
            System.out.println(e.getMessage());
            throw new RuntimeException("Error fetching tecaher details: " + e.getMessage(), e);
        }

    }
}