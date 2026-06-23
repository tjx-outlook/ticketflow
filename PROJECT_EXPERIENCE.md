## 项目经历：TicketFlow 高并发票务系统

·**项目背景**：大型演唱会票务秒杀场景，数万用户同时抢购有限座位，需解决高并发下库存超卖、座位重复分配、支付超时资源浪费等问题。

·**项目介绍**：从零独立完成全栈开发，实现用户端（活动浏览、SVG 交互选座、秒杀下单、支付宝扫码支付、电子票查看）、商家端（活动与座位管理）、管理端（用户/角色/权限控制）三大角色功能模块。

- **秒杀抢票**：通过 Redis 预热库存 + DECR 原子扣减 + Redisson 分布式锁排队 + MySQL 乐观锁（`UPDATE seat SET status=1 WHERE status=0`）三级防护解决超卖；Redis Set 快速去重 + DB 查询已支付订单兜底验证，解决 Redis 脏数据误拦问题；线程池异步落库解决接口响应慢问题；10 分钟支付超时自动取消释放座位与库存，解决资源占用问题。
- **支付宝支付**：手动构建 alipay.trade.precreate 请求并 RSA2 签名，实现异步通知回调接口与验签逻辑；使用 serveo.net 内网穿透解决本地开发无法接收支付宝回调的问题；前端 qrcode 库渲染二维码 + 3 秒轮询支付状态自动跳转，解决支付结果及时感知问题。
- **权限与多租户**：Spring Security + JWT 无状态认证 + RBAC 三级权限模型，每次请求实时查库加载权限，前端路由守卫 + 后端 @PreAuthorize 双重控制；ThreadLocal + HandlerInterceptor 实现多租户数据隔离。
- **前端交互**：Vue 3 Composition API + SVG 纯手写扇形场馆座位图，分区渲染（VIP/内场/看台），实时区分可选/已选/已售/锁定状态；Pinia 管理登录态，Axios 拦截器统一处理 Token 续期、错误提示、静默请求。
- **其他**：雪花 ID 转 String 解决 JavaScript Long 精度丢失导致订单查询失败；全局异常处理 + 统一响应体规范接口返回；OpenAPI 3.0 手写完整接口文档。

·**技术框架**：Spring Boot 3.5 / Spring Security + JWT / MyBatis-Plus 3.5 / Redisson 3.40 / Redis / MySQL / Vue 3 + Vite + Element Plus / 支付宝沙箱 Native 支付
·**项目地址**：https://github.com/tjx-outlook/ticketflow
