package com.davanddev.drillbi_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@RequestMapping("/api/phototoquiz")
public class PhotoToQuizController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final String[] ALLOWED_TYPES = {"image/png", "image/jpeg"};

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractTextFromImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Ingen fil mottagen.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("Filen är för stor (max 5 MB).");
        }
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(file.getContentType())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return ResponseEntity.badRequest().body("Fel filtyp. Endast PNG och JPG/JPEG stöds.");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("Kunde inte läsa bilden.");
            }
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("tessdata"); // Förutsätter att tessdata ligger i rooten eller konfigurerad path
            tesseract.setLanguage("swe+eng"); // Svenska och engelska
            String text = tesseract.doOCR(image);
            return ResponseEntity.ok().body(text);
        } catch (TesseractException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OCR-fel: " + e.getMessage());
        }
    }
}
