package com.spiritedhub.spiritedhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.spiritedhub.spiritedhub.repository")
public class SpiritedHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpiritedHubApplication.class, args);
	}

}
