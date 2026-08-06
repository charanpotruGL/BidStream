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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        return buildAuthResponse(saved, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is deactivated");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user, token);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    public User toEntity(UserResponse response) {
        return User.builder()
                .id(response.getId())
                .username(response.getUsername())
                .email(response.getEmail())
                .fullName(response.getFullName())
                .role(response.getRole())
                .active(response.isActive())
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtTokenProvider.getExpiration())
                .build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
