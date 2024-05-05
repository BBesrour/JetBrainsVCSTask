package com.example.jetbrainsvcstask.controller;

import com.example.jetbrainsvcstask.domain.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

@RestController()
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private SimpMessagingTemplate template;


    @PostMapping()
    public ResponseEntity<Void> push(@RequestHeader("X-GitHub-Hook-ID") String eventID, @RequestHeader("X-GitHub-Event") String eventType, @RequestHeader(required = false) String signature, @RequestBody String payload) {
        // validate the signature
        try {
            // Check if the signature is present and valid
            // For testing purposes, we allow the signature to be empty
            if ( signature != null && !signature.isEmpty() && !validateSignature(payload, signature)) {
                logger.warn("Invalid signature");
                return ResponseEntity.badRequest().build();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error validating signature", e);
            return ResponseEntity.badRequest().build();
        }

        // Start a new thread to process the payload
        new Thread(() -> {
            // Send the event to the WebSocket
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            WebhookEvent event = new WebhookEvent(eventType, eventID, formatter.format(date));
            this.template.convertAndSend("/topic/webhookEvent", event);
        }).start();

        return ResponseEntity.accepted().build();
    }

    // Calculate and validate the signature
    private boolean validateSignature(String payload, String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        String secret = "randomString";
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String hashHex = toHexString(hash);
        String trusted = "sha256=" + hashHex;

        logger.info("Calculated signature: {}", trusted);
        logger.info("Received signature: {}", signature);

        return MessageDigest.isEqual(trusted.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
