package com.davanddev.drillbi_backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromptService {

    private final Map<String, String> promptMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prompts/quiz-prompts.xml");
            if (inputStream == null) {
                throw new IllegalStateException("Missing quiz-prompts.xml in resources folder");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList promptNodes = doc.getElementsByTagName("prompt");

            for (int i = 0; i < promptNodes.getLength(); i++) {
                Element promptElement = (Element) promptNodes.item(i);
                String id = promptElement.getAttribute("id");
                String language = promptElement.getAttribute("language");

                if (id.isBlank() || language.isBlank()) {
                    continue;
                }

                String key = generateKey(id, language);
                String content = promptElement.getTextContent().trim();
                promptMap.put(key, content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompts from quiz-prompts.xml", e);
        }
    }

    public String getPromptOrThrow(String id, String language) {
        String key = generateKey(id, language);
        String prompt = promptMap.get(key);

        if (prompt == null) {
            throw new IllegalArgumentException("Missing prompt for id: " + id + " and language: " + language);
        }

        return prompt;
    }

    public String getPromptById(String id, String language, Object... args) {
        String prompt = getPromptOrThrow(id, language);
        if (args != null && args.length > 0) {
            return String.format(prompt, args);
        }
        return prompt;
    }

    private String generateKey(String id, String language) {
        return id + ":" + language;
    }

}