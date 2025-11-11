package com.gold.goldsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoldSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldSystemApplication.class, args);
	}

}
