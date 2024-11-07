package org.zakariafarih.syncly.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.zakariafarih.syncly.validation.ValidPassword;

@Data
public class SetNewPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @ValidPassword
    private String newPassword;
}
