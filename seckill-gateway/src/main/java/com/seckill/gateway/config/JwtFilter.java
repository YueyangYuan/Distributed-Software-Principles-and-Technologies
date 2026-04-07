package com.seckill.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.Result;
import com.seckill.common.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class JwtFilter implements Filter {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/user/login", "/api/user/register", "/health",
            "/swagger-ui", "/v3/api-docs"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (WHITE_LIST.stream().anyMatch(path::startsWith) || "OPTIONS".equals(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                JwtUtil.parseToken(token);
                chain.doFilter(req, res);
                return;
            } catch (Exception e) {
                log.warn("Token验证失败: {}", e.getMessage());
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(new ObjectMapper().writeValueAsString(Result.fail(401, "未登录或Token已过期")));
    }
}
