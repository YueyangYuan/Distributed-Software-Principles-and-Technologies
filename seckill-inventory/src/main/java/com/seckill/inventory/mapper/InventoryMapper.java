package com.seckill.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.common.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("UPDATE t_inventory SET available_stock = available_stock - #{count}, " +
            "locked_stock = locked_stock + #{count} " +
            "WHERE product_id = #{productId} AND available_stock >= #{count}")
    int deductStock(@Param("productId") Long productId, @Param("count") int count);

    @Update("UPDATE t_inventory SET locked_stock = locked_stock - #{count}, " +
            "sold_count = sold_count + #{count} " +
            "WHERE product_id = #{productId} AND locked_stock >= #{count}")
    int confirmDeduct(@Param("productId") Long productId, @Param("count") int count);

    @Update("UPDATE t_inventory SET available_stock = available_stock + #{count}, " +
            "locked_stock = locked_stock - #{count} " +
            "WHERE product_id = #{productId} AND locked_stock >= #{count}")
    int rollbackStock(@Param("productId") Long productId, @Param("count") int count);
}
