package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String usernameOrEmail;
    private String password;
    private String deviceInfo;
    private String deviceType;
}
