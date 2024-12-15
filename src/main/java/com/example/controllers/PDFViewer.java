package com.example.controllers;

import com.example.controllers.ReadingTracker.PageReadingState;
import com.example.services.CourseService;
import com.mongodb.client.MongoDatabase;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
// import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class PDFViewer {
    private ReadingTracker readingTracker;
    private ScrollPane scrollPane;
    private VBox pagesContainer;
    private PDDocument document;
    private Stage pdfStage;
    private MongoDatabase database;
    private String studentId;
    private String courseId;
    private CourseService courseService;

    public ReadingTracker getReadingTracker() {
        return readingTracker;
    }

    // Main method to display PDF with integrated tracking
    public void display(String pdfPath, String studentId, String courseId, MongoDatabase database,
            Consumer<String> onProcessComplete) {
        try {
            // Store context for progress tracking
            this.studentId = studentId;
            this.courseId = courseId;
            this.database = database;
            this.courseService = new CourseService(database);

            pdfStage = new Stage();
            pdfStage.setTitle("PDF Viewer with Reading Tracker");

            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Fichier PDF non trouvé");
                return;
            }

            // Use try-with-resources to ensure proper document closure
            try (PDDocument loadedDocument = PDDocument.load(pdfFile)) {
                this.document = loadedDocument;
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int totalPages = document.getNumberOfPages();

                // Initialize reading tracker with total number of pages
                readingTracker = new ReadingTracker(document.getNumberOfPages());

                // Create pages container
                pagesContainer = new VBox(10);
                pagesContainer.setPadding(new Insets(10));

                // Collect all page texts for quiz generation
                StringBuilder allPageText = new StringBuilder();

                // Render PDF pages
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    // Extract text from the page
                    String pageText = extractTextFromPage(pageIndex);
                    allPageText.append(pageText).append("\n");
                    System.out.println("Page " + (pageIndex + 1) + " Text Length: " + pageText.length());
                    System.out.println("--------------------text ga3  = " + pageText);

                    // Render page image
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 72);
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(800);

                    // Tag each image view with its page number for tracking
                    imageView.setUserData(pageIndex + 1);
                    readingTracker.setPageText(pageIndex + 1, pageText);

                    pagesContainer.getChildren().add(imageView);
                }

                // Create scrollpane with page container
                scrollPane = new ScrollPane(pagesContainer);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: white;");

                // Setup progress tracking on scroll
                setupScrollTracking(totalPages);

                // Create scene and stage
                Scene scene = new Scene(scrollPane, 850, 600);
                pdfStage.setScene(scene);
                pdfStage.show();

                // Set up closing event to process reading progress
                setupCloseEvent(allPageText, onProcessComplete);
                pdfStage.setOnCloseRequest(event -> {
                    try {
                        int currentPage = calculateCurrentPage();
                        courseService.updateCourseProgress(studentId, courseId, currentPage, totalPages);

                    } catch (Exception e) {
                        System.err.println("Error updating course progress: " + e.getMessage());
                    }

                    // Prevent default close operation
                    event.consume();

                    int currentPage = calculateCurrentPage();
                    Map<Integer, Set<String>> keywords = readingTracker.extractKeywordsFromCompletedPages(currentPage);

                    // Debug prints
                    System.out.println("Total Pages: " + readingTracker.getTotalPages());
                    System.out.println("Current Page States: " + readingTracker.getPageStates());
                    // System.out.println("Completed Pages: " + completedPages);
                    System.out.println("Page Keywords: " + keywords);
                    System.out.println("Current Page: " + currentPage);

                    // Pass both extracted text and keywords to the callback
                    if (onProcessComplete != null) {
                        onProcessComplete.accept(allPageText.toString());
                    }

                    // Ensure document is closed
                    try {
                        if (document != null) {
                            document.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Explicitly close the stage
                    pdfStage.close();

                });

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Impossible de charger le PDF: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la préparation de l'application : " + e.getMessage());
        }
    }

    private String extractTextFromPage(int pageIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        return stripper.getText(document);
    }

    // Setup scroll tracking mechanism
    private void setupScrollTracking(int totalPages) {
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            int currentPage = calculateCurrentPage();
            double scrollPercentage = calculateScrollPercentage();

            // Debug prints
            System.out.println("Tracking - Page: " + currentPage + ", Scroll %: " + scrollPercentage);

            // Update progress when scroll is significant
            if (scrollPercentage > 0.5) {
                courseService.updateCourseProgress(
                        studentId,
                        courseId,
                        currentPage,
                        totalPages);
                // Track page reading state
                readingTracker.trackPageReading(currentPage, scrollPercentage);

            }

            // Ensure page tracking starts when scrolling
            PageReadingState state = readingTracker.getPageStates().get(currentPage);
            if (state != null) {
                // Start tracking if not already started
                if (state.getStartTime() == null) {
                    readingTracker.trackPageReading(currentPage, scrollPercentage);
                }
            }

            // Optional: Print tracking info
            System.out.println("Page: " + currentPage + ", Scroll: " + scrollPercentage);
        });
    }

    /**
     * Setup document close event to save final reading progress
     * 
     * @param allPageText       Accumulated text from all pages
     * @param onProcessComplete Callback for processing completion
     */
    private void setupCloseEvent(
            StringBuilder allPageText,
            Consumer<String> onProcessComplete) {
        pdfStage.setOnCloseRequest(event -> {
            try {
                // Get final reading progress
                Map<String, Object> progressDetails = readingTracker.getReadingProgress();
                int currentPage = (int) progressDetails.get("currentPage");
                int totalPages = (int) progressDetails.get("totalPages");

                // Update final course progress
                courseService.updateCourseProgress(
                        studentId,
                        courseId,
                        currentPage,
                        totalPages);

                // Call process complete callback with extracted text
                if (onProcessComplete != null) {
                    onProcessComplete.accept(allPageText.toString());
                }

                // Close document
                if (document != null) {
                    document.close();
                }

                // Close stage
                pdfStage.close();

            } catch (Exception e) {
                System.err.println("Error updating course progress: " + e.getMessage());
            }
        });
    }

    // Calculate current page based on scroll position
    private int calculateCurrentPage() {
        double scrollPosition = scrollPane.getVvalue();
        int totalPages = pagesContainer.getChildren().size();

        // Calculate the current page based on the scroll position
        int calculatedPage = Math.min(
                Math.max(1, (int) Math.ceil(scrollPosition * totalPages)),
                totalPages);

        System.out.println("Scroll Position: " + scrollPosition +
                ", Total Pages: " + totalPages +
                ", Calculated Page: " + calculatedPage);

        // Simple proportional calculation
        return calculatedPage;
    }

    // Calculate scroll percentage of current visible area
    private double calculateScrollPercentage() {
        Bounds viewportBounds = scrollPane.getViewportBounds();
        Bounds contentBounds = pagesContainer.getBoundsInParent();

        // Calculate percentage of content scrolled
        double totalContentHeight = contentBounds.getHeight();
        double viewportHeight = viewportBounds.getHeight();
        double scrollProgress = scrollPane.getVvalue();

        System.out.println("Viewport Height: " + viewportHeight +
                ", Content Height: " + totalContentHeight +
                ", Scroll Progress: " + scrollProgress);

        return Math.min(1.0, Math.max(0.0, scrollProgress + (viewportHeight / totalContentHeight)));
    }

    // Utility method to show alerts
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}