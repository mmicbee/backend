package com.zone01kisumu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuration class to define RestTemplate bean for dependency injection
@Configuration
public class RestTemplates {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    
}
