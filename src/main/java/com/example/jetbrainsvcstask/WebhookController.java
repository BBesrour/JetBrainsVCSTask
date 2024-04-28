package com.example.jetbrainsvcstask;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;

@RestController()
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);


    @PostMapping("/push")
    public ResponseEntity<Void> push(@RequestHeader("X-GitHub-Event") String eventType, @RequestHeader("X-Hub-Signature-256") String signature, @RequestBody String payload) {
        // validate the signature
        try {
            if (!validateSignature(payload, signature)) {
                logger.warn("Invalid signature");
                return ResponseEntity.badRequest().build();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error validating signature", e);
            return ResponseEntity.badRequest().build();
        }

        // Start a new thread to process the payload
        new Thread(() -> {
            JSONObject jsonPayload = new JSONObject(payload);
            // Check if the event is a push event
            if (eventType.equals("push")) {
                System.out.println("Push event detected");
                // Get the repository name
                String repositoryName = jsonPayload.getJSONObject("repository").getString("name");
                System.out.println("Repository name: " + repositoryName);
                // Print commit count
                JSONArray commits = jsonPayload.getJSONArray("commits");
                System.out.println("Number of commits: " + commits.length());
                // Print commit details
                for (int i = 0; i < commits.length(); i++) {
                    JSONObject commit = commits.getJSONObject(i);
                    String commitMessage = commit.getString("message");
                    String commitAuthor = commit.getJSONObject("author").getString("name");
                    System.out.println("Commit " + (i + 1) + ": " + commitMessage + " by " + commitAuthor);
                }
            }
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
