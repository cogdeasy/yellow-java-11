package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.Document;
import com.yellowinsurance.claims.repository.DocumentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private static final Logger logger = LogManager.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir;

    /**
     * VULNERABILITY: Path traversal - no sanitization of filename
     * An attacker could upload a file with name "../../etc/passwd" or similar
     */
    public Document uploadDocument(Long claimId, Long customerId, MultipartFile file, String uploadedBy) {
        try {
            // VULNERABILITY: No path sanitization - path traversal attack
            String fileName = file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir, fileName);

            // VULNERABILITY: No file type validation
            // VULNERABILITY: No file size check
            Files.createDirectories(uploadPath.getParent());
            file.transferTo(uploadPath.toFile());

            Document doc = new Document();
            doc.setClaimId(claimId);
            doc.setCustomerId(customerId);
            doc.setFileName(fileName);
            doc.setFilePath(uploadPath.toString()); // VULNERABILITY: Storing absolute path
            doc.setContentType(file.getContentType());
            doc.setFileSize(file.getSize());
            doc.setUploadedBy(uploadedBy);
            doc.setCreatedAt(LocalDateTime.now());

            logger.info("Document uploaded: " + fileName + " by " + uploadedBy);

            return documentRepository.save(doc);
        } catch (Exception e) {
            // ISSUE: Catch-all exception handling
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    /**
     * VULNERABILITY: Path traversal in file download
     * No verification that the file path is within the upload directory
     */
    public byte[] downloadDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        try {
            // VULNERABILITY: Reading arbitrary file paths
            Path filePath = Paths.get(doc.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document: " + e.getMessage(), e);
        }
    }

    /**
     * VULNERABILITY: SSRF - fetching arbitrary URLs
     * An attacker could use this to probe internal network
     */
    public Document importFromUrl(Long claimId, Long customerId, String documentUrl, String uploadedBy) {
        try {
            // VULNERABILITY: SSRF - no URL validation, could access internal services
            URL url = new URL(documentUrl);

            // VULNERABILITY: Resource leak - stream not properly closed in all cases
            InputStream inputStream = url.openStream();
            String fileName = documentUrl.substring(documentUrl.lastIndexOf('/') + 1);
            Path uploadPath = Paths.get(uploadDir, fileName);

            Files.createDirectories(uploadPath.getParent());

            // VULNERABILITY: Resource leak - OutputStream not in try-with-resources
            FileOutputStream outputStream = new FileOutputStream(uploadPath.toFile());
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalSize = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalSize += bytesRead;
            }
            outputStream.close();
            inputStream.close();

            Document doc = new Document();
            doc.setClaimId(claimId);
            doc.setCustomerId(customerId);
            doc.setFileName(fileName);
            doc.setFilePath(uploadPath.toString());
            doc.setContentType("application/octet-stream");
            doc.setFileSize(totalSize);
            doc.setUploadedBy(uploadedBy);
            doc.setCreatedAt(LocalDateTime.now());

            return documentRepository.save(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import document from URL: " + e.getMessage(), e);
        }
    }

    public List<Document> getDocumentsByClaimId(Long claimId) {
        return documentRepository.findByClaimId(claimId);
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * VULNERABILITY: No authorization check - any user can delete any document
     */
    public void deleteDocument(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        try {
            // VULNERABILITY: Deleting file at arbitrary path
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            logger.error("Failed to delete file: " + doc.getFilePath(), e);
        }

        documentRepository.delete(doc);
    }
}
