package com.project.bpm.controller;

import com.project.bpm.entity.Employe;
import com.project.bpm.entity.EmployeDTO;
import com.project.bpm.service.EmployeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@Tag(name = "Employés", description = "API pour la gestion des employés")
public class EmployeController {
    
    @Autowired
    private EmployeService employeService;
    
    @GetMapping
    @Operation(summary = "Récupérer tous les employés", description = "Récupère la liste de tous les employés")
    public List<Employe> getAllEmployes() {
        return employeService.getAllEmployes();
    }

    @PostMapping
    @Operation(summary = "Créer un employé", description = "Crée un nouvel employé et son compte utilisateur associé")
    public ResponseEntity<Employe> createEmploye(@RequestBody EmployeDTO employeDTO) {
        Employe createdEmploye = employeService.createEmploye(employeDTO);
        return ResponseEntity.ok(createdEmploye);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un employé par ID", description = "Récupère les détails d'un employé spécifique")
    public ResponseEntity<Employe> getEmployeById(@PathVariable Long id) {
        return employeService.getEmployeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un employé", description = "Met à jour les informations d'un employé existant")
    public ResponseEntity<Employe> updateEmploye(@PathVariable Long id, @RequestBody EmployeDTO employeDTO) {
        return employeService.updateEmploye(id, employeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un employé", description = "Supprime un employé par son ID")
    public ResponseEntity<?> deleteEmploye(@PathVariable Long id) {
        boolean deleted = employeService.deleteEmploye(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 