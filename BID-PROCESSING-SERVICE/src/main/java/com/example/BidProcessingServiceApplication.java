package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class BidProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidProcessingServiceApplication.class, args);
		log.info("Bid Processing Service started on port 8083");
	}

}
