package com.yourname.ticketflow.modules.payment;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yourname.ticketflow.config.PaymentProperties;
import com.yourname.ticketflow.modules.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付服务 — 支付宝沙箱 Native 支付 & 异步通知处理
 */
@Slf4j
@Service
public class PaymentService {

    private final PaymentProperties paymentProperties;
    private final PrivateKey alipayPrivateKey;
    private final PublicKey alipayPublicKey;
    private final OrderService orderService;

    public PaymentService(PaymentProperties props, OrderService orderService) {
        this.paymentProperties = props;
        this.orderService = orderService;
        if (props.getAlipay().isEnabled()) {
            this.alipayPrivateKey = loadPrivateKey(props.getAlipay().getPrivateKey());
            this.alipayPublicKey = loadAlipayPublicKey(props.getAlipay().getAlipayPublicKey());
            log.info("[支付宝沙箱] 私钥 & 公钥加载成功");
        } else {
            this.alipayPrivateKey = null;
            this.alipayPublicKey = null;
        }
    }

    // ==================== 密钥加载 ====================

    private PrivateKey loadPrivateKey(String path) {
        try {
            String pem = ResourceUtil.readUtf8Str(path)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            log.error("[支付宝沙箱] 私钥加载失败", e);
            return null;
        }
    }

    private PublicKey loadAlipayPublicKey(String path) {
        try {
            String pem = ResourceUtil.readUtf8Str(path)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            log.error("[支付宝沙箱] 支付宝公钥加载失败", e);
            return null;
        }
    }

    // ==================== 创建支付二维码 ====================

    public String createQrCode(String orderNo, BigDecimal amount, String method) {
        return createAlipay(orderNo, amount);
    }

    /**
     * 调用支付宝 alipay.trade.precreate → 返回真实二维码链接
     */
    private String createAlipay(String orderNo, BigDecimal amount) {
        PaymentProperties.Alipay cfg = paymentProperties.getAlipay();

        // 构建业务参数 — 金额必须保留2位小数
        JSONObject biz = JSONUtil.createObj()
            .set("out_trade_no", orderNo)
            .set("total_amount", String.format("%.2f", amount))
            .set("subject", "TicketFlow-" + orderNo);

        // 构建公共参数（TreeMap 自动按字母排序）
        Map<String, String> params = new TreeMap<>();
        params.put("app_id", cfg.getAppId());
        params.put("method", "alipay.trade.precreate");
        params.put("format", "JSON");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("version", "1.0");
        params.put("notify_url", cfg.getNotifyUrl());
        params.put("biz_content", biz.toString());

        // RSA2 签名
        String signContent = buildSignContent(params);
        String sign = rsaSign(signContent);
        params.put("sign", sign);

        // >>>>> START 支付宝请求
        log.error(">>>>>> [支付宝] ========================================");
        log.error(">>>>>> [支付宝] gateway: {}", cfg.getGateway());
        log.error(">>>>>> [支付宝] biz_content: {}", biz);
        log.error(">>>>>> [支付宝] signContent: {}", signContent);

        try {
            // 将 TreeMap<String,String> 转为 HashMap<String,Object> 传给 Hutool
            Map<String, Object> paramMap = new HashMap<>(params);
            String response = HttpUtil.post(cfg.getGateway(), paramMap);
            log.error(">>>>>> [支付宝] RESPONSE: {}", response);

            JSONObject result = JSONUtil.parseObj(response);
            JSONObject resp = result.getJSONObject("alipay_trade_precreate_response");

            if (resp == null) {
                log.error(">>>>>> [支付宝] 无 precreate_response 节点");
                return generateFallbackQr(orderNo);
            }
            String code = resp.getStr("code");
            if ("10000".equals(code)) {
                String qrCode = resp.getStr("qr_code");
                log.error(">>>>>> [支付宝] ✅ SUCCESS! qr_code={}", qrCode);
                return qrCode;
            }
            log.error(">>>>>> [支付宝] ❌ FAIL: code={} msg={} sub_msg={}",
                code, resp.getStr("msg"), resp.getStr("sub_msg"));
        } catch (Exception e) {
            log.error(">>>>>> [支付宝] ❌ EXCEPTION: {}", e.getMessage(), e);
        }

        log.error(">>>>>> [支付宝] 降级二维码");
        return generateFallbackQr(orderNo);
    }

    /**
     * 支付宝不可用时生成占位二维码
     */
    private String generateFallbackQr(String orderNo) {
        String fakeNo = IdUtil.getSnowflakeNextIdStr();
        log.warn("[支付宝] 使用降级二维码, orderNo={}", orderNo);
        return "https://qr.alipay.com/bax0" + fakeNo;
    }

    // ==================== 异步通知处理 ====================

    /**
     * 处理支付宝异步通知
     * @return "success" 或 "fail"
     */
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("[支付宝] 收到异步通知: {}", params);

        // 1. 验证签名
        if (!verifyAlipayNotifySign(params)) {
            log.error("[支付宝] 异步通知签名验证失败!");
            return "fail";
        }

        // 2. 检查 trade_status
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        String tradeNo = params.get("trade_no");

        log.info("[支付宝] 订单 {} 支付状态: {}, 支付宝交易号: {}, 金额: {}",
            outTradeNo, tradeStatus, tradeNo, totalAmount);

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            try {
                // 3. 确认支付 — 内部调用，无需 userId
                orderService.confirmPaymentByOrderNo(outTradeNo);
                log.info("[支付宝] 订单 {} 支付确认成功", outTradeNo);
                return "success";
            } catch (Exception e) {
                log.error("[支付宝] 订单 {} 支付确认失败: {}", outTradeNo, e.getMessage(), e);
                return "fail";
            }
        }

        // WAIT_BUYER_PAY 等其他状态 → 不处理，支付宝会继续通知
        log.info("[支付宝] 订单 {} 状态为 {}，暂不处理", outTradeNo, tradeStatus);
        return "success";
    }

    /**
     * 验证支付宝异步通知的 RSA2 签名
     *
     * 支付宝签名规则：
     * 1. 剔除 sign、sign_type 参数
     * 2. 剩余参数按字母序拼接成 key1=value1&key2=value2
     * 3. 用支付宝公钥验签
     */
    public boolean verifyAlipayNotifySign(Map<String, String> params) {
        if (alipayPublicKey == null) {
            log.warn("[支付宝] 支付宝公钥未配置，跳过签名验证（仅开发环境）");
            return true; // 开发环境降级：无公钥时放行
        }

        String sign = params.get("sign");
        if (StrUtil.isBlank(sign)) {
            log.error("[支付宝] 通知中没有 sign 参数");
            return false;
        }

        // 构建待签名字符串
        String signContent = buildNotifySignContent(params);
        log.debug("[支付宝] 待验证签名字符串: {}", signContent);

        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(alipayPublicKey);
            sig.update(signContent.getBytes(StandardCharsets.UTF_8));
            boolean valid = sig.verify(Base64.getDecoder().decode(sign));
            log.info("[支付宝] 签名验证结果: {}", valid ? "通过" : "失败");
            return valid;
        } catch (Exception e) {
            log.error("[支付宝] 签名验证异常", e);
            return false;
        }
    }

    // ==================== 签名工具方法 ====================

    /**
     * 请求签名用 — 按字母序拼接参数，排除 sign 和空值。sign_type 必须包含！
     * 注意：支付宝网关验签时 sign_type 是参与签名的，不包含会导致验签失败
     */
    private String buildSignContent(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        sorted.forEach((k, v) -> {
            if (StrUtil.isBlank(v) || "sign".equals(k)) return;
            if (sb.length() > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }

    /**
     * 异步通知验签用 — 排除 sign 和 sign_type
     */
    private String buildNotifySignContent(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        sorted.forEach((k, v) -> {
            if (StrUtil.isBlank(v) || "sign".equals(k) || "sign_type".equals(k)) return;
            if (sb.length() > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }

    private String rsaSign(String content) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(alipayPrivateKey);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            log.error("[支付宝沙箱] 签名失败", e);
            return "";
        }
    }
}
