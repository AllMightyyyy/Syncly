package org.zakariafarih.syncly.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwoFactorSetupResponse {
    private String qrCodeUrl;
}
