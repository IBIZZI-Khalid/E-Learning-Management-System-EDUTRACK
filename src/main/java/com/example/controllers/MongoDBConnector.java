package com.example.controllers;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnector {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoDatabase connect(String connectionString) {
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase("TestingApp");
        System.out.println("Connected to MongoDB successfully!");
        return database;
    }

    // we dont actually need this one since we already return the database flmethod li qbel but meeeeh
    public static MongoDatabase getDatabase() {
        return database;
    }
}
