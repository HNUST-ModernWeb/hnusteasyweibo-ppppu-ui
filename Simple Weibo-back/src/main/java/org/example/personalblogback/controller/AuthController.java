package org.example.personalblogback.controller;

import org.example.personalblogback.common.Result;
import org.example.personalblogback.dto.LoginRequest;
import org.example.personalblogback.dto.LoginResponse;
import org.example.personalblogback.dto.RegisterRequest;
import org.example.personalblogback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.register(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
