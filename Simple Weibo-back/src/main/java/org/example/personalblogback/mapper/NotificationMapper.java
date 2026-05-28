package org.example.personalblogback.mapper;

import org.apache.ibatis.annotations.*;
import org.example.personalblogback.entity.Notification;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification (user_id, sender_id, type, related_id, content, is_read) " +
            "VALUES (#{userId}, #{senderId}, #{type}, #{relatedId}, #{content}, #{isRead})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "relatedId", column = "related_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "sender", column = "sender_id",
                    one = @One(select = "org.example.personalblogback.mapper.UserMapper.findById"))
    })
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = true WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = true WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);

    @Delete("DELETE FROM notification WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM notification WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Long userId);
}
