package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@Slf4j
public class AuctionServiceApplication {

	public static void main(String[] args) {
		log.info("Starting Auction Service Application");
		SpringApplication.run(AuctionServiceApplication.class, args);

		log.info("Auction Service Application started successfully");
	}

}
