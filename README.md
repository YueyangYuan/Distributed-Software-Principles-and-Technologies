# Distributed-Software-Principles-and-Technologies

基于 `Spring Boot 3`、`MyBatis-Plus`、`Redis`、`Kafka`、`Nacos`、`Spring Cloud Gateway`、`Nginx` 的分布式秒杀课程项目。

当前仓库已经补齐并跑通了以下核心能力：

- 用户、商品、库存、订单四个核心微服务
- Docker Compose 一键启动
- Nginx 负载均衡和动静分离
- Nacos 服务注册与发现
- Spring Cloud Gateway 动态路由
- Redis 商品缓存
- Redis 预扣库存
- Kafka 异步下单与最终一致性
- 订单雪花算法 ID
- 商品服务代码级读写路由
- 网关限流与熔断降级

## 快速启动

1. 复制环境变量模板

```powershell
Copy-Item .env.example .env
```

2. 打包项目

```powershell
mvn -q -DskipTests package
```

3. 启动全部服务

```powershell
docker compose up -d --build
```

## 默认访问地址

- 前端页面: `http://localhost:18080`
- Gateway: `http://localhost:18081`
- Nacos: `http://localhost:18848/nacos`
- MySQL Master: `localhost:13306`
- MySQL Slave: `localhost:13307`
- Redis: `localhost:16379`
- Kafka: `localhost:19092`

## 目录说明

- `seckill-user`: 用户注册、登录、JWT 鉴权
- `seckill-product`: 商品列表、商品详情、Redis 缓存、读写路由
- `seckill-inventory`: 库存预热、库存扣减、库存回滚
- `seckill-order`: 秒杀下单、订单查询、支付、Kafka 消息发送
- `seckill-gateway`: Gateway 动态路由、JWT 透传、限流、熔断
- `seckill-common`: 通用实体、DTO、异常、工具类、雪花算法
- `frontend`: 演示前端
- `nginx`: Nginx 负载均衡与动静分离配置
- `sql`: 初始化脚本
- `docs`: 课程文档、要求核对和运行记录

## 文档

- 系统设计文档: `docs/system-design.md`
- 作业要求核对: `docs/requirement-checklist.md`
- 本地运行记录: `docs/run-verification.md`

## 已验证的关键结果

- `mvn -q -DskipTests package` 可通过
- `docker compose up -d --build` 可启动完整环境
- `http://localhost:18081/actuator/health` 返回 `UP`
- `Nacos` 中可看到用户、订单、商品服务实例
- `http://localhost:18080/api/user/health` 可通过 `Nginx` 轮询访问到两个用户服务实例
- `GET /api/product/datasource/read-route` 返回 `SLAVE`
- `GET /api/product/datasource/write-route` 返回 `MASTER`

## 当前仍未完全覆盖的项

- 没有补完整的 `JMeter` 脚本和压测报告
- `Nacos` 动态配置刷新没有做成完整作业展示
- 分库分表、ElasticSearch 仍属于可选项，当前未继续扩展
