package com.nammametro.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseMetroEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    private LocalDateTime timestamp = LocalDateTime.now();

    public BaseMetroEvent() {}

    public BaseMetroEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
