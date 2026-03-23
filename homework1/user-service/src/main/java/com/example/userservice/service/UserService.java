package com.example.userservice.service;

import com.example.userservice.entity.User;

// 业务逻辑接口
public interface UserService {
    // 注册方法
    boolean register(User user);
    // 登录方法
    User login(String username, String password);
}