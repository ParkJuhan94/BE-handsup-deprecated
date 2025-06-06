package dev.handsup.common.entity;

import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProcessedEvent extends TimeBaseEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;
}
