package com.yourname.ticketflow.modules.payment;

import cn.hutool.core.util.IdUtil;
import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.order.entity.OrderInfo;
import com.yourname.ticketflow.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付接口 — 支付宝沙箱 / 微信模拟
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 调试接口 — 测试支付宝 API 连通性
     * GET /api/payment/debug?amount=1.00
     * 返回完整的请求参数和支付宝响应
     */
    @GetMapping("/debug")
    public Result<Map<String, Object>> debug(@RequestParam(defaultValue = "0.01") String amount) {
        String orderNo = "DEBUG" + System.currentTimeMillis();
        String qrUrl = paymentService.createQrCode(orderNo, new BigDecimal(amount), "alipay");
        // 假二维码都是 bax0 + 19位纯数字，真的二维码 bax0 后面混合字母数字
        boolean isReal = qrUrl.contains("bax0") && !qrUrl.matches(".*bax0\\d{15,}.*");
        return Result.ok(Map.of(
            "orderNo", orderNo,
            "amount", amount,
            "qrUrl", qrUrl,
            "isRealQr", isReal
        ));
    }

    /**
     * 创建支付 → 调用支付宝 precreate 返回真实二维码
     * 前端传 orderNo（秒杀返回的）+ amount + method
     */
    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        String method = body.get("method").toString(); // alipay
        String amount = body.getOrDefault("amount", "0").toString();
        String orderNo = body.getOrDefault("orderNo", "TF" + IdUtil.getSnowflakeNextIdStr()).toString();

        String qrUrl = paymentService.createQrCode(orderNo, new BigDecimal(amount), method);

        return Result.ok(Map.of(
            "qrUrl", qrUrl,
            "orderNo", orderNo,
            "method", method,
            "amount", amount
        ));
    }

    /**
     * 支付宝异步通知回调 — 支付宝支付成功后 POST 到此接口
     *
     * 公网可达: https://xxxx.serveo.net/api/payment/alipay/notify
     * 收到通知后验签 → 确认订单 → 返回 "success"
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        // 解析支付宝 POST 的所有参数
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }

        log.info("[支付宝通知] 收到异步回调, 参数数量: {}", params.size());
        String result = paymentService.handleAlipayNotify(params);
        log.info("[支付宝通知] 处理结果: {}", result);
        return result;
    }

    /**
     * 查询支付状态 — 前端轮询此接口判断是否支付成功
     * @return status: 0=待支付, 1=已支付, 2=已取消, 3=已退款
     */
    @GetMapping("/status/{orderNo}")
    public Result<Map<String, Object>> queryStatus(@PathVariable String orderNo) {
        try {
            // 先检查超时作废
            orderService.expireIfStale(orderNo);
            OrderInfo order = orderService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }
            return Result.ok(Map.of(
                "orderNo", order.getOrderNo(),
                "status", order.getStatus(),
                "payTime", order.getPayTime() != null ? order.getPayTime().toString() : null
            ));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }
}
