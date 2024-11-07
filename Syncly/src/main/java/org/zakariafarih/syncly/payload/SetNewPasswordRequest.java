package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class SetNewPasswordRequest {
    private String token;
    private String newPassword;
}
