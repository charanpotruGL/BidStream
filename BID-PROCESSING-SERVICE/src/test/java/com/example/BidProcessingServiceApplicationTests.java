package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class BidProcessingServiceApplicationTests {

	@Test
	void mainClassIsAnnotatedSpringBootApplication() {
		assertThat(BidProcessingServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
	}
}
