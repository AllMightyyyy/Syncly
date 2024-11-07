package org.zakariafarih.syncly.payload;

import lombok.Data;

@Data
public class PasteBinResponse {
    private Long id;
    private String name;
    private String content; // TODO -> Need to be decrypted make sure it is
    private String timestamp;
}
