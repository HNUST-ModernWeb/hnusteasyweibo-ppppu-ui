package org.example.personalblogback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private String content;
    private Long authorId;
    private User author;
    private List<String> images;
    private Integer likes;
    private Integer reposts;
    private Integer commentsCount;
    private Integer views;
    private Boolean liked; // 当前用户是否点赞
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
