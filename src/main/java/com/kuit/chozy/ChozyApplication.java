package com.kuit.chozy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChozyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChozyApplication.class, args);
	}

}
