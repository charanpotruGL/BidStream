package com.example.controller;

import com.example.DTO.AuthResponse;
import com.example.DTO.LoginRequest;
import com.example.DTO.RegisterRequest;
import com.example.DTO.UserResponse;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void register_returnsCreated() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "secret", "Alice", "USER");
        AuthResponse authResponse = AuthResponse.builder()
                .token("token")
                .tokenType("Bearer")
                .userId(1L)
                .username("alice")
                .role("USER")
                .build();
        when(userService.register(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(authResponse);
    }

    @Test
    void login_returnsOk() {
        LoginRequest request = new LoginRequest("alice", "secret");
        AuthResponse authResponse = AuthResponse.builder().token("token").build();
        when(userService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(authResponse);
    }

    @Test
    void getUserById_returnsOk() {
        UserResponse userResponse = UserResponse.builder().id(1L).username("alice").build();
        when(userService.getUserById(1L)).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(userResponse);
    }

    @Test
    void getUserByUsername_returnsOk() {
        UserResponse userResponse = UserResponse.builder().id(1L).username("alice").build();
        when(userService.getUserByUsername("alice")).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getUserByUsername("alice");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(userResponse);
    }

    @Test
    void deleteUser_returnsNoContent() {
        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(1L);
    }
}
