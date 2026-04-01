package com.yellowinsurance.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClaimsApplication {

    // ISSUE: Hardcoded admin credentials used for bootstrapping
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123!";
    public static final String API_SECRET_KEY = "sk-yellow-insurance-2024-prod-key-do-not-share";

    public static void main(String[] args) {
        SpringApplication.run(ClaimsApplication.class, args);
    }
}
