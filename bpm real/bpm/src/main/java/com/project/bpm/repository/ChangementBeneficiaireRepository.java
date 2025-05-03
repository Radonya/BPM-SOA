package com.project.bpm.repository;

import com.project.bpm.entity.ChangementBeneficiaire;
import com.project.bpm.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ChangementBeneficiaireRepository extends JpaRepository<ChangementBeneficiaire, Long> {
    
    // Trouver par employé
    List<ChangementBeneficiaire> findByEmploye(Employe employe);
    
    // Trouver par status
    List<ChangementBeneficiaire> findByStatus(String status);
    
    // Trouver par numéro de contrat d'assurance
    List<ChangementBeneficiaire> findByNumeroContratAssurance(String numeroContratAssurance);
    
    // Trouver les changements entre deux dates
    List<ChangementBeneficiaire> findByDateChangementBetween(Date dateDebut, Date dateFin);
    
    // Trouver les changements par employé et status
    List<ChangementBeneficiaire> findByEmployeAndStatus(Employe employe, String status);
    
    // Requête personnalisée pour trouver les changements récents d'un employé
    @Query("SELECT c FROM ChangementBeneficiaire c WHERE c.employe = :employe AND c.dateChangement >= :date ORDER BY c.dateChangement DESC")
    List<ChangementBeneficiaire> findRecentChangementsByEmploye(@Param("employe") Employe employe, @Param("date") Date date);
    
    // Compter le nombre de changements par status
    long countByStatus(String status);
    
    // Vérifier si un employé a des changements en cours
    boolean existsByEmployeAndStatus(Employe employe, String status);
    
    // Trouver les derniers changements, limités à un certain nombre
    @Query("SELECT c FROM ChangementBeneficiaire c ORDER BY c.dateChangement DESC")
    List<ChangementBeneficiaire> findLastChangements(org.springframework.data.domain.Pageable pageable);
} 