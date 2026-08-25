package com.nammametro.service;

import com.nammametro.event.BaseMetroEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryEventBus {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);
    private static final int MAX_HISTORY = 100;

    private final List<BaseMetroEvent> eventHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void publish(BaseMetroEvent event) {
        // Maintain fixed size ring history
        eventHistory.add(event);
        while (eventHistory.size() > MAX_HISTORY) {
            eventHistory.remove(0);
        }

        // Broadcast to SSE clients
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.getEventType())
                        .data(event));
            } catch (IOException | IllegalStateException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 min timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send recent events immediately to client
        try {
            emitter.send(SseEmitter.event().name("INIT").data(new ArrayList<>(eventHistory)));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public List<BaseMetroEvent> getRecentEvents() {
        return new ArrayList<>(eventHistory);
    }
}
