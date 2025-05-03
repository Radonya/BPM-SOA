package com.project.bpm.service;

import com.project.bpm.entity.*;
import com.project.bpm.repository.UserRepository;
import com.project.bpm.repository.EmployeRepository;
import com.project.bpm.config.SecurityConfig.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Authenticate a user with email and password
     */
    public AuthResponseDTO authenticate(LoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        
        if (userOpt.isEmpty()) {
            return new AuthResponseDTO("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            return new AuthResponseDTO("Compte désactivé");
        }
        
        if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return new AuthResponseDTO(user, "Authentification réussie");
        } else {
            return new AuthResponseDTO("Mot de passe incorrect");
        }
    }
    
    @Transactional
    public User createUserFromEmploye(Employe employe, String role) {
        // Check if user already exists
        if (userRepository.existsByEmail(employe.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(employe.getEmail());
            return existingUser.orElse(null);
        }
        
        // Validate role
        String validRole = validateRole(role);
        
        // Create new user
        User user = new User();
        user.setEmail(employe.getEmail());
        user.setEmployeId(employe.getId());
        user.setRole(validRole);
        
        // Set default password as email
        String rawPassword = employe.getEmail();
        user.setPassword(passwordEncoder.encode(rawPassword));
        
        return userRepository.save(user);
    }
    

    private String validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return UserRole.SIMPLE_EMPLOYE; 
        }
        
        String upperRole = role.toUpperCase().trim();
        if (upperRole.equals(UserRole.CONSEILLER_RH) || upperRole.equals(UserRole.SIMPLE_EMPLOYE)) {
            return upperRole;
        }
        
        // Default to SIMPLE_EMPLOYE if the role is not recognized
        return UserRole.SIMPLE_EMPLOYE;
    }
    
    /**
     * Change user password
     */
    @Transactional
    public AuthResponseDTO changePassword(PasswordChangeDTO passwordChangeDTO) {
        Optional<User> userOpt = userRepository.findById(passwordChangeDTO.getUserId());
        
        if (userOpt.isEmpty()) {
            return new AuthResponseDTO("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        // Verify old password
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPassword())) {
            return new AuthResponseDTO("Ancien mot de passe incorrect");
        }
        
        // Set new password
        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        User savedUser = userRepository.save(user);
        
        return new AuthResponseDTO(savedUser, "Mot de passe modifié avec succès");
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Reset user password
     */
    @Transactional
    public AuthResponseDTO resetPassword(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return new AuthResponseDTO("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        // Reset password to email
        Optional<Employe> employe = employeRepository.findById(user.getEmployeId());
        if (employe.isEmpty()) {
            return new AuthResponseDTO("Employé non trouvé");
        }
        
        String rawPassword = employe.get().getEmail();
        user.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);
        
        return new AuthResponseDTO(savedUser, "Mot de passe réinitialisé avec succès");
    }
} 