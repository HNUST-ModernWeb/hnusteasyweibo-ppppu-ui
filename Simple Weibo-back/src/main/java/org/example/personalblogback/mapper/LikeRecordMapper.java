package org.example.personalblogback.mapper;

import org.apache.ibatis.annotations.*;
import org.example.personalblogback.entity.LikeRecord;

@Mapper
public interface LikeRecordMapper {

    @Select("SELECT * FROM like_record WHERE post_id = #{postId} AND user_id = #{userId}")
    LikeRecord findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO like_record (post_id, user_id) VALUES (#{postId}, #{userId})")
    int insert(LikeRecord likeRecord);

    @Delete("DELETE FROM like_record WHERE post_id = #{postId} AND user_id = #{userId}")
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM like_record WHERE post_id = #{postId}")
    long countByPostId(Long postId);
}
