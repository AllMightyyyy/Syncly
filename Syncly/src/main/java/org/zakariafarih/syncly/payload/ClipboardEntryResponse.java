package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class ClipboardEntryResponse {
    private Long id;
    private String content; // TODO -> Need to be decrypted make sure it is
    private String timestamp;
    private String deviceInfo;
}
