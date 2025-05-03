package com.project.bpm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class PasswordChangeDTO implements Serializable {
    private Long userId;
    private String oldPassword;
    private String newPassword;
} 