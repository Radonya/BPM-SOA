package com.project.bpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

   
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SimplePasswordEncoder();
    }
    
    // Simple password encoder that replaces BCryptPasswordEncoder
    public static class SimplePasswordEncoder implements PasswordEncoder {
        public String encode(String rawPassword) {
          
            return rawPassword;
        }
        
        public boolean matches(String rawPassword, String encodedPassword) {
            return rawPassword.equals(encodedPassword);
        }
    }
} 