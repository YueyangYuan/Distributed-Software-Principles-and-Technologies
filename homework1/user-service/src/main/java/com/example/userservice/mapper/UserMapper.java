package com.example.userservice.mapper;

import com.example.userservice.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

// MyBatis注解：直接写SQL，无需XML文件
public interface UserMapper {

    // 注册用户：插入用户数据
    @Insert("INSERT INTO user (username, password, email) VALUES (#{username}, #{password}, #{email})")
    int insertUser(User user);

    // 登录：根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectUserByUsername(String username);
}