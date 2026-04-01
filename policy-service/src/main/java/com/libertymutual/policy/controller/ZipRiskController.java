package com.libertymutual.policy.controller;

import com.libertymutual.policy.dto.ErrorResponse;
import com.libertymutual.policy.model.ZipRiskData;
import com.libertymutual.policy.service.ZipRiskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/zip-risk")
public class ZipRiskController {

    private final ZipRiskService zipRiskService;

    public ZipRiskController(ZipRiskService zipRiskService) {
        this.zipRiskService = zipRiskService;
    }

    @GetMapping("/{zipcode}")
    public ResponseEntity<?> getZipRiskFactors(@PathVariable String zipcode) {
        return zipRiskService.getZipRiskFactors(zipcode)
                .map(data -> ResponseEntity.ok((Object) data))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("not_found",
                                "Risk data not found for ZIP code " + zipcode)));
    }
}
