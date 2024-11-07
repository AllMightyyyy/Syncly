package org.zakariafarih.syncly.payload;

import lombok.Data;
import org.zakariafarih.syncly.model.DeviceType;

@Data
public class TwoFactorVerifyRequest {
    private String username;
    private String code;
    private String deviceInfo;
    private DeviceType deviceType;
}
