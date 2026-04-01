package com.libertymutual.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class RecalculateRequest {

    @NotBlank(message = "new_zipcode is required")
    @Pattern(regexp = "\\d{5}", message = "new_zipcode must be a 5-digit code")
    private String newZipcode;

    @Pattern(regexp = "\\d{5}", message = "old_zipcode must be a 5-digit code")
    private String oldZipcode;

    public String getNewZipcode() {
        return newZipcode;
    }

    public void setNewZipcode(String newZipcode) {
        this.newZipcode = newZipcode;
    }

    public String getOldZipcode() {
        return oldZipcode;
    }

    public void setOldZipcode(String oldZipcode) {
        this.oldZipcode = oldZipcode;
    }
}
