package com.seckill.inventory.controller;

import com.seckill.common.dto.Result;
import com.seckill.common.entity.Inventory;
import com.seckill.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public Result<Inventory> getStock(@PathVariable Long productId) {
        return inventoryService.getStock(productId);
    }

    @PostMapping
    public Result<Inventory> initStock(@RequestBody Inventory inventory) {
        return inventoryService.initStock(inventory);
    }

    @PostMapping("/preload/{productId}")
    public Result<Void> preloadStock(@PathVariable Long productId) {
        inventoryService.preloadStock(productId);
        return Result.ok();
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("Inventory Service is running");
    }
}
