package com.project.bpm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "employes")
@Data
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = true)
    private String nom;

    @Column(nullable = true)
    private String prenom;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String adresse;

    @Column(nullable = true)
    private String telephone;

    @Column(nullable = true)
    private String numeroEmploye;

    @Column(nullable = true)
    private String numeroAssurance;

    @Column(nullable = true)
    private String departement;

    private boolean estBeneficiaire;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChangementBeneficiaire> changementsBeneficiaire;
}
