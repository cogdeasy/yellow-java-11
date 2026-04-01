package com.libertymutual.customer.dto;

import com.libertymutual.customer.model.Customer;

import java.util.List;
import java.util.Map;

public class AddressChangeResponse {

    private Customer customer;
    private List<Map<String, Object>> premiumRecalculation;
    private CoverageReevaluation coverageReevaluation;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Map<String, Object>> getPremiumRecalculation() {
        return premiumRecalculation;
    }

    public void setPremiumRecalculation(List<Map<String, Object>> premiumRecalculation) {
        this.premiumRecalculation = premiumRecalculation;
    }

    public CoverageReevaluation getCoverageReevaluation() {
        return coverageReevaluation;
    }

    public void setCoverageReevaluation(CoverageReevaluation coverageReevaluation) {
        this.coverageReevaluation = coverageReevaluation;
    }

    public static class CoverageReevaluation {
        private boolean triggered;
        private String state;
        private String result;

        public CoverageReevaluation() {
        }

        public CoverageReevaluation(boolean triggered, String state, String result) {
            this.triggered = triggered;
            this.state = state;
            this.result = result;
        }

        public boolean isTriggered() {
            return triggered;
        }

        public void setTriggered(boolean triggered) {
            this.triggered = triggered;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
