package com.example.controllers;

import com.example.controllers.ReadingTracker.PageReadingState;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PDFViewer {
    private ReadingTracker readingTracker;
    private ScrollPane scrollPane;
    private VBox pagesContainer;
    private PDDocument document;
    private Stage pdfStage;  // Add a stage field to manage the window

    public ReadingTracker getReadingTracker() {
        return readingTracker;
    }
    // Main method to display PDF with integrated tracking
    public static void display(String pdfPath, Runnable onCloseAction) {
        PDFViewer viewer = new PDFViewer();
        viewer.initializePDFViewer(pdfPath, onCloseAction);
    }

    private void initializePDFViewer(String pdfPath, Runnable onCloseAction) {
        Stage pdfStage = new Stage();
        pdfStage.setTitle("PDF Viewer with Reading Tracker");

        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Fichier PDF non trouvé");
                return;
            }

            // Load PDF document
            document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Initialize reading tracker with total number of pages
            readingTracker = new ReadingTracker(document.getNumberOfPages());

            // Create pages container
            pagesContainer = new VBox(10);
            pagesContainer.setPadding(new Insets(10));

            // Render PDF pages
            renderPDFPages(pdfRenderer);

            // Create scrollpane with page container
            scrollPane = new ScrollPane(pagesContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: white;");

            // Add scroll listener for tracking
            setupScrollTracking();

            // Create scene and stage
            Scene scene = new Scene(scrollPane, 850, 600);
            pdfStage.setScene(scene);
            pdfStage.show();

            // Set up closing event to process reading progress
            pdfStage.setOnHiding(event -> {
                processReadingProgress();

                if (onCloseAction != null) {
                    onCloseAction.run();
                }

                // Close PDF document
                try {
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Impossible de charger le PDF.");
        }
    }

    // Render all PDF pages as images
    private void renderPDFPages(PDFRenderer pdfRenderer) throws Exception {
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            String pageText = extractTextFromPage(pageIndex);
            System.out.println("Page " + (pageIndex + 1) + " Text Length: " + pageText.length());
        
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
    }

    private String extractTextFromPage(int pageIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        return stripper.getText(document);
    }

    // Setup scroll tracking mechanism
    private void setupScrollTracking() {
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            int currentPage = calculateCurrentPage();
            double scrollPercentage = calculateScrollPercentage();

            // Debug prints
            System.out.println("Tracking - Page: " + currentPage + ", Scroll %: " + scrollPercentage);

            // Ensure page tracking starts when scrolling
            PageReadingState state = readingTracker.getPageStates().get(currentPage);
            if (state != null) {
                // Start tracking if not already started
                if (state.getStartTime() == null) {
                    readingTracker.trackPageReading(currentPage, scrollPercentage);
                }
            }
            
            // Update reading tracker
            readingTracker.trackPageReading(currentPage, scrollPercentage);
            
            // Optional: Print tracking info
            System.out.println("Page: " + currentPage + ", Scroll: " + scrollPercentage);
        });
    }

    // Calculate current page based on scroll position
    private int calculateCurrentPage() {
        double scrollPosition = scrollPane.getVvalue();
        int totalPages = pagesContainer.getChildren().size();

        // Calculate the current page based on the scroll position
        int calculatedPage = Math.min(
            Math.max(1, (int) Math.ceil(scrollPosition * totalPages)),
            totalPages
        );

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

    // Process reading progress when PDF viewer closes
    private void processReadingProgress() {
        int currentPage = calculateCurrentPage();
        // Set<Integer> completedPages = new HashSet<>();
    
        // for (int i = 1; i <= currentPage; i++) {
        //     completedPages.add(i);
        // }
        Map<Integer, Set<String>> keywords = readingTracker.extractKeywordsFromCompletedPages(currentPage);
    
        // Debug prints
        System.out.println("Total Pages: " + readingTracker.getTotalPages());
        System.out.println("Current Page States: " + readingTracker.getPageStates());
        // System.out.println("Completed Pages: " + completedPages);
        System.out.println("Page Keywords: " + keywords);
        System.out.println("Current Page: " + currentPage);

    
        // Ensure document is closed
        try {
            if (document != null) {
                document.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Utility method to show alerts
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}