package com.project.bpm.service;

import com.project.bpm.entity.EmployeDTO;
import com.project.bpm.entity.Employe;
import com.project.bpm.entity.User;
import com.project.bpm.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private UserService userService;
    
    @Value("${api.node.assurance.url:http://localhost:3001/api/assurances}")
    private String ASSURANCE_API_URL;
    
    @Value("${api.node.notification.url:http://localhost:3001/api/notifications}")
    private String NOTIFICATION_API_URL;

    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    public Optional<Employe> getEmployeById(Long id) {
        return employeRepository.findById(id);
    }

    @Transactional
    public Employe createEmploye(EmployeDTO employeDTO) {
        Employe employe = new Employe();
        
        employe.setNom(employeDTO.getNom());
        employe.setPrenom(employeDTO.getPrenom());
        employe.setEmail(employeDTO.getEmail());
        employe.setAdresse(employeDTO.getAdresse());
        employe.setTelephone(employeDTO.getTelephone());
        employe.setNumeroEmploye(employeDTO.getNumeroEmploye());
        employe.setDepartement(employeDTO.getDepartement());
        employe.setEstBeneficiaire(employeDTO.isEstBeneficiaire());
        
        String numeroAssurance = employeDTO.getNumeroAssurance();
        if (numeroAssurance == null || numeroAssurance.isEmpty()) {
            numeroAssurance = generateNumeroAssurance(employe.getNom(), employe.getPrenom());
        }
        employe.setNumeroAssurance(numeroAssurance);
        
        Employe savedEmploye = employeRepository.save(employe);
        
        User user = userService.createUserFromEmploye(savedEmploye, employeDTO.getRole());
        
        delegateAssuranceCreation(savedEmploye, numeroAssurance, "CREATION");
        
        notifyEmployeAction(
            savedEmploye.getNumeroEmploye(),
            savedEmploye.getNom(),
            savedEmploye.getPrenom(),
            "CREATION",
            savedEmploye.getTelephone()
        );
        
        return savedEmploye;
    }
    
    private String generateNumeroAssurance(String nom, String prenom) {
        String nomPrefix = (nom != null && nom.length() >= 2) ? nom.substring(0, 2).toUpperCase() : "XX";
        String prenomPrefix = (prenom != null && prenom.length() >= 1) ? prenom.substring(0, 1).toUpperCase() : "X";
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("AS-%s-%s-%s", nomPrefix, prenomPrefix, uuid);
    }
    
    private boolean delegateAssuranceCreation(Employe employe, String numeroContrat, String action) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestMap = new HashMap<>();
            
            Map<String, Object> employeMap = new HashMap<>();
            employeMap.put("id", employe.getId());
            employeMap.put("numeroEmploye", employe.getNumeroEmploye());
            employeMap.put("numeroAssurance", employe.getNumeroAssurance());
            employeMap.put("departement", employe.getDepartement());
            employeMap.put("estBeneficiaire", employe.isEstBeneficiaire());
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", employe.getId());
            userMap.put("nom", employe.getNom());
            userMap.put("prenom", employe.getPrenom());
            userMap.put("email", employe.getEmail());
            userMap.put("adresse", employe.getAdresse());
            userMap.put("telephone", employe.getTelephone());
            
            requestMap.put("employe", employeMap);
            requestMap.put("utilisateur", userMap);
            requestMap.put("numeroContrat", numeroContrat);
            requestMap.put("action", action);
            
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                ASSURANCE_API_URL + "/from-employe", 
                request, 
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Erreur lors de la délégation au microservice d'assurance: " + e.getMessage());
            return false;
        }
    }
    
    private void notifyEmployeAction(String numeroEmploye, String nom, String prenom, String action, String telephone) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> notificationMap = new HashMap<>();
            notificationMap.put("numeroEmploye", numeroEmploye);
            notificationMap.put("nom", nom);
            notificationMap.put("prenom", prenom);
            notificationMap.put("action", action);
            notificationMap.put("message", String.format(
                "Action %s pour l'employé %s %s (n°%s)",
                action, prenom, nom, numeroEmploye
            ));
            
            if (telephone != null && !telephone.isEmpty()) {
                notificationMap.put("telephone", telephone);
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(notificationMap, headers);
            
            restTemplate.postForObject(NOTIFICATION_API_URL + "/employe-action", request, Object.class);
        } catch (Exception e) {
            System.err.println("Impossible d'envoyer la notification via l'API Node: " + e.getMessage());
        }
    }

    @Transactional
    public Optional<Employe> updateEmploye(Long id, EmployeDTO employeDTO) {
        return employeRepository.findById(id).map(employe -> {
            employe.setNom(employeDTO.getNom());
            employe.setPrenom(employeDTO.getPrenom());
            employe.setEmail(employeDTO.getEmail());
            employe.setAdresse(employeDTO.getAdresse());
            employe.setTelephone(employeDTO.getTelephone());
            
            String oldNumeroAssurance = employe.getNumeroAssurance();
            employe.setNumeroEmploye(employeDTO.getNumeroEmploye());
            employe.setDepartement(employeDTO.getDepartement());
            employe.setEstBeneficiaire(employeDTO.isEstBeneficiaire());
            
            String newNumeroAssurance = employeDTO.getNumeroAssurance();
            if (newNumeroAssurance == null || newNumeroAssurance.isEmpty()) {
                if (oldNumeroAssurance == null || oldNumeroAssurance.isEmpty()) {
                    newNumeroAssurance = generateNumeroAssurance(employe.getNom(), employe.getPrenom());
                } else {
                    newNumeroAssurance = oldNumeroAssurance;
                }
            }
            employe.setNumeroAssurance(newNumeroAssurance);
            
            Employe updatedEmploye = employeRepository.save(employe);
            
            if (!newNumeroAssurance.equals(oldNumeroAssurance)) {
                delegateAssuranceCreation(updatedEmploye, newNumeroAssurance, "MODIFICATION");
            }
            
            notifyEmployeAction(
                employe.getNumeroEmploye(),
                employe.getNom(),
                employe.getPrenom(),
                "MODIFICATION",
                employe.getTelephone()
            );
            
            return updatedEmploye;
        });
    }

    @Transactional
    public boolean deleteEmploye(Long id) {
        return employeRepository.findById(id).map(employe -> {
            String numeroEmploye = employe.getNumeroEmploye();
            String nom = employe.getNom();
            String prenom = employe.getPrenom();
            String telephone = employe.getTelephone();
            String numeroAssurance = employe.getNumeroAssurance();
            Long employeId = employe.getId();
            
            employeRepository.delete(employe);
            
            notifyEmployeAction(numeroEmploye, nom, prenom, "SUPPRESSION", telephone);
            
            try {
                if (numeroAssurance != null && !numeroAssurance.isEmpty()) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("numeroContrat", numeroAssurance);
                    requestMap.put("action", "SUPPRESSION");
                    
                    Map<String, Object> employeMap = new HashMap<>();
                    employeMap.put("id", employeId);
                    employeMap.put("numeroEmploye", numeroEmploye);
                    
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("nom", nom);
                    userMap.put("prenom", prenom);
                    userMap.put("telephone", telephone);
                    
                    requestMap.put("employe", employeMap);
                    requestMap.put("utilisateur", userMap);
                    
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);
                    
                    restTemplate.postForObject(
                        ASSURANCE_API_URL + "/from-employe",
                        request,
                        Object.class
                    );
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification de suppression au microservice d'assurance: " + e.getMessage());
            }
            
            return true;
        }).orElse(false);
    }
}