package org.zakariafarih.syncly.controller;

import org.zakariafarih.syncly.payload.ClipboardMessage;
import org.zakariafarih.syncly.service.ClipboardEntryService;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketController {

    @Autowired
    private ClipboardEntryService clipboardEntryService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @MessageMapping("/clipboard")
    @SendTo("/topic/clipboard")
    public ClipboardMessage sendClipboard(@Payload ClipboardMessage message, Authentication authentication) {
        String username = authentication.getName();
        // Save the clipboard entry
        clipboardEntryService.saveClipboardEntry(username, message.getContent(), message.getDeviceInfo());

        // Broadcast the message to subscribers
        message.setTimestamp(LocalDateTime.now().toString());
        message.setUsername(username);
        return message;
    }
}
