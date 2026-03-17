package org.springframework.tutorial.producerfields.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Configuration
public class DbConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    @UserDatabase
    public EntityManager entityManager() {
        return entityManager;
    }

}
