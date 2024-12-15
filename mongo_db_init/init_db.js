// init-mongo.js

// Switch to the EduTrack database
db = db.getSiblingDB('EduTrack');

// Create collections
db.createCollection('students');
db.createCollection('teachers');
db.createCollection('courses');
db.createCollection('group_messages');
db.createCollection('private_messages');

// Insert the student document into the 'students' collection (without enrolledCourses)
db.students.insertOne({
    "_id": ObjectId("67559a491c487a67c681606e"),
    "name": "student",
    "email": "student@uiz.ac.ma",
    "securityQuestion": "What was your first pet's name?",
    "securityAnswer": "cc",
    "type": "Student",
    "password": "$2a$12$iR02o.4sq5QgN/i/77z5KuGbEpOFqoqqrVj6zeTKbaRxB4IqieE3a",
    "courseProgress": {
        "6749e67488b0575df620f91b": 0,
        "6755af83cdf1585560454f47": 0,
        "674f7f13d373f76a67faeb54": 0,
        "675857723872d50d03cf122d": 46.15384615384615,
        "675aef5fd74ee32f5d143314": 29.310344827586203
    }
});

// Insert the teacher document into the 'teachers' collection
db.teachers.insertOne({
    "_id": ObjectId("6755999d1c487a67c681606d"),
    "name": "teacher",
    "email": "teacher@uiz.ac.ma",
    "securityQuestion": "What was your first pet's name?",
    "securityAnswer": "coco",
    "type": "Teacher",
    "password": "$2a$12$xRACfsbToY1xNi6hj1YvguNqpZT.TcQOqUKDPMiyHvP0RCuxpOI.i"
});

