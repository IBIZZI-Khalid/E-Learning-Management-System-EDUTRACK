package com.example.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class QuizApp {

    private final PdfHandler pdfHandler = new PdfHandler();
    private final QuizHandler quizHandler = new QuizHandler();

    private final ArrayList<String[]> questionsWithChoices = new ArrayList<>();
    private String quiz;
    private String extractedText;

    public void generateQuizFromPDF(String pdfPath) {
        try {
            BackgroundFill backgroundFill = new BackgroundFill(Color.web("#F0F4F8"), CornerRadii.EMPTY, Insets.EMPTY);
            Background background = new Background(backgroundFill);

            VBox questionsBox = new VBox(10);
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(450);
            scrollPane.setVisible(false);
            scrollPane.setContent(questionsBox);

            Button generateQuizButton = new Button("Générer Quiz");
            Button showAnswersButton = new Button("Afficher Réponses");
            Button downloadPdfButton = new Button("Télécharger en PDF");

            TextArea answersArea = new TextArea();
            answersArea.setVisible(false);

            // Styles
            generateQuizButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-border-radius: 5px;");
            showAnswersButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-border-radius: 5px;");
            downloadPdfButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-border-radius: 5px;");
            showAnswersButton.setVisible(false);
            downloadPdfButton.setVisible(false);

            if (pdfPath != null) {
                try {
                    Stage pdfStage = new Stage();

                    // Use PDFViewer to display and track progress
                    PDFViewer.display(pdfPath, () -> {
                        PDFViewer pdfViewer = new PDFViewer();
    
                        // Get the current page from the reading tracker
                        int currentPage = pdfViewer.getReadingTracker().getCurrentPage();
                        
                        // Extract keywords up to the current page
                        Map<Integer, Set<String>> keywordsMap = pdfViewer.getReadingTracker().extractKeywordsFromCompletedPages(currentPage);

                        // Combine keywords into a single text block
                        StringBuilder combinedText = new StringBuilder();
                        for (Set<String> keywords : keywordsMap.values()) {
                            combinedText.append(String.join(" ", keywords)).append(" ");
                        }
                        extractedText = combinedText.toString().trim();

                        if (extractedText.isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "Aucun texte extrait des pages terminées !");
                            return;
                        }

                        showAlert(Alert.AlertType.INFORMATION, "Texte extrait des pages terminées avec succès !");

                        // Set up quiz generation
                        generateQuizButton.setOnAction(event -> {
                            generateQuizButton.setVisible(false);

                            Label titleLabel = new Label("Quiz à passer, Bon courage !!!");
                            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                            scrollPane.setVisible(true);
                            if (extractedText.isEmpty()) {
                                showAlert(Alert.AlertType.WARNING, "Aucun contenu à traiter !");
                                return;
                            }

                            try {
                                quiz = quizHandler.generateQuiz(extractedText);
                                String[] questions = quiz.split("\n\n");

                                questionsWithChoices.clear();
                                for (String question : questions) {
                                    String[] parts = question.split("\n");

                                    if (parts.length > 1) {
                                        String[] questionAndChoices = new String[parts.length];
                                        questionAndChoices[0] = parts[0];
                                        System.arraycopy(parts, 1, questionAndChoices, 1, parts.length - 1);
                                        questionsWithChoices.add(questionAndChoices);
                                    }
                                }

                                answersArea.setVisible(false);
                                displayAllQuestions(questionsBox);
                                showAnswersButton.setVisible(true);

                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Erreur lors de la génération du quiz : " + e.getMessage());
                            }
                        });

                        showAnswersButton.setOnAction(event -> {
                            if (quiz == null || quiz.isEmpty()) {
                                showAlert(Alert.AlertType.WARNING, "Aucun quiz généré à traiter pour générer des réponses !");
                                return;
                            }

                            try {
                                StringBuilder filteredAnswers = new StringBuilder();
                                String[] questions = quiz.split("\n\n");

                                for (String question : questions) {
                                    String[] questionParts = question.split("\n");

                                    if (questionParts.length > 1 && questionParts[0] != null && !questionParts[0].isEmpty()) {
                                        String answer = quizHandler.generateAnswers(question);
                                        filteredAnswers.append(questionParts[0]).append("\n").append(answer).append("\n\n");
                                    } else {
                                        System.out.println("Question vide ou mal formatée détectée, réponse ignorée.");
                                    }
                                }

                                if (filteredAnswers.length() > 0) {
                                    answersArea.setVisible(true);
                                    answersArea.setText(filteredAnswers.toString());
                                } else {
                                    showAlert(Alert.AlertType.WARNING, "Aucune réponse valide à afficher !");
                                }

                                generateQuizButton.setVisible(false);
                                downloadPdfButton.setVisible(true);

                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Erreur lors de la génération des réponses : " + e.getMessage());
                            }
                        });

                        Label titleLabel = new Label("Quiz à passer, Bon courage !!!");
                        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                        VBox vbox = new VBox(15, titleLabel, generateQuizButton, scrollPane, showAnswersButton, answersArea, downloadPdfButton);
                        vbox.setPadding(new Insets(100));
                        vbox.setBackground(background);
                        vbox.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

                        Scene scene = new Scene(vbox);
                        pdfStage.setMaximized(true);
                        pdfStage.setScene(scene);
                        pdfStage.setTitle("Quiz généré à partir du PDF");

                        pdfStage.show();
                    });
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur lors de la préparation de l'application : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la préparation de l'application : " + e.getMessage());
        }
    }

    private void displayAllQuestions(VBox questionsBox) {
        questionsBox.getChildren().clear();

        for (String[] questionWithChoices : questionsWithChoices) {
            if (questionWithChoices.length == 0 || questionWithChoices[0] == null || questionWithChoices[0].isEmpty()) {
                System.out.println("Question vide ou mal formatée détectée !");
                continue;
            }

            Label questionLabel = new Label(questionWithChoices[0]);
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2C3E50;");

            VBox questionVBox = new VBox(5);
            questionVBox.setPadding(new Insets(10));

            questionVBox.getChildren().add(questionLabel);

            VBox choicesBox = new VBox(5);
            choicesBox.setPadding(new Insets(10, 0, 10, 20));

            for (int i = 1; i < questionWithChoices.length; i++) {
                if (questionWithChoices[i] != null && !questionWithChoices[i].isEmpty()) {
                    RadioButton choiceButton = new RadioButton(questionWithChoices[i]);
                    choiceButton.setStyle("-fx-font-size: 14px;");
                    choicesBox.getChildren().add(choiceButton);
                }
            }

            questionVBox.getChildren().add(choicesBox);
            questionsBox.getChildren().add(questionVBox);
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }
}
