package com.seckill.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillRequestDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
