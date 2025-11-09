package com.rdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionInitiateResponse {
    private String connectionUrl;
    private Integer connectionLogId;
    private String guacamoleConnId;
    private String deviceName;
    private String protocol;
}
