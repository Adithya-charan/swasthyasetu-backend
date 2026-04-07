package com.swasthyasetu.webrtc;

import java.util.Objects;
import org.springframework.lang.NonNull;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebRTCController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebRTCController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal/{room}")
    public void processSignal(@DestinationVariable @NonNull String room, @Payload @NonNull String payload) {
        // Broadcast the signal to the specific room's topic
        messagingTemplate.convertAndSend("/topic/signal/" + room, Objects.requireNonNull(payload, "Payload must not be null"));
    }
}
