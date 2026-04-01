package com.libertymutual.policy.service;

import com.libertymutual.policy.model.ZipRiskData;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simulated ZIP risk data — in production this would be backed by an actuarial database.
 */
@Service
public class ZipRiskService {

    private static final Map<String, ZipRiskData> ZIP_RISK_DATA = new HashMap<>();

    static {
        // Massachusetts — Boston area
        ZIP_RISK_DATA.put("02101", new ZipRiskData("02101", false, 0.2, 0.05, 0.4, 1.1, "MA", false));
        ZIP_RISK_DATA.put("02102", new ZipRiskData("02102", false, 0.2, 0.05, 0.35, 1.05, "MA", false));
        // Florida — Miami area (high risk, requires review)
        ZIP_RISK_DATA.put("33101", new ZipRiskData("33101", true, 0.85, 0.1, 0.5, 1.75, "FL", true));
        ZIP_RISK_DATA.put("33139", new ZipRiskData("33139", true, 0.9, 0.05, 0.45, 1.85, "FL", true));
        // California — Los Angeles area (wildfire risk, requires review)
        ZIP_RISK_DATA.put("90001", new ZipRiskData("90001", false, 0.0, 0.7, 0.55, 1.55, "CA", true));
        ZIP_RISK_DATA.put("90210", new ZipRiskData("90210", false, 0.0, 0.8, 0.15, 1.45, "CA", true));
        // Texas — Houston area (flood risk, requires review)
        ZIP_RISK_DATA.put("77001", new ZipRiskData("77001", true, 0.6, 0.15, 0.5, 1.6, "TX", true));
        ZIP_RISK_DATA.put("75201", new ZipRiskData("75201", false, 0.3, 0.2, 0.4, 1.25, "TX", true));
        // New York
        ZIP_RISK_DATA.put("10001", new ZipRiskData("10001", false, 0.15, 0.0, 0.45, 1.3, "NY", false));
        // Colorado — Denver (low risk)
        ZIP_RISK_DATA.put("80201", new ZipRiskData("80201", false, 0.0, 0.35, 0.3, 1.05, "CO", false));
    }

    public Optional<ZipRiskData> getZipRiskFactors(String zipcode) {
        return Optional.ofNullable(ZIP_RISK_DATA.get(zipcode));
    }
}
