package com.example.controllers;

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

import java.awt.image.BufferedImage;
import java.io.File;
public class PDFViewer {

    // Méthode principale pour afficher un fichier PDF
    public static void display(String pdfPath) {
        Stage pdfStage = new Stage(); // Crée une nouvelle fenêtre (stage) pour afficher le PDF
        pdfStage.setTitle("PDF Viewer"); // Définit le titre de la fenêtre

        try {
            File pdfFile = new File(pdfPath); // Charge le fichier PDF à partir du chemin spécifié
            if (pdfFile.exists()) { // Vérifie si le fichier existe
                PDDocument document = PDDocument.load(pdfFile); // Charge le document PDF
                PDFRenderer pdfRenderer = new PDFRenderer(document); // Initialise un moteur de rendu pour le PDF

                VBox pagesContainer = new VBox(10); // Conteneur vertical pour les pages PDF
                pagesContainer.setPadding(new Insets(10)); // Définit les marges internes

                // Boucle pour parcourir et afficher toutes les pages du PDF
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 72); // Rend la page en tant qu'image
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null); // Convertit l'image en format compatible avec JavaFX
                    ImageView imageView = new ImageView(image); // Crée un composant d'affichage pour l'image

                    imageView.setPreserveRatio(true); // Maintient les proportions de l'image
                    imageView.setFitWidth(800); // Ajuste la largeur de l'image
                    pagesContainer.getChildren().add(imageView); // Ajoute l'image de la page au conteneur
                }

                ScrollPane scrollPane = new ScrollPane(pagesContainer); // Ajoute un défilement pour le conteneur
                scrollPane.setFitToWidth(true); // Ajuste automatiquement la largeur
                scrollPane.setStyle("-fx-background-color: white;"); // Définit un style de fond blanc

                Scene scene = new Scene(scrollPane, 850, 600); // Crée une scène avec le contenu et une taille initiale
                pdfStage.setScene(scene); // Associe la scène au stage
                pdfStage.show(); // Affiche la fenêtre

                document.close(); // Ferme le document pour libérer les ressources
            } else {
                // Affiche une alerte si le fichier PDF n'existe pas
                showAlert("Erreur de fichier", "Le fichier PDF n'existe pas à l'emplacement spécifié.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Affiche la pile d'erreurs dans la console
            // Affiche une alerte en cas d'erreur de chargement
            showAlert("Erreur", "Impossible de charger le PDF.");
        }
    }

    // Méthode utilitaire pour afficher une alerte
    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Crée une alerte de type erreur
        alert.setTitle(title); // Définit le titre de l'alerte
        alert.setHeaderText(null); // Supprime l'en-tête
        alert.setContentText(content); // Définit le contenu du message d'erreur
        alert.showAndWait(); // Affiche l'alerte et attend la confirmation de l'utilisateur
    }
}
