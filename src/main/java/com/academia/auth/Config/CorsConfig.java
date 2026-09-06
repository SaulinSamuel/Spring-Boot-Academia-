package com.academia.auth.Config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed.origins}")
    private List<String> allowedOrigins = new ArrayList<>();

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(allowedOrigins);
        
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
