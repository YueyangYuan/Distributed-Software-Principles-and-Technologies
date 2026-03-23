package com.example.userservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

// Lombok注解：自动生成get/set/toString等方法
@Data
public class User {
    // 对应数据库user表字段
    private Long userId;         // 用户ID（自增主键）
    private String username;     // 用户名（唯一）
    private String password;     // 密码（加密存储）
    private String email;        // 邮箱（唯一）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}