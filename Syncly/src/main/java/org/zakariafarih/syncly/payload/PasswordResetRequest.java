package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
}
