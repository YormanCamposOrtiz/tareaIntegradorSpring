package com.novatech.paginaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaginawebApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaginawebApplication.class, args);
	}
}
