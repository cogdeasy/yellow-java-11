package com.libertymutual.audit.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class AuditEvent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 10, nullable = false)
    private String specversion = "1.0";

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Column(length = 100, nullable = false)
    private String source;

    @Column(name = "event_time", nullable = false)
    private OffsetDateTime eventTime;

    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(length = 100)
    private String action;

    @Column(length = 100)
    private String actor;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> changes;

    @Type(type = "jsonb")
    @Column(name = "raw_event", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> rawEvent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public AuditEvent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSpecversion() {
        return specversion;
    }

    public void setSpecversion(String specversion) {
        this.specversion = specversion;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(OffsetDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public Map<String, Object> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, Object> changes) {
        this.changes = changes;
    }

    public Map<String, Object> getRawEvent() {
        return rawEvent;
    }

    public void setRawEvent(Map<String, Object> rawEvent) {
        this.rawEvent = rawEvent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
