package com.project.bpm.controller;

import com.project.bpm.entity.ChangementBeneficiaire;
import com.project.bpm.entity.Employe;
import com.project.bpm.repository.ChangementBeneficiaireRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/changements")
@Tag(name = "Changement de Bénéficiaire", description = "API pour la gestion des changements de bénéficiaire d'assurance")
public class ChangementBeneficiaireController {
    
    @Autowired
    private ChangementBeneficiaireRepository changementRepository;
    
    private final String NODE_BASE_URL = "http://localhost:3001";
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping
    @Operation(summary = "Obtenir tous les changements de bénéficiaire", description = "Retourne la liste complète des demandes de changement de bénéficiaire")
    public List<ChangementBeneficiaire> getAllChangements() {
        return changementRepository.findAll();
    }
    
    @PostMapping
    @Operation(summary = "Créer un nouveau changement de bénéficiaire", description = "Crée une nouvelle demande de changement de bénéficiaire et envoie une notification")
    public ChangementBeneficiaire createChangement(@RequestBody ChangementBeneficiaire changement) {
        changement.setDateChangement(new Date());
        changement.setStatus("EN_ATTENTE");
        ChangementBeneficiaire savedChangement = changementRepository.save(changement);
        
        sendWhatsAppNotification(
            savedChangement,
            "Nouveau changement de bénéficiaire créé pour le contrat " + changement.getNumeroContratAssurance(),
            "CREATION"
        );
        
        return savedChangement;
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un changement de bénéficiaire par ID", description = "Retrouve une demande de changement spécifique par son identifiant")
    public ResponseEntity<ChangementBeneficiaire> getChangementById(@PathVariable Long id) {
        return changementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un changement de bénéficiaire", description = "Modifie une demande de changement existante et envoie une notification")
    public ResponseEntity<ChangementBeneficiaire> updateChangement(@PathVariable Long id, @RequestBody ChangementBeneficiaire changementDetails) {
        return changementRepository.findById(id)
                .map(changement -> {
                    changement.setRaisonChangement(changementDetails.getRaisonChangement());
                    changement.setStatus("MISE_A_JOUR");
                    changement.setEmploye(changementDetails.getEmploye());
                    changement.setNumeroContratAssurance(changementDetails.getNumeroContratAssurance());
                    
                    ChangementBeneficiaire updatedChangement = changementRepository.save(changement);
                    
                    sendWhatsAppNotification(
                        updatedChangement,
                        "Mise à jour du changement de bénéficiaire pour le contrat " + changement.getNumeroContratAssurance(),
                        "MISE_A_JOUR"
                    );
                    
                    return ResponseEntity.ok(updatedChangement);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/confirmer")
    @Operation(summary = "Confirmer un changement de bénéficiaire", description = "Valide définitivement une demande de changement, crée l'assurance si nécessaire et notifie l'employé")
    public ResponseEntity<ChangementBeneficiaire> confirmerChangement(@PathVariable Long id) {
        return changementRepository.findById(id)
                .map(changement -> {
                    Employe employe = changement.getEmploye();
                    String numeroContrat = changement.getNumeroContratAssurance();
                    
                    boolean assuranceExists = checkAssuranceExists(numeroContrat);
                    
                    if (!assuranceExists) {
                        createAssuranceInNodeService(employe, numeroContrat);
                    }
                    
                    changement.setStatus("CONFIRME");
                    ChangementBeneficiaire updatedChangement = changementRepository.save(changement);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    Map<String, Object> assuranceData = Map.of(
                        "changementId", id.toString(),
                        "numeroContrat", numeroContrat,
                        "dateChangement", new Date(),
                        "status", "CONFIRME"
                    );
                    
                    HttpEntity<Map<String, Object>> assuranceRequest = new HttpEntity<>(assuranceData, headers);
                    try {
                        restTemplate.postForEntity(
                            NODE_BASE_URL + "/api/assurances/confirmer-changement",
                            assuranceRequest,
                            Object.class
                        );
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la confirmation du changement: " + e.getMessage());
                    }
                    
                    sendWhatsAppNotification(
                        updatedChangement,
                        "Confirmation du changement de bénéficiaire pour le contrat " + numeroContrat,
                        "CONFIRMATION"
                    );
                    
                    return ResponseEntity.ok(updatedChangement);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private boolean checkAssuranceExists(String numeroContrat) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                NODE_BASE_URL + "/api/assurances/check/" + numeroContrat,
                Map.class
            );
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody() != null && 
                   Boolean.TRUE.equals(response.getBody().get("exists"));
        } catch (Exception e) {
            return false;
        }
    }
    
    private void createAssuranceInNodeService(Employe employe, String numeroContrat) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ResponseEntity<Employe> employeResponse = restTemplate.getForEntity(
                "http://localhost:8080/api/employes/" + employe.getId(), 
                Employe.class
            );
            
            if (employeResponse.getStatusCode().is2xxSuccessful() && employeResponse.getBody() != null) {
                Employe employeComplet = employeResponse.getBody();
                
                Map<String, Object> employeMap = new HashMap<>();
                employeMap.put("id", employeComplet.getId());
                employeMap.put("numeroEmploye", employeComplet.getNumeroEmploye());
                employeMap.put("numeroAssurance", numeroContrat);
                employeMap.put("departement", employeComplet.getDepartement());
                employeMap.put("estBeneficiaire", employeComplet.isEstBeneficiaire());
                
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", employeComplet.getId());
                userMap.put("nom", employeComplet.getNom());
                userMap.put("prenom", employeComplet.getPrenom());
                userMap.put("email", employeComplet.getEmail());
                userMap.put("adresse", employeComplet.getAdresse());
                userMap.put("telephone", employeComplet.getTelephone());
                
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("employe", employeMap);
                requestMap.put("utilisateur", userMap);
                requestMap.put("numeroContrat", numeroContrat);
                requestMap.put("action", "CREATION");
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);
                
                restTemplate.postForEntity(
                    NODE_BASE_URL + "/api/assurances/from-employe", 
                    request, 
                    Map.class
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'assurance dans le microservice: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/refuser")
    @Operation(summary = "Refuser un changement de bénéficiaire", description = "Rejette une demande de changement et indique optionnellement la raison du refus")
    public ResponseEntity<ChangementBeneficiaire> refuserChangement(
            @PathVariable Long id, 
            @RequestParam(required = false) String raisonRefus) {
        return changementRepository.findById(id)
                .map(changement -> {
                    changement.setStatus("REFUSE");
                    if (raisonRefus != null && !raisonRefus.isEmpty()) {
                        changement.setRaisonChangement(raisonRefus);
                    }
                    ChangementBeneficiaire updatedChangement = changementRepository.save(changement);
                    
                    String message = "Refus du changement de bénéficiaire pour le contrat " + changement.getNumeroContratAssurance();
                    if (raisonRefus != null && !raisonRefus.isEmpty()) {
                        message += ". Raison: " + raisonRefus;
                    }
                    
                    sendWhatsAppNotification(
                        updatedChangement,
                        message,
                        "REFUS"
                    );
                    
                    return ResponseEntity.ok(updatedChangement);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler un changement de bénéficiaire", description = "Annule une demande de changement en cours et notifie l'employé concerné")
    public ResponseEntity<ChangementBeneficiaire> annulerChangement(@PathVariable Long id) {
        return changementRepository.findById(id)
                .map(changement -> {
                    changement.setStatus("ANNULE");
                    ChangementBeneficiaire updatedChangement = changementRepository.save(changement);
                    
                    sendWhatsAppNotification(
                        updatedChangement,
                        "Annulation du changement de bénéficiaire pour le contrat " + changement.getNumeroContratAssurance(),
                        "ANNULATION"
                    );
                    
                    return ResponseEntity.ok(updatedChangement);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/employe/{employeId}")
    @Operation(summary = "Obtenir les changements d'un employé", description = "Retourne toutes les demandes de changement associées à un employé spécifique")
    public List<ChangementBeneficiaire> getChangementsByEmploye(@PathVariable Long employeId) {
        Employe employe = new Employe();
        employe.setId(employeId);
        return changementRepository.findByEmploye(employe);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Obtenir les changements par statut", description = "Retourne toutes les demandes de changement ayant un statut spécifique")
    public List<ChangementBeneficiaire> getChangementsByStatus(@PathVariable String status) {
        return changementRepository.findByStatus(status);
    }
    
    @GetMapping("/periode")
    @Operation(summary = "Obtenir les changements sur une période", description = "Retourne les demandes de changement effectuées entre deux dates")
    public List<ChangementBeneficiaire> getChangementsByPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        return changementRepository.findByDateChangementBetween(dateDebut, dateFin);
    }

    @GetMapping("/recents")
    @Operation(summary = "Obtenir les changements récents", description = "Retourne les dernières demandes de changement, limité au nombre spécifié")
    public List<ChangementBeneficiaire> getDerniersChangements(@RequestParam(defaultValue = "10") int limite) {
        return changementRepository.findLastChangements(PageRequest.of(0, limite));
    }

    @GetMapping("/stats/status/{status}")
    @Operation(summary = "Obtenir le nombre de changements par statut", description = "Retourne le nombre total de demandes ayant un statut spécifique")
    public ResponseEntity<Long> getCountByStatus(@PathVariable String status) {
        return ResponseEntity.ok(changementRepository.countByStatus(status));
    }

    @GetMapping("/contrat/{numeroContrat}")
    @Operation(summary = "Obtenir les changements par numéro de contrat", description = "Retourne les changements associés à un contrat d'assurance spécifique")
    public List<ChangementBeneficiaire> getChangementsByContrat(@PathVariable String numeroContrat) {
        return changementRepository.findByNumeroContratAssurance(numeroContrat);
    }
    
    private void sendWhatsAppNotification(ChangementBeneficiaire changement, String message, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Long employeId = changement.getEmploye().getId();
        
        try {
            ResponseEntity<Employe> employeResponse = restTemplate.getForEntity(
                "http://localhost:8080/api/employes/" + employeId, 
                Employe.class
            );
            
            if (employeResponse.getStatusCode() == HttpStatus.OK && employeResponse.getBody() != null) {
                Employe employe = employeResponse.getBody();
                String telephone = employe.getTelephone();
                
                if (telephone != null && !telephone.isEmpty()) {
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("message", message);
                    notificationData.put("type", type);
                    notificationData.put("employeId", employeId.toString());
                    notificationData.put("telephone", telephone);
                    
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(notificationData, headers);
                    restTemplate.postForEntity(
                        NODE_BASE_URL + "/api/notifications/whatsapp",
                        request,
                        Object.class
                    );
                    
                    System.out.println("Notification WhatsApp envoyée au numéro: " + telephone);
                } else {
                    System.out.println("Aucun numéro de téléphone disponible pour l'employé " + employeId);
                }
            } else {
                System.out.println("Impossible de récupérer les informations de l'employé " + employeId);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification WhatsApp: " + e.getMessage());
        }
    }
}