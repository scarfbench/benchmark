package org.springframework.tutorial.guessnumber.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class GeneratorConfig {

    private final int maxNumber = 100;

    @Bean
    public java.util.Random random() {
        return new java.util.Random(System.currentTimeMillis());
    }

    @Bean
    @Scope("prototype")
    @Random
    public Integer next(java.util.Random random) {
        return random.nextInt(maxNumber + 1);
    }

    @Bean
    @MaxNumber
    public Integer maxNumber() {
        return maxNumber;
    }
}
