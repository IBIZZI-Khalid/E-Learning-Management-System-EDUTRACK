
Overview
This is a Java-based E-Learning Management System designed for online education, facilitating course management, announcements, and progress tracking for both teachers and students.

_____________________________________________________________________________________


1. Running Locally
    Starting the Database
        To start the MongoDB database locally, run the following command in your terminal:
        C:\mongodb\bin>mongod --dbpath C:\mongodb\data\db

    Running the Application with JavaFX
        To run the application using JavaFX, navigate to the project directory and run:
        C:\Users\hp\Desktop\EduTrack\EduTrack> mvn javafx:run

This command ensures the application runs smoothly, especially since it was developed on a Windows operating system. Using this approach will minimize unexpected errors.


_____________________________________________________________________________________


2. Running the App in Docker
    If you'd prefer to run the app in a Docker container, follow these steps:
        In the project directory, run:
            docker-compose up --build

This will:

Install all the necessary dependencies.
Create the required database and collections, initializing them with data from the mongo_db_init\init_db.js script.
Insert an example user-teacher, user-student, and a course document with associated paths.


_____________________________________________________________________________________

3. Login Information
    Once the app is running, you can log in using the following credentials:
        Student login:
            Email: student@uiz.ac.ma
            Password: studentstudent
        Teacher login:
            Email: teacher@uiz.ac.ma
            Password: teacherteacher

the teacher's main features are
when trynna upload a pdf as a teacher I suggest to use this one in : pdf_examples\IntroductionJavaFX-V1 (1).pdf

_____________________________________________________________________________________

Known Issues


1. Progress Tracking and Quiz Suggestion
    The app is designed to track the user's progress as they read a PDF. After reading, when the user closes the app, it is supposed to:
        -Display a page suggesting a quiz based on the keywords extracted from the content they’ve read.
        -Use the Gemini API to generate the quiz based on these keywords.

    However, this feature works fine in local Windows tests (you can see test screenshots in src\test\java\com\example\tests_on_windows_local), but it is encountering an issue when running inside Docker. The error is not yet identified, but it is related to Docker's environment.
.
.
.

2. YouTube Video Suggestions (Incomplete)
    Due to time constraints, the YouTube scraper feature is not fully integrated yet. The idea is to use the keywords extracted from the content the user has finished reading and then suggest relevant YouTube videos through the YouTube Scraper.

    Currently, the scraper is standalone, and you can test it independently by following these steps:
        1. Copy the file src\main\java\com\example\YouTubeScraper.java to your local PC that has chrome.exe installed.
        2. Update the path to chrome.exe on line 53 of the YouTubeScraper.java file.
        3. Run the class to generate a file.txt containing the video links scraped by Selenium WebDriver.

    Once the progress tracking issue is resolved, we plan to integrate the YouTube video suggestion feature into the app.