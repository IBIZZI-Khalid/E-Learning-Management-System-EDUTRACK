package com.example.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

public class PdfHandler {

    public String extractTextWithStopPage(File file) throws Exception {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();

            int totalPages = document.getNumberOfPages();
            int stopPage = askStopPage(totalPages);

            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(stopPage);

            return pdfStripper.getText(document);
        }
    }

    private int askStopPage(int totalPages) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(totalPages));
        dialog.setTitle("Choix de la page");
        dialog.setHeaderText("Entrez le numéro de la dernière page à lire pour passer un Quiz :");
        dialog.setContentText("Numéro de page (1-" + totalPages + "):");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        if (cancelButton != null) {
            cancelButton.setText("Pas maintenant");
        }

        String result = dialog.showAndWait().orElse(null);

        if (result == null) {
            return -1;
        }

        try {
            int page = Integer.parseInt(result);
            return Math.min(Math.max(1, page), totalPages);
        } catch (NumberFormatException e) {
            return totalPages;
        }
    }
}