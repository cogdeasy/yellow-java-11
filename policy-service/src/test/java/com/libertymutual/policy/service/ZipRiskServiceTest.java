package com.libertymutual.policy.service;

import com.libertymutual.policy.model.ZipRiskData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipRiskServiceTest {

    private final ZipRiskService zipRiskService = new ZipRiskService();

    @Test
    void getZipRiskFactors_knownZip_returnsData() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("02101");
        assertTrue(result.isPresent());
        assertEquals("MA", result.get().getState());
        assertEquals(1.1, result.get().getBaseModifier());
        assertFalse(result.get().isRequiresCoverageReview());
    }

    @Test
    void getZipRiskFactors_floridaZip_highRisk() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("33101");
        assertTrue(result.isPresent());
        assertEquals("FL", result.get().getState());
        assertTrue(result.get().isFloodZone());
        assertEquals(0.85, result.get().getHurricaneRisk());
        assertTrue(result.get().isRequiresCoverageReview());
    }

    @Test
    void getZipRiskFactors_californiaZip_wildfireRisk() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("90210");
        assertTrue(result.isPresent());
        assertEquals("CA", result.get().getState());
        assertEquals(0.8, result.get().getWildfireRisk());
        assertTrue(result.get().isRequiresCoverageReview());
    }

    @Test
    void getZipRiskFactors_texasZip_requiresReview() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("77001");
        assertTrue(result.isPresent());
        assertEquals("TX", result.get().getState());
        assertTrue(result.get().isFloodZone());
        assertTrue(result.get().isRequiresCoverageReview());
    }

    @Test
    void getZipRiskFactors_unknownZip_returnsEmpty() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("99999");
        assertFalse(result.isPresent());
    }

    @Test
    void getZipRiskFactors_coloradoZip_lowRisk() {
        Optional<ZipRiskData> result = zipRiskService.getZipRiskFactors("80201");
        assertTrue(result.isPresent());
        assertEquals("CO", result.get().getState());
        assertEquals(1.05, result.get().getBaseModifier());
        assertFalse(result.get().isRequiresCoverageReview());
    }
}
