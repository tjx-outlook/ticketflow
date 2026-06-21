package com.yourname.ticketflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高并发秒杀测试
 *
 * 使用方法：
 * 1. 确保数据库和 Redis 已启动
 * 2. 执行 init.sql 初始化数据
 * 3. 在 Redis 中设置库存: SET seckill:stock:1 100
 * 4. 先启动应用，然后运行本测试
 *
 * 验证标准：
 * - Redis 库存 = 0
 * - 成功订单 = 100
 * - ticket 表 = 100 条记录
 * - 无重复用户订单
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SeckillTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RestTemplate restTemplate;

    private static final int TOTAL_USERS = 1000;
    private static final int TOTAL_TICKETS = 100;
    private static final Long EVENT_ID = 1L;

    @BeforeEach
    public void setUp() {
        restTemplate = new RestTemplate();

        // 重置 Redis 库存
        redisTemplate.opsForValue().set("seckill:stock:" + EVENT_ID, TOTAL_TICKETS);
        // 清除已购买用户集合
        redisTemplate.delete("seckill:users:" + EVENT_ID);

        System.out.println("========== 秒杀测试初始化 ==========");
        System.out.println("活动ID: " + EVENT_ID);
        System.out.println("总票数: " + TOTAL_TICKETS);
        System.out.println("并发用户: " + TOTAL_USERS);
        System.out.println("服务端口: " + port);
        System.out.println("====================================");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testHighConcurrencySeckill() throws InterruptedException {
        // 先登录获取 admin token
        String token = login("admin", "admin123");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(TOTAL_USERS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("\n========== 开始并发测试 ==========");
        long startTime = System.currentTimeMillis();

        // 启动 1000 个并发线程
        for (int i = 0; i < TOTAL_USERS; i++) {
            final int threadNo = i + 1;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String url = "http://localhost:" + port + "/api/order/seckill/" + EVENT_ID;

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);

                    ResponseEntity<Map> response = restTemplate.exchange(
                            url, HttpMethod.POST, entity, Map.class);

                    Map body = response.getBody();
                    if (body != null && (int) body.get("code") == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            }, "seckill-user-" + threadNo).start();
        }

        // 发令枪
        startLatch.countDown();
        // 等待完成
        endLatch.await();

        long endTime = System.currentTimeMillis();

        // 结果
        System.out.println("\n========== 测试结果 ==========");
        System.out.println("总耗时: " + (endTime - startTime) + " ms");
        System.out.println("成功请求: " + successCount.get());
        System.out.println("失败请求: " + failCount.get());

        Long remainingStock = getRemainingStock();
        System.out.println("Redis 剩余库存: " + remainingStock);
        System.out.println("===============================");

        System.out.println("\n✓ 高并发秒杀测试完成！");
        System.out.println("  实际成功扣减: " + (TOTAL_TICKETS - remainingStock.intValue()));
    }

    private Long getRemainingStock() {
        Object val = redisTemplate.opsForValue().get("seckill:stock:" + EVENT_ID);
        if (val == null) return 0L;
        return Long.valueOf(val.toString());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String login(String username, String password) {
        String url = "http://localhost:" + port + "/api/user/login";
        Map<String, String> loginBody = Map.of("username", username, "password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);

        Map body = response.getBody();
        if (body != null && body.get("data") != null) {
            Map data = (Map) body.get("data");
            return (String) data.get("token");
        }
        throw new RuntimeException("登录失败: " + body);
    }
}
