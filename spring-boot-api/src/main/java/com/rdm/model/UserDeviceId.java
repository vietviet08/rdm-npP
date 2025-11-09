package com.rdm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceId implements Serializable {
    private Integer userId;
    private Integer deviceId;
}

