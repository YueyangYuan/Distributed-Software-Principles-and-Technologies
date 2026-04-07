package com.example.userservice.service.impl;

import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

// 服务实现类注解
@Service
public class UserServiceImpl implements UserService {

    // 注入Mapper
    @Autowired
    private UserMapper userMapper;

    // 注册：密码加密后入库
    @Override
    public boolean register(User user) {
        String encryptPwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(encryptPwd);
        return userMapper.insertUser(user) > 0;
    }
//
    // 登录：校验用户名和密码
    @Override
    public User login(String username, String password) {
        // 根据用户名查用户
        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            return null; // 用户名不存在
        }
        // 密码加密后对比
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes());
        return encryptPwd.equals(user.getPassword()) ? user : null;
    }
}