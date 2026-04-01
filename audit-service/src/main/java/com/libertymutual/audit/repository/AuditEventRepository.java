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

    @Query("SELECT a FROM AuditEvent a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:entityId IS NULL OR a.entityId = :entityId) AND " +
            "(:source IS NULL OR a.source = :source) AND " +
            "(:fromTime IS NULL OR a.eventTime >= :fromTime) AND " +
            "(:toTime IS NULL OR a.eventTime <= :toTime) " +
            "ORDER BY a.eventTime DESC")
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
