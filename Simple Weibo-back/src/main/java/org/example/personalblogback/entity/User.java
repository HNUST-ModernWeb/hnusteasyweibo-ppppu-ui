package org.example.personalblogback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String avatar;
    private String bio;
    private Integer followers;
    private Integer following;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
