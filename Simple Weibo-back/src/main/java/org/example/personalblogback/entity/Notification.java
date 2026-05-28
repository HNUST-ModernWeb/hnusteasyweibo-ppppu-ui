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
public class Notification {
    private Long id;
    private Long userId;
    private Long senderId;
    private String type;
    private Long relatedId;
    private String content;
    private Boolean isRead;
    private User sender;
    private LocalDateTime createdAt;
}
