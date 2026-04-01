package com.yellowinsurance.claims.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * VULNERABILITIES:
 * - Path traversal in file operations
 * - Zip slip vulnerability in extract
 * - Resource leaks
 * - No file type validation
 */
public class FileUtils {

    // VULNERABILITY: Hardcoded temporary directory
    private static final String TEMP_DIR = "/tmp/yellow-insurance";

    /**
     * VULNERABILITY: Path traversal - no validation of filename
     */
    public static String saveFile(String fileName, byte[] content) throws IOException {
        // VULNERABILITY: No sanitization of fileName - path traversal possible
        Path path = Paths.get(TEMP_DIR, fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, content);
        return path.toString();
    }

    /**
     * VULNERABILITY: Reading arbitrary files from filesystem
     */
    public static byte[] readFile(String filePath) throws IOException {
        // VULNERABILITY: No path validation - can read any file
        return Files.readAllBytes(Paths.get(filePath));
    }

    /**
     * VULNERABILITY: Zip Slip - malicious ZIP entry names can write outside target directory
     * Example: entry name "../../etc/cron.d/malicious" would write to /etc/cron.d/
     */
    public static void extractZip(InputStream zipStream, String targetDir) throws IOException {
        // VULNERABILITY: Resource leak - ZipInputStream not in try-with-resources
        ZipInputStream zis = new ZipInputStream(zipStream);
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            // VULNERABILITY: Zip Slip - entry.getName() not validated against targetDir
            File outputFile = new File(targetDir, entry.getName());

            if (entry.isDirectory()) {
                outputFile.mkdirs();
            } else {
                outputFile.getParentFile().mkdirs();
                // VULNERABILITY: Resource leak
                FileOutputStream fos = new FileOutputStream(outputFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }

    /**
     * ISSUE: Insecure temporary file creation
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        // ISSUE: Using java.io.File.createTempFile without secure permissions
        File tempFile = File.createTempFile(prefix, suffix);
        // ISSUE: Not setting restrictive permissions
        // ISSUE: File may be readable by other users on shared system
        return tempFile;
    }
}
