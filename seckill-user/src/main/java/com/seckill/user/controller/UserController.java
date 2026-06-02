package com.seckill.user.controller;

import com.seckill.common.dto.Result;
import com.seckill.common.dto.UserLoginDTO;
import com.seckill.common.dto.UserRegisterDTO;
import com.seckill.common.entity.User;
import com.seckill.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @Value("${HOSTNAME:local-user-service}")
    private String instanceId;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "service", "seckill-user",
                "instance", instanceId
        ));
    }
}
