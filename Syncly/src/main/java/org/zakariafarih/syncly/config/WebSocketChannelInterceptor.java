package org.zakariafarih.syncly.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChannelInterceptor(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getMessageType() == SimpMessageType.SUBSCRIBE) {
            Authentication user = (Authentication) accessor.getUser();
            String destination = accessor.getDestination();

            if (user != null && destination != null) {
                String username = user.getName();
                String expectedDestination = "/topic/clipboard/" + username;

                if (!expectedDestination.equals(destination)) {
                    // Send error message to the user
                    messagingTemplate.convertAndSendToUser(username, "/queue/errors", "Unauthorized subscription attempt to " + destination);

                    // Deny the subscription by returning null
                    return null;
                }
            }
        }

        return message;
    }
}