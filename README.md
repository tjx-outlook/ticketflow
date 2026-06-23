# TicketFlow 高并发票务系统

大型演唱会门票秒杀系统，Spring Boot 3 + Vue 3 全栈项目。支持活动管理、SVG 交互选座、Redis 分布式锁秒杀、支付宝沙箱支付、RBAC 权限控制、多租户数据隔离。

## 快速启动

**环境要求**：Java 17、Node.js 18+、MySQL 8.x、Redis

```bash
# 1. 导入数据库
mysql -u root -p < ticketflow/src/main/resources/db/init.sql

# 2. 启动后端（默认 8080 端口）
cd ticketflow
mvn spring-boot:run

# 3. 启动前端（默认 3000 端口）
cd ticketflow-ui
npm install
npm run dev

# 4. 打开浏览器
# http://localhost:3000
```

**测试账号**：admin / admin123

## 功能模块

| 角色 | 功能 |
|------|------|
| 普通用户 | 活动浏览、SVG 选座、秒杀下单、支付宝扫码支付、订单管理、电子票查看 |
| 商家 | 活动发布与上下架、座位批量创建与管理 |
| 管理员 | 用户管理、角色分配、权限管理、商家管理、数据统计 |

## 秒杀架构

```
用户请求 → Redis Set 去重检测 → Redisson 分布式锁
→ Redis DECR 原子扣库存 → 线程池异步创建订单
→ MySQL 乐观锁占座 → 返回结果
```

- **防超卖**：Redis 库存 + 分布式锁 + MySQL 乐观锁 三级防护
- **防重复**：Redis Set 快速去重 + DB 已支付订单兜底
- **支付超时**：10 分钟未支付自动取消，释放座位与库存
- **并发测试**：[SeckillTest.java](ticketflow/src/test/java/com/yourname/ticketflow/SeckillTest.java) 1000 线程压力测试

## 支付宝沙箱支付

```bash
# 内网穿透（使支付宝能回调本地）
ssh -R 80:localhost:8080 serveo.net

# 将输出的域名配置到 application.yml
# payment.alipay.notify-url: https://xxxx.serveo.net/api/payment/alipay/notify
```

沙箱版支付宝 App 下载：https://open.alipay.com/develop/sandbox/app

## 技术栈

**后端**：Spring Boot 3.5 / Spring Security + JWT / MyBatis-Plus 3.5 / Redisson 3.40 / Redis / MySQL / Hutool / Knife4j

**前端**：Vue 3 / Vite / Element Plus / Pinia / Vue Router / Axios / QRCode

**接口文档**：[openapi.yaml](openapi.yaml) OpenAPI 3.0 规范

## 项目结构

```
ticketflow/          # 后端 Spring Boot
  src/main/java/.../
    common/          # 统一响应、异常处理、JWT工具
    config/          # Security、Redis、线程池等配置
    security/        # JWT 过滤器与 Spring Security 集成
    tenant/          # 多租户拦截器
    modules/
      event/         # 活动管理
      seat/          # 座位管理（乐观锁 SQL）
      order/         # 订单秒杀（核心）
      payment/       # 支付宝沙箱支付
      ticket/        # 电子票核销
      user/role/permission/  # 用户与权限
      statistics/    # 数据统计

ticketflow-ui/       # 前端 Vue 3
  src/
    views/           # 页面组件
    api/             # Axios 封装
    stores/          # Pinia 状态
    router/          # 路由守卫
```
