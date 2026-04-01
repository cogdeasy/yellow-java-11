package com.libertymutual.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class CloudEventRequest {

    @NotBlank(message = "specversion is required")
    private String specversion;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "source is required")
    private String source;

    @NotBlank(message = "id is required")
    private String id;

    @NotBlank(message = "time is required")
    private String time;

    @NotNull(message = "data is required")
    private Map<String, Object> data;

    public String getSpecversion() {
        return specversion;
    }

    public void setSpecversion(String specversion) {
        this.specversion = specversion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
