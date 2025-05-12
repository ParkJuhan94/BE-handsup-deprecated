package dev.handsup.event.consumer;

import dev.handsup.event.common.DomainEvent;

public interface EventHandler<T extends DomainEvent> {

    boolean supports(T event);

    void handle(T event);
}
