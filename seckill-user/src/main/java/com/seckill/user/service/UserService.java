package com.seckill.user.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.dto.Result;
import com.seckill.common.dto.UserLoginDTO;
import com.seckill.common.dto.UserRegisterDTO;
import com.seckill.common.entity.User;
import com.seckill.common.exception.BizException;
import com.seckill.common.util.JwtUtil;
import com.seckill.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public Result<Map<String, Object>> register(UserRegisterDTO dto) {
        // 检查用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) throw new BizException("用户名已存在");

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setSalt(IdUtil.fastSimpleUUID().substring(0, 8));
        user.setPassword(SecureUtil.md5(dto.getPassword() + user.getSalt()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeleted(0);
        userMapper.insert(user);

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        cacheToken(user.getId(), token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        return Result.ok(result);
    }

    public Result<Map<String, Object>> login(UserLoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) throw new BizException("用户不存在");

        String encrypted = SecureUtil.md5(dto.getPassword() + user.getSalt());
        if (!encrypted.equals(user.getPassword())) throw new BizException("密码错误");

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        cacheToken(user.getId(), token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return Result.ok(result);
    }

    public Result<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        user.setPassword(null);
        user.setSalt(null);
        return Result.ok(user);
    }

    private void cacheToken(Long userId, String token) {
        redisTemplate.opsForValue().set("user:token:" + userId, token, 2, TimeUnit.HOURS);
    }
}
