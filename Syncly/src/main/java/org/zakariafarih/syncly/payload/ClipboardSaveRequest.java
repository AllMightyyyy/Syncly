package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class ClipboardSaveRequest {
    private String content; // TODO -> Need to be encrypted make sure it is
    private String deviceInfo;
}
