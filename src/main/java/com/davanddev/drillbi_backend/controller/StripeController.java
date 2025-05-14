package com.davanddev.drillbi_backend.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/stripe")
public class StripeController {

    // Stripe secret key loaded from environment variable via application.properties indirection
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final com.davanddev.drillbi_backend.repository.UserRepository userRepository;

    public StripeController(com.davanddev.drillbi_backend.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a Stripe Checkout session for the authenticated user.
     * Uses the user's email from the JWT principal.
     */
    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) throws StripeException {
        // Set Stripe API key
        Stripe.apiKey = stripeSecretKey;

        // Get authenticated user's email from JWT
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            throw new IllegalArgumentException("Authenticated user must have an email claim in JWT.");
        }

        // Look up user in database
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found in database for email: " + email);
        }
        var user = userOpt.get();

        String priceId = "price_1RNV4mFT8IcQLrR6nCXbfcaq";

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl("https://drillbi.se/profil?success=true&session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl("https://drillbi.se/profil?canceled=true")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPrice(priceId)
                                        .build())
                        .setCustomerEmail(email)
                        .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        return response;
    }

    /**
     * Stripe webhook endpoint to handle subscription events.
     * Verifies signature and updates user isPremium status on successful payment.
     */
    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostMapping("/webhook")
    public String handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            return "Invalid signature";
        }

        // Handle the event
        if ("checkout.session.completed".equals(event.getType()) || "invoice.paid".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String customerEmail = session.getCustomerDetails() != null ? session.getCustomerDetails().getEmail() : null;
                if (customerEmail != null) {
                    var userOpt = userRepository.findByEmail(customerEmail);
                    if (userOpt.isPresent()) {
                        var user = userOpt.get();
                        user.setPremium(true);
                        userRepository.save(user);
                    }
                }
            }
        }
        return "ok";
    }
}

