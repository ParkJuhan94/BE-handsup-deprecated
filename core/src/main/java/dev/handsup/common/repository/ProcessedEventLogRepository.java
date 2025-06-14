package dev.handsup.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.handsup.kafka.domain.ProcessedEvent;

@Repository
public interface ProcessedEventLogRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventId(String eventId);
}
