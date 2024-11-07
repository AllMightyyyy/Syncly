package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class ClipboardSaveRequest {
    private String content;
    private String deviceInfo;
}
