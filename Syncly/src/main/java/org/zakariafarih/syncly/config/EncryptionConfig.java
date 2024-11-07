package org.zakariafarih.syncly.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.keys.primary}")
    private String primaryKey;

    @Value("${encryption.keys.secondary}")
    private String secondaryKey;

    // Add more keys as needed for rotation

    private Map<String, String> encryptionKeys;

    @PostConstruct
    public void init() {
        encryptionKeys = new LinkedHashMap<>();
        encryptionKeys.put("key1", primaryKey);       // Active key
        encryptionKeys.put("key2", secondaryKey);     // Previous key
        // Add more keys to rotate
    }

    public Map<String, String> getEncryptionKeys() {
        return encryptionKeys;
    }
}