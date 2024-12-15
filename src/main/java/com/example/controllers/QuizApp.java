package com.example.controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.util.ArrayList;

import com.mongodb.client.MongoDatabase;

public class QuizApp {

    private final PdfHandler pdfHandler = new PdfHandler();
    private final QuizHandler quizHandler = new QuizHandler();

    private final ArrayList<String[]> questionsWithChoices = new ArrayList<>();
    private String quiz;
    private String extractedText;

    public void generateQuizFromPDF(String pdfPath, String studentId, String courseId, MongoDatabase database) {
        try {
            Stage quizStage = new Stage();
            quizStage.setTitle("Quiz généré à partir du PDF");

            // Load the css
            String cssPath = getClass().getResource("/css/quizapp.css").toExternalForm();

            PDFViewer pdfViewer = new PDFViewer();
            pdfViewer.display(
                    pdfPath,
                    studentId, // Provide the student ID
                    courseId, // Provide the course ID if available
                    database, // Provide the MongoDB database
                    extractedText -> {
                        if (extractedText == null || extractedText.trim().isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "Aucun texte extrait du PDF !");
                            return;
                        }

                        this.extractedText = extractedText;

                        // Create a centered main container
                        VBox mainContainer = new VBox(15);
                        mainContainer.setAlignment(Pos.CENTER);
                        mainContainer.getStyleClass().add("quiz-container");
                        mainContainer.setPadding(new Insets(20));

                        Label titleLabel = new Label("Quiz à passer, Bon courage !!!");
                        titleLabel.getStyleClass().add("quiz-title");
                        titleLabel.setAlignment(Pos.CENTER);

                        // Improved loading indicator
                        ProgressIndicator loadingIndicator = new ProgressIndicator();
                        loadingIndicator.getStyleClass().add("loading-indicator");
                        loadingIndicator.setVisible(false);
                        // Make loading indicator larger and more prominent
                        loadingIndicator.setPrefSize(100, 100);

                        Button generateQuizButton = new Button("Générer Quiz");
                        generateQuizButton.getStyleClass().add("generate-quiz-button");

                        Button showAnswersButton = new Button("Afficher Réponses");
                        showAnswersButton.getStyleClass().add("show-answers-button");
                        showAnswersButton.setVisible(false);

                        Button downloadPdfButton = new Button("Télécharger en PDF");
                        downloadPdfButton.getStyleClass().add("download-pdf-button");
                        downloadPdfButton.setVisible(false);

                        ScrollPane scrollPane = new ScrollPane();
                        scrollPane.setFitToWidth(true);
                        scrollPane.setPrefHeight(450);
                        scrollPane.setVisible(false);

                        VBox questionsBox = new VBox(10);
                        questionsBox.setAlignment(Pos.CENTER);
                        scrollPane.setContent(questionsBox);

                        TextArea answersArea = new TextArea();
                        answersArea.setVisible(false);
                        answersArea.setEditable(false);

                        // Centered button container
                        HBox buttonContainer = new HBox(15);
                        buttonContainer.setAlignment(Pos.CENTER);
                        buttonContainer.getChildren().addAll(generateQuizButton, showAnswersButton, downloadPdfButton);

                        generateQuizButton.setOnAction(event -> {
                            generateQuizButton.setVisible(false);
                            loadingIndicator.setVisible(true);

                            Task<Void> quizGenerationTask = new Task<>() {
                                @Override
                                protected Void call() throws Exception {
                                    quiz = quizHandler.generateQuiz(extractedText);
                                    return null;
                                }

                                @Override
                                protected void succeeded() {
                                    Platform.runLater(() -> {
                                        loadingIndicator.setVisible(false);
                                        processQuizGeneration(quiz, questionsBox, showAnswersButton, scrollPane);
                                    });
                                }

                                @Override
                                protected void failed() {
                                    Platform.runLater(() -> {
                                        loadingIndicator.setVisible(false);
                                        showAlert(Alert.AlertType.ERROR, "Erreur lors de la génération du quiz : "
                                                + getException().getMessage());
                                        generateQuizButton.setVisible(true);
                                    });
                                }
                            };

                            new Thread(quizGenerationTask).start();
                        });

                        showAnswersButton.setOnAction(event -> {
                            // Disable the button and show loading indicator
                            showAnswersButton.setVisible(false);
                            loadingIndicator.setVisible(true);

                            // Create a task to generate answers
                            Task<Void> answersGenerationTask = new Task<>() {
                                @Override
                                protected Void call() throws Exception {
                                    // Generate answers using the same extracted text used for quiz generation
                                    String answers = quizHandler.generateAnswers(extractedText, quiz);

                                    // Update UI on JavaFX Application Thread
                                    Platform.runLater(() -> {
                                        answersArea.setText(answers);
                                        answersArea.setVisible(true);
                                        loadingIndicator.setVisible(false);
                                    });
                                    return null;
                                }

                                @Override
                                protected void failed() {
                                    Platform.runLater(() -> {
                                        loadingIndicator.setVisible(false);
                                        showAlert(Alert.AlertType.ERROR, "Erreur lors de la génération des réponses : "
                                                + getException().getMessage());
                                        showAnswersButton.setVisible(true);
                                    });
                                }
                            };

                            // Run the task in a new thread
                            new Thread(answersGenerationTask).start();
                        });

                        // Add components to the main container with center alignment
                        mainContainer.getChildren().addAll(
                                titleLabel,
                                loadingIndicator,
                                buttonContainer,
                                scrollPane,
                                answersArea);

                        Scene scene = new Scene(mainContainer, 600, 800);
                        scene.getStylesheets().add(cssPath);

                        quizStage.setScene(scene);
                        quizStage.setMaximized(true);
                        quizStage.show();
                    });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la préparation de l'application : " + e.getMessage());
        }
    }

    private void processQuizGeneration(String quiz, VBox questionsBox, Button showAnswersButton,
            ScrollPane scrollPane) {
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

        displayAllQuestions(questionsBox);
        scrollPane.setVisible(true);
        showAnswersButton.setVisible(true);
    }

    private void displayAllQuestions(VBox questionsBox) {
        questionsBox.getChildren().clear();

        for (String[] questionWithChoices : questionsWithChoices) {
            if (questionWithChoices.length == 0 || questionWithChoices[0] == null || questionWithChoices[0].isEmpty()) {
                System.out.println("Question vide ou mal formatée détectée !");
                continue;
            }

            Label questionLabel = new Label(questionWithChoices[0]);
            questionLabel.getStyleClass().add("quiz-question-label");

            VBox questionVBox = new VBox(5);
            questionVBox.getChildren().add(questionLabel);

            VBox choicesBox = new VBox(5);
            choicesBox.setPadding(new Insets(10, 0, 10, 20));

            ToggleGroup toggleGroup = new ToggleGroup();

            for (int i = 1; i < questionWithChoices.length; i++) {
                if (questionWithChoices[i] != null && !questionWithChoices[i].isEmpty()) {
                    RadioButton choiceButton = new RadioButton(questionWithChoices[i]);
                    choiceButton.getStyleClass().add("quiz-choice-radio");
                    choiceButton.setToggleGroup(toggleGroup);
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