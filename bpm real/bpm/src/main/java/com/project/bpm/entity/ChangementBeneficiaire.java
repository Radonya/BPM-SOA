package com.project.bpm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "changements_beneficiaire")
@Data
public class ChangementBeneficiaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateChangement;

    @Column(nullable = false)
    private String raisonChangement;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id", nullable = false)
    @JsonDeserialize(using = EmployeDeserializer.class)
    private Employe employe;

    @Column(name = "numero_contrat_assurance", nullable = false)
    private String numeroContratAssurance;
}