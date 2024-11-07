package org.zakariafarih.syncly.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwoFactorRequiredResponse {
    private String message;
    private String username;
}

