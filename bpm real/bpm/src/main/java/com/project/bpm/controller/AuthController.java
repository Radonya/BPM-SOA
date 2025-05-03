package com.project.bpm.controller;

import com.project.bpm.entity.*;
import com.project.bpm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API pour l'authentification et la gestion des utilisateurs")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Authentifier un utilisateur", 
        description = "Permet à un utilisateur de se connecter avec son email et mot de passe et retourne un token d'accès"
    )
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = userService.authenticate(loginDTO);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @PostMapping("/password-change")
    @Operation(
        summary = "Changer le mot de passe", 
        description = "Permet à un utilisateur authentifié de modifier son mot de passe en fournissant l'ancien et le nouveau"
    )
    public ResponseEntity<AuthResponseDTO> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO) {
        AuthResponseDTO response = userService.changePassword(passwordChangeDTO);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }
    
    @GetMapping("/users/{id}")
    @Operation(
        summary = "Obtenir les détails d'un utilisateur", 
        description = "Récupère les informations complètes d'un utilisateur par son identifiant"
    )
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/password-reset/{userId}")
    @Operation(
        summary = "Réinitialiser le mot de passe", 
        description = "Réinitialise le mot de passe d'un utilisateur à la valeur par défaut (son email)"
    )
    public ResponseEntity<AuthResponseDTO> resetPassword(@PathVariable Long userId) {
        AuthResponseDTO response = userService.resetPassword(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }
} 