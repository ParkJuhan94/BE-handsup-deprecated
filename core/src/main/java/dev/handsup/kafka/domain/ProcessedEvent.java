package dev.handsup.kafka.domain;

import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import dev.handsup.common.domain.TimeBaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProcessedEvent extends TimeBaseEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;
}
