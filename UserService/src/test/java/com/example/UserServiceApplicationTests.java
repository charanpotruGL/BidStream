package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceApplicationTests {

	@Test
	void mainClassIsAnnotatedSpringBootApplication() {
		assertThat(UserServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
	}
}
