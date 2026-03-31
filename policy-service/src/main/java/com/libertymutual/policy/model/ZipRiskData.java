package com.libertymutual.policy.model;

public class ZipRiskData {

    private String zipcode;
    private boolean floodZone;
    private double hurricaneRisk;
    private double wildfireRisk;
    private double crimeRate;
    private double baseModifier;
    private String state;
    private boolean requiresCoverageReview;

    public ZipRiskData() {
    }

    public ZipRiskData(String zipcode, boolean floodZone, double hurricaneRisk,
                       double wildfireRisk, double crimeRate, double baseModifier,
                       String state, boolean requiresCoverageReview) {
        this.zipcode = zipcode;
        this.floodZone = floodZone;
        this.hurricaneRisk = hurricaneRisk;
        this.wildfireRisk = wildfireRisk;
        this.crimeRate = crimeRate;
        this.baseModifier = baseModifier;
        this.state = state;
        this.requiresCoverageReview = requiresCoverageReview;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public boolean isFloodZone() {
        return floodZone;
    }

    public void setFloodZone(boolean floodZone) {
        this.floodZone = floodZone;
    }

    public double getHurricaneRisk() {
        return hurricaneRisk;
    }

    public void setHurricaneRisk(double hurricaneRisk) {
        this.hurricaneRisk = hurricaneRisk;
    }

    public double getWildfireRisk() {
        return wildfireRisk;
    }

    public void setWildfireRisk(double wildfireRisk) {
        this.wildfireRisk = wildfireRisk;
    }

    public double getCrimeRate() {
        return crimeRate;
    }

    public void setCrimeRate(double crimeRate) {
        this.crimeRate = crimeRate;
    }

    public double getBaseModifier() {
        return baseModifier;
    }

    public void setBaseModifier(double baseModifier) {
        this.baseModifier = baseModifier;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isRequiresCoverageReview() {
        return requiresCoverageReview;
    }

    public void setRequiresCoverageReview(boolean requiresCoverageReview) {
        this.requiresCoverageReview = requiresCoverageReview;
    }
}
