package com.rdm.dto;

import com.rdm.model.Device;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class UpdateDeviceDTO {
    private String name;
    private String description;
    private String host;

    @Positive(message = "Port must be positive")
    private Integer port;

    private Device.Protocol protocol;
    private String username;
    private String password;
    private String privateKey;
    private List<String> tags;
    private Boolean isActive;
}
