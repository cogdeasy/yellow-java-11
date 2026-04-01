package com.yellowinsurance.claims.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * VULNERABILITIES IN THIS CLASS:
 * - Using MD5 for password hashing (broken, no salt)
 * - Using DES encryption (broken, key too short)
 * - Hardcoded encryption key
 * - No IV for encryption
 * - Using ECB mode (deterministic encryption)
 */
public class EncryptionUtils {

    // VULNERABILITY: Hardcoded encryption key
    private static final String ENCRYPTION_KEY = "YellowK1";  // 8 bytes for DES
    // VULNERABILITY: Another hardcoded secret
    private static final String AES_KEY = "YellowInsurance!"; // 16 bytes for AES

    /**
     * VULNERABILITY: MD5 is cryptographically broken for password hashing
     * Should use bcrypt, scrypt, or Argon2
     * No salt used
     */
    public static String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    /**
     * VULNERABILITY: DES is broken and insecure
     * ECB mode is deterministic (identical plaintext = identical ciphertext)
     * No IV used
     */
    public static String encryptDES(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "DES");
            // VULNERABILITY: ECB mode
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * VULNERABILITY: DES decryption with hardcoded key
     */
    public static String decryptDES(String encryptedText) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * VULNERABILITY: SHA-1 is deprecated for security purposes
     */
    public static String hashSHA1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
}
