package com.example.service;

import com.example.DTO.AuthResponse;
import com.example.DTO.LoginRequest;
import com.example.DTO.RegisterRequest;
import com.example.DTO.UserResponse;
import com.example.model.User;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    void deleteUser(Long id);

    User toEntity(UserResponse response);
}
