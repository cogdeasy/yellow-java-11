package com.libertymutual.audit.dto;

import java.util.Map;

public class AuditStatsResponse {

    private long totalEvents;
    private Map<String, Long> eventsByType;
    private Map<String, Long> eventsBySource;
    private TimeRange timeRange;

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public Map<String, Long> getEventsByType() {
        return eventsByType;
    }

    public void setEventsByType(Map<String, Long> eventsByType) {
        this.eventsByType = eventsByType;
    }

    public Map<String, Long> getEventsBySource() {
        return eventsBySource;
    }

    public void setEventsBySource(Map<String, Long> eventsBySource) {
        this.eventsBySource = eventsBySource;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public static class TimeRange {
        private String earliest;
        private String latest;

        public TimeRange() {
        }

        public TimeRange(String earliest, String latest) {
            this.earliest = earliest;
            this.latest = latest;
        }

        public String getEarliest() {
            return earliest;
        }

        public void setEarliest(String earliest) {
            this.earliest = earliest;
        }

        public String getLatest() {
            return latest;
        }

        public void setLatest(String latest) {
            this.latest = latest;
        }
    }
}
