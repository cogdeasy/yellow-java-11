package com.yellowinsurance.claims.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;

/**
 * ISSUES:
 * - Hardcoded SMTP credentials
 * - No actual email sending implementation (stub)
 * - Insecure deserialization of notification templates
 * - Resource leaks
 */
@Service
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    // VULNERABILITY: Hardcoded email credentials
    private static final String SMTP_HOST = "smtp.yellowinsurance.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "notifications@yellowinsurance.com";
    private static final String SMTP_PASSWORD = "NotifyP@ss2024!";

    // VULNERABILITY: Hardcoded API key for SMS service
    private static final String TWILIO_ACCOUNT_SID = "FAKE_TWILIO_SID_REPLACE_ME";
    private static final String TWILIO_AUTH_TOKEN = "FAKE_TWILIO_TOKEN_REPLACE_ME";

    public void sendClaimNotification(String email, String claimNumber, String status) {
        // VULNERABILITY: Log4Shell vector - user-controlled email logged
        logger.info("Sending notification to: " + email + " for claim: " + claimNumber);

        String subject = "Claim " + claimNumber + " Status Update";
        String body = "Your claim " + claimNumber + " has been updated to: " + status;

        // ISSUE: Stub implementation - email not actually sent
        logger.info("Email would be sent - Subject: " + subject);
    }

    /**
     * VULNERABILITY: Insecure deserialization
     * Loading serialized template objects from disk
     */
    public Object loadNotificationTemplate(String templateName) {
        try {
            // VULNERABILITY: Insecure deserialization of arbitrary objects
            String templatePath = "/templates/" + templateName + ".ser";
            FileInputStream fis = new FileInputStream(templatePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object template = ois.readObject();
            ois.close();
            fis.close();
            return template;
        } catch (Exception e) {
            logger.error("Failed to load template: " + templateName, e);
            return null;
        }
    }

    /**
     * VULNERABILITY: Insecure deserialization from byte array
     */
    public Object deserializeNotification(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            logger.error("Failed to deserialize notification", e);
            return null;
        }
    }
}
