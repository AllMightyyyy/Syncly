package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class ClipboardEntryResponse {
    private Long id;
    private String content;
    private String timestamp;
    private String deviceInfo;
}
