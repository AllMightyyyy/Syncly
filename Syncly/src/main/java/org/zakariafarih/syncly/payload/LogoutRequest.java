package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
