package org.zakariafarih.syncly.payload;


import lombok.Data;

@Data
public class PasteBinCreateRequest {
    private String name;
    private String content; // TODO -> Need to be encrypted make sure it is
}