package com.example.userservice.controller;

import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 标识为控制器，返回JSON
@RestController
// 接口统一前缀
@RequestMapping("/api/v1/users")
public class UserController {

    // 注入服务
    @Autowired
    private UserService userService;

    // 注册接口：POST请求
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.register(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 500);
            result.put("msg", "注册失败");
        }
        return result;
    }

    // 登录接口：POST请求
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginParam) {
        Map<String, Object> result = new HashMap<>();
        String username = loginParam.get("username");
        String password = loginParam.get("password");
        User user = userService.login(username, password);
        if (user != null) {
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", user);
        } else {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }
}