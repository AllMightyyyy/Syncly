package org.zakariafarih.syncly.payload;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 100)
    private String displayName;

    @URL(message = "Invalid URL format")
    private String avatarUrl;
}
