package org.example.personalblogback.service;

import org.example.personalblogback.dto.LoginRequest;
import org.example.personalblogback.dto.LoginResponse;
import org.example.personalblogback.dto.RegisterRequest;
import org.example.personalblogback.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    User getUserById(Long id);
    User updateUser(Long userId, String username, String bio, String avatar);
}
