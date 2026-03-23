package com.example.userservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 启动类注解
@SpringBootApplication
// 扫描Mapper接口目录
@MapperScan("com.example.userservice.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        // 启动Spring Boot项目
        SpringApplication.run(UserServiceApplication.class, args);
    }

}