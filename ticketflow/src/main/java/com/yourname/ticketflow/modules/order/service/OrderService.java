package com.yourname.ticketflow.modules.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.order.dto.SeckillResultVO;
import com.yourname.ticketflow.modules.order.entity.OrderInfo;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 秒杀抢票（可指定座位列表，1-5个；不指定则自动选最优1个）
     */
    SeckillResultVO seckill(Long eventId, Long userId, List<Long> seatIds);

    Page<OrderInfo> page(int current, int size, Long userId);

    OrderInfo detail(Long orderId);

    void cancel(Long orderId, Long userId);

    /** 通过订单号取消（避免 JS 长整型精度丢失） */
    void cancelByOrderNo(String orderNo, Long userId);

    void confirmPayment(String orderNo, Long userId);

    /**
     * 内部支付确认 — 由支付宝异步通知调用，无需 userId
     */
    void confirmPaymentByOrderNo(String orderNo);

    /**
     * 根据订单号查询订单
     */
    OrderInfo getOrderByOrderNo(String orderNo);

    /**
     * 将超时未支付的订单自动作废（超过10分钟）
     * @return 是否作废了该订单
     */
    boolean expireIfStale(String orderNo);

    boolean hasPaidOrder(Long eventId, Long userId);
}
