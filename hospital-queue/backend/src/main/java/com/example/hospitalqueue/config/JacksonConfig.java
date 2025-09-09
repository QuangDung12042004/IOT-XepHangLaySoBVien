package com.example.hospitalqueue.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Module hibernateModule() {
        Hibernate6Module module = new Hibernate6Module();
        // Configure as needed; defaults are fine for most cases
        return module;
    }
}
