package com.rdm.dto;

import com.rdm.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private User.Role role;
    private Boolean isActive;
}
