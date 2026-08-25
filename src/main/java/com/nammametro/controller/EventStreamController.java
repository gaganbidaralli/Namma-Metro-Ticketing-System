package com.nammametro.controller;

import com.nammametro.event.BaseMetroEvent;
import com.nammametro.service.InMemoryEventBus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "*")
public class EventStreamController {

    private final InMemoryEventBus inMemoryEventBus;

    public EventStreamController(InMemoryEventBus inMemoryEventBus) {
        this.inMemoryEventBus = inMemoryEventBus;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return inMemoryEventBus.registerEmitter();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<BaseMetroEvent>> getRecentEvents() {
        return ResponseEntity.ok(inMemoryEventBus.getRecentEvents());
    }
}
