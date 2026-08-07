package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class AuctionServiceApplicationTests {

	@Test
	void mainClassIsAnnotatedSpringBootApplication() {
		assertThat(AuctionServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
	}
}
