package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
