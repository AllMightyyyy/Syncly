package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class ClipboardMessage {
    private String content;
    private String deviceInfo;
    private String timestamp;
    private String username; // Sender's username
}
