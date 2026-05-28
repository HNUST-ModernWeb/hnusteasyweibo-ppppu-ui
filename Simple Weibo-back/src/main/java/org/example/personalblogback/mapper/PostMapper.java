package org.example.personalblogback.mapper;

import org.apache.ibatis.annotations.*;
import org.example.personalblogback.entity.Post;

import java.util.List;

@Mapper
public interface PostMapper {

    @Select("<script>" +
            "SELECT p.*, u.username as author_username, u.avatar as author_avatar " +
            "FROM post p LEFT JOIN user u ON p.author_id = u.id " +
            "<where>" +
            "<if test='authorId != null'>AND p.author_id = #{authorId}</if>" +
            "</where>" +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "content", column = "content"),
            @Result(property = "authorId", column = "author_id"),
            @Result(property = "images", column = "images", typeHandler = org.example.personalblogback.handler.JsonTypeHandler.class),
            @Result(property = "likes", column = "likes"),
            @Result(property = "reposts", column = "reposts"),
            @Result(property = "commentsCount", column = "comments_count"),
            @Result(property = "views", column = "views"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "author.id", column = "author_id"),
            @Result(property = "author.username", column = "author_username"),
            @Result(property = "author.avatar", column = "author_avatar")
    })
    List<Post> findList(@Param("authorId") Long authorId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM post " +
            "<where>" +
            "<if test='authorId != null'>AND author_id = #{authorId}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("authorId") Long authorId);

    @Select("SELECT p.*, u.username as author_username, u.avatar as author_avatar " +
            "FROM post p LEFT JOIN user u ON p.author_id = u.id " +
            "WHERE p.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "content", column = "content"),
            @Result(property = "authorId", column = "author_id"),
            @Result(property = "images", column = "images", typeHandler = org.example.personalblogback.handler.JsonTypeHandler.class),
            @Result(property = "likes", column = "likes"),
            @Result(property = "reposts", column = "reposts"),
            @Result(property = "commentsCount", column = "comments_count"),
            @Result(property = "views", column = "views"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "author.id", column = "author_id"),
            @Result(property = "author.username", column = "author_username"),
            @Result(property = "author.avatar", column = "author_avatar")
    })
    Post findById(Long id);

    @Insert("INSERT INTO post (content, author_id, images, likes, reposts, comments_count, views) " +
            "VALUES (#{content}, #{authorId}, #{images, typeHandler=org.example.personalblogback.handler.JsonTypeHandler}, " +
            "#{likes}, #{reposts}, #{commentsCount}, #{views})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Post post);

    @Update("UPDATE post SET content = #{content}, images = #{images, typeHandler=org.example.personalblogback.handler.JsonTypeHandler} " +
            "WHERE id = #{id}")
    int update(Post post);

    @Delete("DELETE FROM post WHERE id = #{id}")
    int delete(Long id);

    @Update("UPDATE post SET views = views + 1 WHERE id = #{id}")
    int incrementViews(Long id);

    @Update("UPDATE post SET likes = likes + #{delta} WHERE id = #{id}")
    int updateLikes(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE post SET comments_count = comments_count + #{delta} WHERE id = #{id}")
    int updateCommentsCount(@Param("id") Long id, @Param("delta") int delta);
}
