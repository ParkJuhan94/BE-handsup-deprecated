package dev.handsup.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.handsup.common.entity.DeadLetterLog;

@Repository
public interface DeadLetterLogRepository extends JpaRepository<DeadLetterLog, Long> {

}
