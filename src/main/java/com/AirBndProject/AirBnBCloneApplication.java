package com.AirBndProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirBnBCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirBnBCloneApplication.class, args);
	}

}
