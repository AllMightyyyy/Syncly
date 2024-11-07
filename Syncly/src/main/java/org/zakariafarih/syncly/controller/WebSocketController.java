package org.zakariafarih.syncly.controller;

import jakarta.validation.Valid;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.zakariafarih.syncly.model.ClipboardEntry;
import org.zakariafarih.syncly.payload.ClipboardMessage;
import org.zakariafarih.syncly.service.ClipboardEntryService;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private ClipboardEntryService clipboardEntryService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/clipboard")
    public void sendClipboard(@Valid ClipboardMessage message, Authentication authentication) {
        String username = authentication.getName();

        // Validate that the message's username matches the authenticated user
        if (!username.equals(message.getUsername())) {
            throw new IllegalArgumentException("Username in message does not match authenticated user");
        }

        // Sanitize the content to prevent injection attacks
        String sanitizedContent = Jsoup.clean(message.getContent(), Safelist.basic());

        // Save the clipboard entry with sanitized content
        ClipboardEntry entry = clipboardEntryService.saveClipboardEntry(username, sanitizedContent, message.getDeviceInfo());

        // Prepare the message to send to the user
        message.setTimestamp(entry.getTimestamp().toString());
        message.setUsername(username);
        message.setContent(sanitizedContent); // Use sanitized content

        // Send the message to the user's personal topic
        messagingTemplate.convertAndSend("/topic/clipboard/" + username, message);
    }
}

/*
    User-Specific Topics: Messages are sent to /topic/clipboard/{username} to ensure that only the intended user receives them.
 */
