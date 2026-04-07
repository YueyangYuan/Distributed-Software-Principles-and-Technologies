package com.seckill.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id; // 雪花算法生成
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal orderPrice;
    private Integer quantity;
    private Integer status; // 0-待支付 1-已支付 2-已取消 3-已超时
    private LocalDateTime payTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
