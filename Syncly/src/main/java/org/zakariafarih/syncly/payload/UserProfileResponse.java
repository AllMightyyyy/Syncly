package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
}
