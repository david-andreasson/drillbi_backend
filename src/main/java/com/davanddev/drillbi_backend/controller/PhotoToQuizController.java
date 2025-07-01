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
@RequestMapping("/api/v2/phototoquiz")
public class PhotoToQuizController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final String[] ALLOWED_TYPES = {"image/png", "image/jpeg"};

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractTextFromImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file received.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File is too large (max 5 MB).");
        }
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(file.getContentType())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return ResponseEntity.badRequest().body("Invalid file type. Only PNG and JPG/JPEG are supported.");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("Could not read image.");
            }
            Tesseract tesseract = new Tesseract();
            String tessdataPrefix = System.getenv("TESSDATA_PREFIX");
            String datapath;
            if (tessdataPrefix != null && !tessdataPrefix.isEmpty()) {
                datapath = tessdataPrefix;
            } else {
                datapath = "/usr/share/tessdata";
            }
            tesseract.setDatapath(datapath);
            tesseract.setLanguage("swe+eng"); // Swedish and English

            // Check that language files exist
            java.io.File sweFile = new java.io.File(datapath, "swe.traineddata");
            java.io.File engFile = new java.io.File(datapath, "eng.traineddata");
            if (!sweFile.exists() || !engFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("OCR error: Could not find swe.traineddata or eng.traineddata in tessdata directory (" + datapath + ")");
            }

            String text = tesseract.doOCR(image);
            return ResponseEntity.ok().body(text);
        } catch (TesseractException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OCR-fel: " + e.getMessage());
        }
    }
}
