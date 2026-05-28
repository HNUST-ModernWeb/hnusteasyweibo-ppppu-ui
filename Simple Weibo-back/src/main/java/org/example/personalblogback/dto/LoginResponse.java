package org.example.personalblogback.dto;

import lombok.Builder;
import lombok.Data;
import org.example.personalblogback.entity.User;

@Data
@Builder
public class LoginResponse {
    private String token;
    private User user;
}
