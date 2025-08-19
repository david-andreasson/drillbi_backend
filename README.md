# Drillbi – Backend (Spring Boot)

**Drillbi** är ett Java-baserat REST-API byggt med Spring Boot för att ge mig och mina kurskamrater ett enkelt sätt att plugga inför tentor.  
API:et driver en quiz-applikation som stödjer flera kurser genom att tillhandahålla ändpunkter för att hämta frågor, hantera quiz-sessioner, skicka in svar och autentisera användare via Google OAuth2.

---

## Frontend

En React-baserad frontend finns i ett separat repo: [drillbi_frontend](https://github.com/david-andreasson/drillbi_frontend)  

Den kommunicerar med detta API och levererar hela quiz-upplevelsen.

---

## Live

Tjänsten finns tillgänglig på: [https://drillbi.se](https://drillbi.se)

---

## Funktioner

- Hämta quizfrågor för kurs (ordning: `ORDER`, `RANDOM`, `REVERSE`).  
- Hantera quiz-sessioner: starta, hämta nästa fråga, skicka svar, statistik (poäng/felprocent).  
- Användarautentisering via Google OAuth2; nya användare skapas och lagras i **H2**.  
- **JWT** genereras via dedikerad `TokenService` och används för efterföljande anrop.  
- Loggning med **SLF4J/Logback** och global exception-handler (`@ControllerAdvice`).  

---

## Integrationer

- **Google OAuth2 → JWT** – inloggning i frontend, token utfärdas av backend.  
- **AI (OpenAI & Anthropic/Claude)** – genererar frågor och förklaringar.  
- **OCR (Tesseract)** – *"foto-till-quiz"*: text extraheras ur bild (sv/eng) och används som frågeunderlag.  
- **Stripe (Checkout & webhook)** – premium/prenumeration uppdateras via webhook.  
- **Databas**: H2 används i projektet.  

---

## Teknik i korthet

- **Java 21**  
- **Spring Boot** (Web, Security, JPA)  
- **Maven**  
- **H2**  
- **JUnit**  

---

## Licens

Detta projekt är **inte open source**. *All rights reserved.*  
Koden är publik enbart för visning i utbildnings-/portföljsyfte.  

Ingen användning, distribution eller modifiering är tillåten utan skriftligt tillstånd.  
Se `LICENSE` för fullständig text.
