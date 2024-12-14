package com.example.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuizHandler {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyAyB4gVwpswMafG3vn0VEaBkzgEU8yk210";

    public String generateQuiz(String text) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{"
                + "\"contents\": [{"
                + "\"parts\": [{"
                + "\"text\": \"Rédigez un quiz (qcm) , de plus de cinq questions (de 5 jusqu'a 15) basé sur le texte suivant : (sans réponses + verifier que tout les questions ne sont pas vide ou mal formatée détectée!) " + text.replace("\"", "\\\"").replace("\n", " ") + "\""
                + "}]"
                + "}]"
                + "}";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return formatQuizResponse(response.toString());
        } else {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
            }
            throw new Exception("Erreur API (" + responseCode + "): " + errorResponse);
        }
    }

    public String generateAnswers(String extractedText, String quiz) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{"
            + "\"contents\": [{"
            + "\"parts\": [{"
            + "\"text\": \"Je veux des réponses précises pour le quiz suivant, basé sur ce texte : \n\n"
            + "Texte source : " + extractedText.replace("\"", "\\\"").replace("\n", " ") + "\n\n"
            + "Quiz : " + quiz.replace("\"", "\\\"").replace("\n", " ") + "\n\n"
            + "Fournissez uniquement les réponses correspondant exactement aux questions du quiz, dans le même ordre. Assurez-vous que chaque réponse est basée sur le texte source.\""
            + "}]"
            + "}]"
            + "}";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return formatAnswersResponse(response.toString());
        } else {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
            }
            throw new Exception("Erreur API (" + responseCode + "): " + errorResponse);
        }
    }

    private String formatAnswersResponse(String jsonResponse) {
        StringBuilder formattedAnswers = new StringBuilder();
        JSONObject responseObject = new JSONObject(jsonResponse);

        JSONArray candidates = responseObject.optJSONArray("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            if (content != null) {
                JSONArray parts = content.optJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject part = parts.optJSONObject(i);
                        if (part != null) {
                            String answerText = parts.getJSONObject(0).optString("text", "Réponses non disponibles.");
                            String[] answerLines = answerText.split("\n");
                            // String answer = part.optString("text", "Réponse non disponible");
                            // formattedAnswers.append(answer).append("\n");
                            for (String line : answerLines) {
                                line = line.trim();
                                if (!line.isEmpty()) {
                                    formattedAnswers.append(line).append("\n");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            formattedAnswers.append("Aucune réponse disponible.");
        }

        return formattedAnswers.toString();
    }

    private String formatQuizResponse(String jsonResponse) {
        StringBuilder formattedQuiz = new StringBuilder();
        JSONObject responseObject = new JSONObject(jsonResponse);

        JSONArray candidates = responseObject.optJSONArray("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            if (content != null) {
                JSONArray parts = content.optJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject part = parts.optJSONObject(i);
                        if (part != null) {
                            String text = part.optString("text", "Texte non disponible");
                            formattedQuiz.append(text).append("\n\n");
                        }
                    }
                }
            }
        } else {
            formattedQuiz.append("Aucun contenu disponible.");
        }

        return formattedQuiz.toString();
    }
}
