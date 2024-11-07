package org.zakariafarih.syncly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.zakariafarih.syncly.security.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;

    // Inject properties using @Value
    @Value("${spring.websocket.stomp.brokerRelay.host}")
    private String relayHost;

    @Value("${spring.websocket.stomp.brokerRelay.port}")
    private int relayPort;

    @Value("${spring.websocket.stomp.brokerRelay.clientLogin}")
    private String clientLogin;

    @Value("${spring.websocket.stomp.brokerRelay.clientPasscode}")
    private String clientPasscode;

    @Value("${spring.websocket.stomp.brokerRelay.username}")
    private String systemLogin;

    @Value("${spring.websocket.stomp.brokerRelay.password}")
    private String systemPasscode;

    @Value("${spring.websocket.stomp.brokerRelay.virtualHost}")
    private String virtualHost;

    public WebSocketConfig(WebSocketChannelInterceptor webSocketChannelInterceptor) {
        this.webSocketChannelInterceptor = webSocketChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configure the external RabbitMQ message broker
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode)
                .setSystemLogin(systemLogin)
                .setSystemPasscode(systemPasscode)
                .setVirtualHost(virtualHost);

        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // adjust cors now it's allows all origins -> :D
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor, webSocketChannelInterceptor);
    }
}
