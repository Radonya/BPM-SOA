package com.project.bpm.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.project.bpm.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmployeDeserializer extends JsonDeserializer<Employe> {
    
    private static EmployeRepository employeRepository;
    
    @Autowired
    public void setEmployeRepository(EmployeRepository employeRepository) {
        EmployeDeserializer.employeRepository = employeRepository;
    }
    
    @Override
    public Employe deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String idStr = p.getValueAsString();
        try {
            Long id = Long.parseLong(idStr);
            return employeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé avec ID " + id + " non trouvé"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID Employé invalide: " + idStr);
        }
    }
}