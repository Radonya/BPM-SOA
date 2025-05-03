package com.project.bpm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class LoginDTO implements Serializable {
    private String email;
    private String password;
} 