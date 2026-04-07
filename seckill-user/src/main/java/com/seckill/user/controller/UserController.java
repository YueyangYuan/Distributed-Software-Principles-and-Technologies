package com.seckill.user.controller;

import com.seckill.common.dto.Result;
import com.seckill.common.dto.UserLoginDTO;
import com.seckill.common.dto.UserRegisterDTO;
import com.seckill.common.entity.User;
import com.seckill.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    public Result<String> health() {
        return Result.ok("User Service is running on port ${server.port}");
    }
}
