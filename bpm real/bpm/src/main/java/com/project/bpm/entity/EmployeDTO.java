package com.project.bpm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
@Schema(description = "Objet de transfert de données pour la création et mise à jour d'employés")
public class EmployeDTO implements Serializable {
    
    @Schema(description = "Nom de famille de l'employé", example = "Dupont", required = true)
    private String nom;
    
    @Schema(description = "Prénom de l'employé", example = "Jean", required = true)
    private String prenom;
    
    @Schema(description = "Adresse email professionnelle", example = "jean.dupont@company.com", required = true)
    private String email;
    
    @Schema(description = "Adresse postale complète", example = "123 Rue de Paris, 75001 Paris")
    private String adresse;
    
    @Schema(description = "Numéro de téléphone (format international)", example = "+33612345678")
    private String telephone;
    
    @Schema(description = "Identifiant unique d'employé", example = "EMP-2023-001", required = true)
    private String numeroEmploye;
    
    @Schema(description = "Numéro d'assurance (généré automatiquement si non fourni)", example = "AS-DU-J-12345678")
    private String numeroAssurance;
    
    @Schema(description = "Département ou service", example = "INFORMATIQUE", required = true)
    private String departement;

    @Schema(description = "Indique si l'employé bénéficie des avantages sociaux", example = "true")
    private boolean estBeneficiaire;

    @Schema(description = "Rôle dans le système", example = "SIMPLE_EMPLOYE", defaultValue = "SIMPLE_EMPLOYE")
    private String role = UserRole.SIMPLE_EMPLOYE; 
}