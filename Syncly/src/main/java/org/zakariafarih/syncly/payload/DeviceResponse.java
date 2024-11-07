package org.zakariafarih.syncly.payload;

import org.zakariafarih.syncly.model.DeviceType;
import lombok.Data;

@Data
public class DeviceResponse {
    private Long id;
    private String deviceName;
    private DeviceType deviceType;
    private String createdAt;
}
