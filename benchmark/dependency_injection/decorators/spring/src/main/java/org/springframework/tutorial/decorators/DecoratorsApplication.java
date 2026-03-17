package org.springframework.tutorial.decorators;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class DecoratorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecoratorsApplication.class, args);
    }
}
