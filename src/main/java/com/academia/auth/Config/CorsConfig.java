package com.academia.auth.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource configurationSource() {
        
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:8080"));
        
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        
        configuration.setAllowedHeaders(
            List.of("*"));
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
