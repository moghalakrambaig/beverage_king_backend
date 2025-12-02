package com.spiritedhub.spiritedhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.spiritedhub.spiritedhub.jpa.repositories")
@EnableMongoRepositories(basePackages = "com.spiritedhub.spiritedhub.mongo.repositories")
public class SpiritedHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpiritedHubApplication.class, args);
	}

}
