package org.zakariafarih.syncly.payload;

import org.zakariafarih.syncly.model.DeviceType;
import lombok.Data;

@Data
public class DeviceAddRequest {
    private String deviceName;
    private DeviceType deviceType;
}
