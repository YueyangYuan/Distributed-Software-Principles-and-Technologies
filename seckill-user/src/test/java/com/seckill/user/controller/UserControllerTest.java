package com.seckill.user.controller;

import com.seckill.common.dto.Result;
import com.seckill.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UserControllerTest {

    @Test
    void healthReturnsInstanceMetadataForLoadBalancingVerification() {
        UserController controller = new UserController(Mockito.mock(UserService.class));
        ReflectionTestUtils.setField(controller, "instanceId", "user-service-1");

        Result<?> result = controller.health();

        assertEquals(200, result.getCode());
        Map<?, ?> payload = assertInstanceOf(Map.class, result.getData());
        assertEquals("UP", payload.get("status"));
        assertEquals("seckill-user", payload.get("service"));
        assertEquals("user-service-1", payload.get("instance"));
    }
}
