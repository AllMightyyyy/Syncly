package org.zakariafarih.syncly.payload;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 100)
    private String displayName;

    // TODO -> add URL validation in future, maybe make it so we can only upload images to a desired platform ?
    @Size(max = 2000)
    private String avatarUrl;
}
