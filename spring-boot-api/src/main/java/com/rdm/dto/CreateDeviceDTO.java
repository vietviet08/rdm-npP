package com.rdm.dto;

import com.rdm.model.Device;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateDeviceDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Host is required")
    private String host;
    
    @NotNull(message = "Port is required")
    @Positive(message = "Port must be positive")
    private Integer port;
    
    @NotNull(message = "Protocol is required")
    private Device.Protocol protocol;
    
    private String username;
    private String password;
    private String privateKey;
    private List<String> tags;
}

