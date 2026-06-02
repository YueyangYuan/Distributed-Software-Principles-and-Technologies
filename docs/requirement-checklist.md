# 课程作业要求核对清单

说明：

- `已完成`：仓库中已有实现，并且我已经在本地环境中验证过主要链路
- `部分完成`：已经有实现或基础环境，但没有做到完整展示或完整作业级交付
- `未完成`：仓库中当前没有对应实现
- `选做`：原作业中属于可选项

| 要求 | 状态 | 证据 | 说明 |
| --- | --- | --- | --- |
| 系统架构图 | 已完成 | `docs/system-design.md` | 已补充架构说明和模块职责 |
| 服务 API 设计 | 已完成 | `docs/system-design.md`、各服务 `controller` | 已整理主要接口 |
| 数据库 ER 设计 | 已完成 | `docs/system-design.md`、`sql/init.sql` | 已补充数据模型说明 |
| 技术选型说明 | 已完成 | `docs/system-design.md` | 已补充 |
| 初始化项目仓库 | 已完成 | Git 仓库、Maven 多模块结构 | 仓库已可构建 |
| 基础开发环境搭建 | 已完成 | 根 `pom.xml`、各模块 `application.yml` | 基础微服务环境可运行 |
| 用户注册登录 | 已完成 | `seckill-user` | 注册、登录、JWT 已跑通 |
| Dockerfile 与 Docker Compose | 已完成 | 各模块 `Dockerfile`、`docker-compose.yml` | 已统一整理端口和依赖 |
| 后端多实例与 Nginx 负载均衡 | 已完成 | `docker-compose.yml`、`nginx/nginx.conf` | 用户服务双实例、订单服务双实例 |
| 不同负载均衡算法 | 已完成 | `nginx/nginx.conf` | 已使用轮询、`ip_hash`、`least_conn` |
| 简单前端与动静分离 | 已完成 | `frontend/`、`nginx/nginx.conf` | 前端页面可直接访问 |
| Redis 缓存商品详情 | 已完成 | `seckill-product` `ProductService.java` | 已实现缓存、空值缓存、互斥锁、随机 TTL |
| 缓存穿透/击穿/雪崩处理 | 已完成 | `ProductService.java` | 已实现三类策略 |
| Redis 预扣库存 | 已完成 | `seckill-order` `SeckillService.java` | Lua 脚本原子扣减 |
| Kafka 异步削峰下单 | 已完成 | `seckill-order`、`seckill-inventory` | 已实现下单消息和库存消费 |
| 订单 ID 生成 | 已完成 | `seckill-common` `SnowflakeIdGenerator.java` | 已实现雪花算法 |
| 按用户 ID 或订单 ID 查询订单 | 已完成 | `seckill-order` `SeckillController.java` | 两类查询接口已存在 |
| 幂等防重复下单 | 已完成 | `SeckillService.java` | 已做用户维度防重 |
| 最终一致性与防超卖 | 已完成 | `seckill-order`、`seckill-inventory` | Redis 预扣减 + DB 扣减 + Kafka 协调 |
| 秒杀场景下订单与库存服务独立 | 已完成 | `seckill-order`、`seckill-inventory` | 两个独立服务和独立库 |
| 订单支付与状态流转 | 部分完成 | `payOrder`、支付消息链路 | 有基础链路，但没有做完整超时补偿演示 |
| MySQL 主从环境 | 已完成 | `docker-compose.yml` | 已配置 `mysql-master` 和 `mysql-slave` |
| 代码级读写分离 | 已完成 | `seckill-product` 数据源路由代码 | 已支持 `MASTER/SLAVE` 路由验证接口 |
| Nacos 注册与发现 | 已完成 | `docker-compose.yml`、各服务 `application.yml` | 服务已注册到 Nacos |
| Spring Cloud Gateway 动态路由 | 已完成 | `seckill-gateway` | Gateway 已代理各服务 |
| 网关鉴权透传 | 已完成 | `JwtAuthenticationFilter.java` | JWT 校验并透传用户信息 |
| 熔断、限流、降级 | 已完成 | `seckill-gateway/application.yml`、Fallback Controller | 已接入 `Resilience4j` 和 `RequestRateLimiter` |
| JMeter 验证负载均衡 | 部分完成 | `docs/run-verification.md` | 已做实机请求验证，但未补 `.jmx` |
| JMeter 验证流量治理 | 部分完成 | `docs/run-verification.md` | 已验证出现 `429`，但未补完整压测资产 |
| Nacos 动态配置刷新 | 未完成 | 无 | 当前只做了注册发现，未做完整动态配置演示 |
| ElasticSearch 商品搜索 | 选做 / 未完成 | 无 | 当前未实现 |
| 分库分表 | 选做 / 部分完成 | 根 `pom.xml` 有依赖 | 仅保留依赖，未继续落地真实规则 |

## 当前结论

从“能运行、能展示、能交作业”的角度看，当前仓库已经覆盖了大多数核心要求，尤其是：

- 微服务拆分
- Nginx 负载均衡
- Gateway 路由治理
- Nacos 注册发现
- Redis 缓存与库存预扣
- Kafka 异步秒杀链路
- 读写路由

剩下没有完全做满的，主要是：

- JMeter 资产没有补成完整实验包
- Nacos 动态配置刷新没有专门演示
- 选做项没有继续扩展
