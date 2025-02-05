package com.example.services;

import com.example.models.Course;
import com.example.models.Student;
import com.example.models.Teacher;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

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

    public MongoCollection<Document> getCourseCollection() {
        return courseCollection;
    }

    // Fetch courses created by a teacher
    public List<Course> getCoursesByTeacher(String teacherEmail) {
        List<Course> courses = new ArrayList<>();
        // Fetch courses from the database
        MongoCursor<Document> cursor = courseCollection.find(Filters.eq("teacherEmail", teacherEmail)).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                double progressPercentage = doc.get("progressPercentage") instanceof Integer
                        ? doc.getInteger("progressPercentage").doubleValue()
                        : doc.getDouble("progressPercentage");

                Course course = new Course(
                        doc.getObjectId("_id").toString(), // Course ID
                        doc.getString("title"), // Title
                        doc.getString("description"), // Description
                        doc.getString("pdfPath"),
                        // doc.getDouble("progressPercentage"), // Average progress of students
                        // (optional)
                        progressPercentage,
                        doc.getBoolean("isOpenAccess"));

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
                                    doc.getString("email"),
                                    doc.getString("securityQuestion"),
                                    doc.getString("securityAnswer"),
                                    doc.getString("password"));
                            students.add(student);
                        });
            }
            return students;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching students: " + e.getMessage(), e);
        }
    }

    public List<Document> getTeachersForStudentsEnroledCourses(String studentId) {
        List<Document> teachers = new ArrayList<>();

        Document student = studentCollection.find(Filters.eq("_id", new ObjectId(studentId))).first();
        if (student != null) {
            List<String> enrolledCourses = student.getList("enrolledCourses", String.class);
            for (String courseId : enrolledCourses) {
                Document course = courseCollection.find(Filters.eq("_id", new ObjectId(courseId))).first();
                if (course != null) {
                    String teacherEmail = course.getString("teacherEmail");
                    Document teacher = teacherCollection.find(Filters.eq("email", teacherEmail)).first();
                    if (teacher != null) {
                        teachers.add(teacher);
                    }
                }
            }
        }
        return teachers;
    }

    public String createCourse(String title, String description, String teacherEmail, String PDFpath[],
            boolean isOpenAccess) {
        try {
            Document course = new Document()
                    .append("title", title)
                    .append("description", description)
                    .append("teacherEmail", teacherEmail)
                    .append("progressPercentage", 0.0) // Start with 0 progress
                    .append("pdfPath", PDFpath[0])
                    .append("isOpenAccess", isOpenAccess);

            courseCollection.insertOne(course);
            return course.getObjectId("_id").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating course: " + e.getMessage(), e);
        }
    }

    public List<Course> getCoursesForStudent(String studentId) {
        List<Course> courses = new ArrayList<>();

        try {
            // First get the student's enrolled course IDs
            Document student = studentCollection.find(Filters.eq("_id", new ObjectId(studentId))).first();

            List<String> enrolledCourseIds = new ArrayList<>();
            if (student != null && student.containsKey("enrolledCourses")) {
                enrolledCourseIds = student.getList("enrolledCourses", String.class);
            }

            // Fetch open-access courses and the student's enrolled courses
            courseCollection.find(Filters.or(
                    Filters.eq("isOpenAccess", true),
                    Filters.in("_id", enrolledCourseIds))).forEach(doc -> {
                        // first lets find course percentage hiatch fih error ma3rfoch program wach int
                        // wlla double
                        double progressPercentage = doc.get("progressPercentage") instanceof Integer
                                ? doc.getInteger("progressPercentage").doubleValue()
                                : doc.getDouble("progressPercentage");

                        Course course = new Course(
                                doc.getObjectId("_id").toString(),
                                doc.getString("title"),
                                doc.getString("description"),
                                doc.getString("pdfPath"),
                                // (optional)
                                // doc.getDouble("progressPercentage"),
                                progressPercentage,
                                doc.getBoolean("isOpenAccess"));
                        courses.add(course);
                    });

            // // Then fetch the actual courses
            // courseCollection.find(Filters.in("_id", enrolledCourseIds))
            // .forEach(doc -> {
            // Course course = new Course(
            // doc.getObjectId("_id").toString(),
            // doc.getString("title"),
            // doc.getString("description"),
            // doc.getDouble("progressPercentage"));
            // courses.add(course);
            // });
            // }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching enrolled courses: " + e.getMessage(), e);
        }
        return courses;
    }

    public void initializeCourseProgress(String studentId, String courseId) {
        try {
            // Check if the student is already enrolled in the course
            Document student = studentCollection.find(
                    Filters.and(
                            Filters.eq("_id", new ObjectId(studentId)),
                            Filters.in("enrolledCourses", courseId)))
                    .first();

            // If student is not enrolled, add them to the course
            if (student == null) {
                addStudentToCourse(studentId, courseId);

            }

            // Update or set the course progress to 0
            studentCollection.updateOne(
                    Filters.eq("_id", new ObjectId(studentId)),
                    Updates.set("courseProgress." + courseId, 0.0));
            System.out.println(
                    "------------------------progress initialized to 00.0 ---------------------------------- ");

        } catch (Exception e) {
            throw new RuntimeException("Error initializing course progress: " + e.getMessage(), e);
        }
    }

    public double getAverageStudentProgressForCourse(String courseId) {
        try {
            // Find all students enrolled in this course
            List<Document> studentsInCourse = new ArrayList<>();
            studentCollection.find(Filters.in("enrolledCourses", courseId))
                    .into(studentsInCourse);

            // If no students are enrolled, return 0 progress
            if (studentsInCourse.isEmpty()) {
                return 0.0;
            }

            // Calculate total progress
            double totalProgress = 0.0;
            int studentCount = studentsInCourse.size();

            // Iterate through students and sum their progress
            for (Document student : studentsInCourse) {
                // Assuming each student document has a courseProgress document or array
                // You might need to adjust this based on how you store student progress
                Document courseProgress = (Document) student.get("courseProgress");
                if (courseProgress != null) {
                    Double progress = courseProgress.getDouble(courseId);
                    if (progress != null) {
                        totalProgress += progress;
                    }
                }
            }

            // Calculate and return average progress
            return totalProgress / studentCount;

        } catch (Exception e) {
            // Log the error and return a default progress
            System.err.println("Error calculating average course progress: " + e.getMessage());
            return 0.0;
        }
    }

    public void updateCourseProgress(String studentId, String courseId, int currentPage, int totalPages) {
        try {
            // Calculate progress percentage
            double progressPercentage = (double) currentPage / totalPages * 100.0;
            System.out.println("progressPercentage from courseservice line 241:" + progressPercentage);

            // Update student's course progress in the database
            studentCollection.updateOne(
                    Filters.and(
                            Filters.eq("_id", new ObjectId(studentId)),
                            Filters.eq("enrolledCourses", courseId)),
                    Updates.set("courseProgress." + courseId, progressPercentage));

            // Optionally, update the overall course progress (average of all students)
            double avgProgress = getAverageStudentProgressForCourse(courseId);
            courseCollection.updateOne(
                    Filters.eq("_id", new ObjectId(courseId)),
                    Updates.set("progressPercentage", avgProgress));

            System.out.println("Updated course progress for student: " + studentId +
                    ", Course: " + courseId +
                    ", Progress: " + progressPercentage + "%");

        } catch (Exception e) {
            throw new RuntimeException("Error updating course progress: " + e.getMessage(), e);
        }
    }

    public Course getCourseById(String courseId) {
        try {
            ObjectId objectId = new ObjectId(courseId);
            Document courseDoc = courseCollection.find(Filters.eq("_id", objectId)).first();
            if (courseDoc == null) {
                throw new RuntimeException("Course not found with ID: " + courseId);
            }

            // Explicitly extract and convert _id to String
            ObjectId docId = courseDoc.getObjectId("_id");
            return new Course(
                    // courseDoc.getString("_id"),
                    docId.toString(),
                    courseDoc.getString("title"),
                    courseDoc.getString("description"),
                    // courseDoc.getDouble("teacherEmail"),
                    courseDoc.getString("pdfPath"),
                    courseDoc.getDouble("progressPercentage"),
                    courseDoc.getBoolean("isOpenAccess"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid course ID format: " + courseId);
        }
    }

    public double getStudentCourseProgressPercentage(String studentId, String courseId) {
        try {
            // Find the student document
            Document student = studentCollection.find(Filters.eq("_id", new ObjectId(studentId))).first();

            // Verify student exists
            if (student == null) {
                throw new RuntimeException("Student not found with ID: " + studentId);
            }

            // Safely retrieve enrolled courses
            Object enrolledCoursesObj = student.get("enrolledCourses");

            // Check if enrolledCourses is a list
            if (!(enrolledCoursesObj instanceof List)) {
                System.out.println("Invalid enrolled courses format");
                return 0.0;
            }

            // Cast to List and check if the student is enrolled in the course
            List<String> enrolledCourses = (List<String>) enrolledCoursesObj;
            if (!enrolledCourses.contains(courseId)) {
                // Instead of throwing an exception, you might want to handle this differently
                // For example, return 0.0 or log a warning
                System.out.println("Student is not enrolled in the specified course");
                return 0.0;
            }

            // Extract the courseProgress document
            Document courseProgress = (Document) student.get("courseProgress");

            // Retrieve the progress for the specific course
            if (courseProgress != null && courseProgress.containsKey(courseId)) {
                Object progressValue = courseProgress.get(courseId);

                // Handle both Integer and Double progress values
                if (progressValue instanceof Integer) {
                    return ((Integer) progressValue).doubleValue();
                } else if (progressValue instanceof Double) {
                    return (Double) progressValue;
                }
            }

            // If no progress is found, return 0
            return 0.0;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving student course progress: " + e.getMessage(), e);
        }
    }

    public void addStudentToCourse(String studentId, String courseId) {
        try {
            studentCollection.updateOne(
                    Filters.eq("_id", new ObjectId(studentId)),
                    Updates.addToSet("enrolledCourses", courseId));
        } catch (Exception e) {
            throw new RuntimeException("Error adding student to course: " + e.getMessage(), e);
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
            Document teacher = teacherCollection.find(Filters.eq("email", email)).first();
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