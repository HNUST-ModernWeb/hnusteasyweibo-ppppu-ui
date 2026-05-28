package org.example.personalblogback.mapper;

import org.apache.ibatis.annotations.*;
import org.example.personalblogback.entity.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT c.*, u.username as author_username, u.avatar as author_avatar " +
            "FROM comment c LEFT JOIN user u ON c.author_id = u.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "postId", column = "post_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "authorId", column = "author_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "author.id", column = "author_id"),
            @Result(property = "author.username", column = "author_username"),
            @Result(property = "author.avatar", column = "author_avatar")
    })
    List<Comment> findByPostId(@Param("postId") Long postId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId}")
    long countByPostId(Long postId);

    @Insert("INSERT INTO comment (post_id, content, author_id) " +
            "VALUES (#{postId}, #{content}, #{authorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int delete(Long id);
}
