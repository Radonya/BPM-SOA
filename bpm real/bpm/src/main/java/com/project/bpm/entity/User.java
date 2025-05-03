package com.project.bpm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "utilisateurs")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role = "USER"; // Default role
    
    private boolean isActive = true;
    
    // Reference to the employee ID 
    @Column(name = "employe_id", nullable = false)
    private Long employeId;
}

