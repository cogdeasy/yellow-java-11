package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.Document;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @RequestParam Long claimId,
            @RequestParam Long customerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "anonymous") String uploadedBy) {
        // VULNERABILITY: No file type restriction, no size limit
        Document doc = documentService.uploadDocument(claimId, customerId, file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(doc));
    }

    /**
     * VULNERABILITY: Path traversal in download - reads arbitrary files
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> docOpt = documentService.getDocumentById(id);
        if (!docOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Document doc = docOpt.get();
        byte[] content = documentService.downloadDocument(id);

        HttpHeaders headers = new HttpHeaders();
        // VULNERABILITY: XSS via Content-Disposition header - filename not sanitized
        headers.setContentDispositionFormData("attachment", doc.getFileName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * VULNERABILITY: SSRF - imports document from arbitrary URL
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Document>> importFromUrl(@RequestBody Map<String, Object> request) {
        Long claimId = Long.valueOf(request.get("claimId").toString());
        Long customerId = Long.valueOf(request.get("customerId").toString());
        String url = request.get("url").toString();
        String uploadedBy = request.getOrDefault("uploadedBy", "system").toString();

        Document doc = documentService.importFromUrl(claimId, customerId, url, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(doc));
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<ApiResponse<List<Document>>> getDocumentsByClaimId(@PathVariable Long claimId) {
        return ResponseEntity.ok(ApiResponse.ok(documentService.getDocumentsByClaimId(claimId)));
    }

    /**
     * Get all documents for a specific customer.
     * VULNERABILITY: IDOR - no check that authenticated user owns this customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Document>>> getDocumentsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok(documentService.getDocumentsByCustomerId(customerId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted"));
    }
}
