package com.libertymutual.audit.repository;

import com.libertymutual.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findByEntityTypeOrderByEventTimeDesc(String entityType);

    List<AuditEvent> findByEntityIdOrderByEventTimeDesc(UUID entityId);

    List<AuditEvent> findBySourceOrderByEventTimeDesc(String source);

    List<AuditEvent> findByEntityTypeAndEntityIdOrderByEventTimeAsc(String entityType, UUID entityId);

    @Query(value = "SELECT * FROM audit_events a WHERE " +
            "(CAST(:entityType AS VARCHAR) IS NULL OR a.entity_type = :entityType) AND " +
            "(CAST(:entityId AS UUID) IS NULL OR a.entity_id = :entityId) AND " +
            "(CAST(:source AS VARCHAR) IS NULL OR a.source = :source) AND " +
            "(CAST(:fromTime AS TIMESTAMP WITH TIME ZONE) IS NULL OR a.event_time >= :fromTime) AND " +
            "(CAST(:toTime AS TIMESTAMP WITH TIME ZONE) IS NULL OR a.event_time <= :toTime) " +
            "ORDER BY a.event_time DESC",
            nativeQuery = true)
    List<AuditEvent> findByFilters(
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId,
            @Param("source") String source,
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime);

    @Query("SELECT a.eventType, COUNT(a) FROM AuditEvent a GROUP BY a.eventType")
    List<Object[]> countByEventType();

    @Query("SELECT a.source, COUNT(a) FROM AuditEvent a GROUP BY a.source")
    List<Object[]> countBySource();

    @Query("SELECT MIN(a.eventTime) FROM AuditEvent a")
    OffsetDateTime findEarliestEventTime();

    @Query("SELECT MAX(a.eventTime) FROM AuditEvent a")
    OffsetDateTime findLatestEventTime();
}
