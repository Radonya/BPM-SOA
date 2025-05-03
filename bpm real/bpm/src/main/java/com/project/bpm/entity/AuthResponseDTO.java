package com.project.bpm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class AuthResponseDTO implements Serializable {
    private Long userId;
    private Long employeId;
    private String email;
    private String role;
    private boolean success;
    private String message;
    
    // Constructor for successful authentication
    public AuthResponseDTO(User user, String message) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.employeId = user.getEmployeId();
        this.success = true;
        this.message = message;
    }
    
    // Constructor for failed authentication
    public AuthResponseDTO(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }
} 