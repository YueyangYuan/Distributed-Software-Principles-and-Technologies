package com.seckill.product.controller;

import com.seckill.common.dto.Result;
import com.seckill.common.entity.Product;
import com.seckill.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/list")
    public Result<List<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.listProducts(page, size);
    }

    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @DeleteMapping("/cache/{id}")
    public Result<Void> evictCache(@PathVariable Long id) {
        productService.evictCache(id);
        return Result.ok();
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("Product Service is running");
    }
}
