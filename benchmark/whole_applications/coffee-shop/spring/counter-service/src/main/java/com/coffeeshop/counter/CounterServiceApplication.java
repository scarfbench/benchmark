package com.coffeeshop.counter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//without the following two annotations tests did not run
@EntityScan(basePackages = "com.coffeeshop.common.domain") 
@EnableJpaRepositories(basePackages = "com.coffeeshop.counter.store")


public class CounterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CounterServiceApplication.class, args);
	}

}
