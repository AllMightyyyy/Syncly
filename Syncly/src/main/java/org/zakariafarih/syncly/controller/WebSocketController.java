package org.zakariafarih.syncly.controller;

import org.zakariafarih.syncly.model.ClipboardEntry;
import org.zakariafarih.syncly.payload.ClipboardMessage;
import org.zakariafarih.syncly.service.ClipboardEntryService;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketController {

    @Autowired
    private ClipboardEntryService clipboardEntryService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/clipboard")
    public void sendClipboard(ClipboardMessage message, Authentication authentication) {
        String username = authentication.getName();

        // Validate that the message's username matches the authenticated user
        if (!username.equals(message.getUsername())) {
            throw new IllegalArgumentException("Username in message does not match authenticated user");
        }

        // Save the clipboard entry
        ClipboardEntry entry = clipboardEntryService.saveClipboardEntry(username, message.getContent(), message.getDeviceInfo());

        // Prepare the message to send to the user
        message.setTimestamp(entry.getTimestamp().toString());
        message.setUsername(username);

        // Send the message to the user's personal topic
        messagingTemplate.convertAndSendToUser(username, "/topic/clipboard", message);
    }
}
