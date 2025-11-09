package com.rdm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDeviceId implements Serializable {
    private Integer groupId;
    private Integer deviceId;
}

