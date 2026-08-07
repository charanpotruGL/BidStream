package com.example.service;

import com.example.DTO.AuthResponse;
import com.example.DTO.LoginRequest;
import com.example.DTO.RegisterRequest;
import com.example.DTO.UserResponse;
import com.example.config.JwtTokenProvider;
import com.example.exception.InvalidCredentialsException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtTokenProvider);
    }

    private RegisterRequest registerRequest(String username, String email) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("secret123");
        request.setFullName("Alice Doe");
        request.setRole("SELLER");
        return request;
    }

    private User userWithId(Long id) {
        return User.builder()
                .id(id)
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .fullName("Alice Doe")
                .role("USER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequest request = registerRequest("alice", "alice@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtTokenProvider.generateToken(1L, "alice", "SELLER")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(3600000L);

        AuthResponse response = userService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo("SELLER");
        assertThat(response.getExpiresIn()).isEqualTo(3600000L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void register_duplicateUsername_throws() {
        RegisterRequest request = registerRequest("alice", "alice@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("alice");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = registerRequest("alice", "alice@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_nullRole_defaultsToUser() {
        RegisterRequest request = registerRequest("bob", "bob@example.com");
        request.setRole(null);
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("token");
        when(jwtTokenProvider.getExpiration()).thenReturn(0L);

        userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("alice", "secret123");
        User user = userWithId(1L);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "alice", "USER")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        AuthResponse response = userService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("alice");
    }

    @Test
    void login_userNotFound_throws() {
        LoginRequest request = new LoginRequest("ghost", "secret123");
        when(userRepository.findByUsernameOrEmail("ghost", "ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest request = new LoginRequest("alice", "wrong");
        User user = userWithId(1L);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid password");
    }

    @Test
    void login_inactiveUser_throws() {
        LoginRequest request = new LoginRequest("alice", "secret123");
        User user = userWithId(1L);
        user.setActive(false);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void getUserById_success() {
        User user = userWithId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getUserByUsername_success() {
        User user = userWithId(10L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUsername("alice");

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void getUserByUsername_notFound_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("nobody"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nobody");
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(5L)).thenReturn(true);

        userService.deleteUser(5L);

        verify(userRepository).deleteById(5L);
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(5L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void toEntity_mapsResponse() {
        UserResponse response = UserResponse.builder()
                .id(7L)
                .username("alice")
                .email("alice@example.com")
                .fullName("Alice")
                .role("ADMIN")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User user = userService.toEntity(response);

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getRole()).isEqualTo("ADMIN");
        assertThat(user.isActive()).isTrue();
    }
}
