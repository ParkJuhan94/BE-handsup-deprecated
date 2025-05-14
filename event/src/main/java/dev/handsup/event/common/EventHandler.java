package dev.handsup.event.common;

import dev.handsup.common.event.DomainEvent;

public interface EventHandler<T extends DomainEvent> {

    boolean supports(T event);

    void handle(T event);
}
