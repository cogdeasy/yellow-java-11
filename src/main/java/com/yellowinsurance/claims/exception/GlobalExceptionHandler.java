package com.yellowinsurance.claims.exception;

import com.yellowinsurance.claims.model.dto.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ISSUES:
 * - Exposes full stack traces to clients in all environments
 * - No distinction between production and development error responses
 * - Catch-all exception handler masks specific errors
 * - Logs sensitive information in error messages
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * VULNERABILITY: Exposing full stack traces to API consumers
     * Stack traces reveal internal implementation details, class names,
     * library versions, and file paths
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        // VULNERABILITY: Logging potentially sensitive user input
        logger.error("Unhandled exception: " + ex.getMessage(), ex);

        // VULNERABILITY: Full stack trace exposed to client
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        ApiResponse<Object> response = ApiResponse.error(
                "Internal server error: " + ex.getMessage(),
                ex.getClass().getName()
        );
        // VULNERABILITY: Stack trace sent to client
        response.setStackTrace(sw.toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception: " + ex.getMessage(), ex);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getClass().getName());
        response.setStackTrace(sw.toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
