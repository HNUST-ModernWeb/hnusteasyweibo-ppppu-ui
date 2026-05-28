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
public class LikeRecord {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}
