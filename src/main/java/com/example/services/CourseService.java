package com.example.services;

import com.example.models.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseService {
    public List<Course> getEnrolledCourses() {
        // Fetch the enrolled courses and their progress from the database
        List<Course> courses = new ArrayList<>();

        // Example data
        courses.add(new Course(
            "Introduction to Java",
            "Learn the fundamentals of Java programming.",
            0.75
        ));
        courses.add(new Course(
            "Data Structures and Algorithms",
            "Explore efficient data structures and algorithms.",
            0.45
        ));
        courses.add(new Course(
            "Web Development with Spring Boot",
            "Build modern web applications using Spring Boot.",
            0.65
        ));

        return courses;
    }
}