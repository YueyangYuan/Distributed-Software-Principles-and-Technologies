# 本地运行记录

记录时间：`2026-06-02`

## 基础构建

- 执行：`mvn -q -DskipTests package`
- 结果：通过

## 容器启动

- 执行：`docker compose up -d --build`
- 结果：完整环境已启动

当前关键端口：

- 前端：`18080`
- Gateway：`18081`
- Nacos：`18848`
- MySQL Master：`13306`
- MySQL Slave：`13307`
- Redis：`16379`
- Kafka：`19092`

## Gateway 健康检查

- 地址：`http://localhost:18081/actuator/health`
- 结果：`{"status":"UP"}`

## Nacos 服务注册

已验证以下服务在 `Nacos` 中可见：

- `seckill-user`：2 个健康实例
- `seckill-order`：2 个健康实例
- `seckill-product`：1 个健康实例

## Nginx 负载均衡验证

调用地址：

- `http://localhost:18080/api/user/health`

健康接口现在会返回实例标识。连续请求 12 次后的统计结果：

```text
241e08112f24:6
ce4d6d43e432:6
```

说明 `Nginx` 已经将请求分发到了两个 `user-service` 实例。

## 商品服务读写路由验证

通过 `Gateway` 调用：

- `GET /api/product/datasource/read-route` 返回 `SLAVE`
- `GET /api/product/datasource/write-route` 返回 `MASTER`

说明商品服务的代码级读写路由已经生效。

## Gateway 路由验证

通过 `Gateway` 调用：

- `POST /api/user/register` 可成功注册用户并返回 token
- `GET /api/product/list?page=1&size=2` 可正常返回商品数据

## 限流验证

对 `POST /api/order/seckill` 做连续请求后，已观测到 `429` 响应，说明网关限流规则已生效。

说明：

- 由于同一用户重复秒杀会先命中业务防重，因此连续请求中除了 `429` 之外，还会出现业务失败返回
- 本次仅保留“限流已经触发”的运行结论，没有继续整理成完整 JMeter 报告
