package org.zakariafarih.syncly.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClipboardMessage {
    @NotBlank(message = "Content must not be blank")
    @Size(max = 10000, message = "Content must not exceed 10,000 characters")
    private String content;

    @NotBlank(message = "Device info must not be blank")
    @Size(max = 255, message = "Device info must not exceed 255 characters")
    private String deviceInfo;

    private String timestamp;
    private String username; // Sender's username
}
