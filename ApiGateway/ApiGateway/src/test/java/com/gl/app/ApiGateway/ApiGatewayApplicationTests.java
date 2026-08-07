package com.gl.app.ApiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTests {

	@Test
	void mainClassIsAnnotatedSpringBootApplication() {
		assertThat(ApiGatewayApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
	}
}
