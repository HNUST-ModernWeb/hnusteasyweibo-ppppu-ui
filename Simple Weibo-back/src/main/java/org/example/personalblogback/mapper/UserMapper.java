package org.example.personalblogback.mapper;

import org.apache.ibatis.annotations.*;
import org.example.personalblogback.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO user (username, password, avatar, bio, followers, following) " +
            "VALUES (#{username}, #{password}, #{avatar}, #{bio}, #{followers}, #{following})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET username = #{username}, avatar = #{avatar}, bio = #{bio} " +
            "WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE user SET followers = followers + #{delta} WHERE id = #{id}")
    int updateFollowers(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE user SET following = following + #{delta} WHERE id = #{id}")
    int updateFollowing(@Param("id") Long id, @Param("delta") int delta);
}
