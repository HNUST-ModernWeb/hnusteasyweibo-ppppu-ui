package org.example.personalblogback.service.impl;

import org.example.personalblogback.dto.LoginRequest;
import org.example.personalblogback.dto.LoginResponse;
import org.example.personalblogback.dto.RegisterRequest;
import org.example.personalblogback.entity.User;
import org.example.personalblogback.mapper.UserMapper;
import org.example.personalblogback.service.UserService;
import org.example.personalblogback.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId());

        // 清除敏感信息
        user.setPassword(null);

        return LoginResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatar(request.getAvatar())
                .bio("")
                .followers(0)
                .following(0)
                .build();

        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId());

        // 清除敏感信息
        user.setPassword(null);

        return LoginResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public User updateUser(Long userId, String username, String bio, String avatar) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setUsername(username);
        user.setBio(bio);
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userMapper.update(user);

        // 清除敏感信息
        user.setPassword(null);
        return user;
    }
}
