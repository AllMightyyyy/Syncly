package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String displayName;
    private String avatarUrl;
}
