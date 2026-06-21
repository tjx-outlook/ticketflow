package com.yourname.ticketflow.modules.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.modules.event.entity.Event;
import com.yourname.ticketflow.modules.event.mapper.EventMapper;
import com.yourname.ticketflow.modules.order.dto.SeckillResultVO;
import com.yourname.ticketflow.modules.order.entity.OrderInfo;
import com.yourname.ticketflow.modules.order.entity.OrderItem;
import com.yourname.ticketflow.modules.order.mapper.OrderInfoMapper;
import com.yourname.ticketflow.modules.order.mapper.OrderItemMapper;
import com.yourname.ticketflow.modules.order.service.OrderService;
import com.yourname.ticketflow.modules.seat.entity.Seat;
import com.yourname.ticketflow.modules.seat.mapper.SeatMapper;
import com.yourname.ticketflow.modules.ticket.entity.Ticket;
import com.yourname.ticketflow.modules.ticket.mapper.TicketMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final EventMapper eventMapper;
    private final SeatMapper seatMapper;
    private final TicketMapper ticketMapper;
    private final ExecutorService orderAsyncExecutor;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RedissonClient redissonClient;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:users:";
    private static final String SECKILL_LOCK_KEY = "seckill:lock:";
    private static final long PAYMENT_TIMEOUT_MINUTES = 10;

    public OrderServiceImpl(OrderInfoMapper orderInfoMapper,
                            OrderItemMapper orderItemMapper,
                            EventMapper eventMapper,
                            SeatMapper seatMapper,
                            TicketMapper ticketMapper,
                            @Qualifier("orderAsyncExecutor") ExecutorService orderAsyncExecutor) {
        this.orderInfoMapper = orderInfoMapper;
        this.orderItemMapper = orderItemMapper;
        this.eventMapper = eventMapper;
        this.seatMapper = seatMapper;
        this.ticketMapper = ticketMapper;
        this.orderAsyncExecutor = orderAsyncExecutor;
    }

    @Override
    public SeckillResultVO seckill(Long eventId, Long userId, List<Long> seatIds) {
        if (redisTemplate == null || redissonClient == null) {
            return SeckillResultVO.fail("Redis 未启动，秒杀功能暂不可用");
        }

        Event event = eventMapper.selectById(eventId);
        if (event == null) return SeckillResultVO.fail("活动不存在");
        if (event.getStatus() != 1) return SeckillResultVO.fail("活动未在热卖中");

        // 未指定座位 → 自动选1个最优
        if (seatIds == null || seatIds.isEmpty()) {
            Seat autoSeat = seatMapper.selectOneAvailable(eventId);
            if (autoSeat == null) return SeckillResultVO.fail("无可用座位");
            seatIds = List.of(autoSeat.getId());
        }

        if (seatIds.size() > 5) {
            return SeckillResultVO.fail("每次最多选择5个座位");
        }

        // 每人每场限购1单 — Redis 快速检查 + DB 兜底验证
        String userKey = SECKILL_USER_KEY + eventId;
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userKey, userId.toString()))) {
            // Redis 说买过 → 去 DB 确认是否真的存在已支付订单
            if (hasPaidOrder(eventId, userId)) {
                return SeckillResultVO.fail("您已购买过该活动，每人限购1单");
            }
            // DB 没有已支付订单 → Redis 脏数据，清理掉，放行
            log.warn("[秒杀] Redis 脏数据清理: userId={}, eventId={}", userId, eventId);
            redisTemplate.opsForSet().remove(userKey, userId.toString());
        }

        int count = seatIds.size();
        String lockKey = SECKILL_LOCK_KEY + eventId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                return SeckillResultVO.fail("系统繁忙，请稍后再试");
            }
            try {
                // 加锁后再验一次（DB 优先）
                if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userKey, userId.toString()))) {
                    if (hasPaidOrder(eventId, userId)) {
                        return SeckillResultVO.fail("您已购买过该活动");
                    }
                    redisTemplate.opsForSet().remove(userKey, userId.toString());
                }

                String stockKey = SECKILL_STOCK_KEY + eventId;
                Long stock = redisTemplate.opsForValue().decrement(stockKey, count);
                if (stock == null || stock < 0) {
                    redisTemplate.opsForValue().increment(stockKey, count);
                    event.setStatus(2);
                    eventMapper.updateById(event);
                    return SeckillResultVO.fail("库存不足，剩余票数不够");
                }

                redisTemplate.opsForSet().add(userKey, userId.toString());
                String orderNo = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + IdUtil.getSnowflakeNextIdStr().substring(10);
                final List<Long> finalSeatIds = seatIds;

                orderAsyncExecutor.submit(() -> {
                    try {
                        createOrderAsync(eventId, userId, orderNo, event.getTenantId(), finalSeatIds);
                    } catch (Exception e) {
                        log.error("异步创建订单失败", e);
                        redisTemplate.opsForValue().increment(stockKey, count);
                        redisTemplate.opsForSet().remove(userKey, userId.toString());
                    }
                });

                log.info("秒杀成功: eventId={}, userId={}, orderNo={}, seats={}, 剩余库存={}",
                        eventId, userId, orderNo, count, stock);
                return SeckillResultVO.success(orderNo, count + "张票出票处理中");

            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SeckillResultVO.fail("系统繁忙");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrderAsync(Long eventId, Long userId, String orderNo, Long tenantId, List<Long> seatIds) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 逐个锁定座位
        for (Long seatId : seatIds) {
            Seat seat = seatMapper.selectById(seatId);
            if (seat == null || seat.getStatus() != 0 || !seat.getEventId().equals(eventId)) {
                throw new BusinessException(ResultCode.SEAT_SOLD, "座位 " + seatId + " 不可用");
            }
            int locked = seatMapper.lockSeat(seatId);
            if (locked == 0) {
                throw new BusinessException(ResultCode.SEAT_SOLD, "座位 " + seatId + " 已被抢走");
            }
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setEventId(eventId);
        order.setTotalAmount(totalAmount);
        order.setSeatCount(seatIds.size());
        order.setStatus(0); // 待支付
        order.setTenantId(tenantId);
        orderInfoMapper.insert(order);

        // 创建订单明细 + 出票
        for (Long seatId : seatIds) {
            Seat seat = seatMapper.selectById(seatId);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(orderNo);
            item.setSeatId(seatId);
            item.setPrice(seat.getPrice());
            orderItemMapper.insert(item);

            seatMapper.markSold(seatId);

            Ticket ticket = new Ticket();
            ticket.setTicketNo(IdUtil.getSnowflakeNextIdStr());
            ticket.setOrderId(order.getId());
            ticket.setOrderNo(orderNo);
            ticket.setUserId(userId);
            ticket.setEventId(eventId);
            ticket.setSeatId(seatId);
            ticket.setQrCode("QR:" + orderNo + ":" + seatId);
            ticket.setStatus(0);
            ticket.setTenantId(tenantId);
            ticketMapper.insert(ticket);
        }

        log.info("订单创建完成: orderNo={}, seats={}, total=¥{}", orderNo, seatIds.size(), totalAmount);
    }

    @Override
    public Page<OrderInfo> page(int current, int size, Long userId) {
        // 查询前先批量作废该用户超时未支付的订单
        expireStaleOrdersForUser(userId);
        return orderInfoMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getUserId, userId)
                        .orderByDesc(OrderInfo::getCreateTime));
    }

    /** 清理用户所有超时未支付的订单 */
    private void expireStaleOrdersForUser(Long userId) {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<OrderInfo> staleOrders = orderInfoMapper.selectList(
            new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getStatus, 0) // 待支付
                .lt(OrderInfo::getCreateTime, deadline));
        for (OrderInfo order : staleOrders) {
            try {
                expireIfStale(order.getOrderNo());
            } catch (Exception e) {
                log.error("批量作废失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    @Override
    public OrderInfo detail(Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, Long userId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);

        // 超时自动取消 → 已由 expireIfStale 处理完成，直接返回
        if (order.getStatus() == 0 && expireIfStale(order.getOrderNo())) {
            return;
        }
        if (order.getStatus() == 2) throw new BusinessException(ResultCode.ORDER_ALREADY_CANCELLED);
        if (order.getStatus() == 3) throw new BusinessException(ResultCode.ORDER_ALREADY_REFUNDED);

        // 已支付 → 退款(3)，待支付 → 取消(2)
        int newStatus = order.getStatus() == 1 ? 3 : 2;

        // 释放座位
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            seatMapper.releaseSeat(item.getSeatId());
            // 删除票
            ticketMapper.delete(new LambdaQueryWrapper<Ticket>().eq(Ticket::getSeatId, item.getSeatId()));
        }

        order.setStatus(newStatus);
        orderInfoMapper.updateById(order);

        // 恢复Redis库存
        if (redisTemplate != null) {
            String stockKey = SECKILL_STOCK_KEY + order.getEventId();
            redisTemplate.opsForValue().increment(stockKey, items.size());
            String userKey = SECKILL_USER_KEY + order.getEventId();
            redisTemplate.opsForSet().remove(userKey, userId.toString());
        }

        log.info("订单{}成功: orderId={}, seats={}", newStatus == 3 ? "退款" : "取消", orderId, items.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByOrderNo(String orderNo, Long userId) {
        OrderInfo order = orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);

        // 超时自动取消
        if (order.getStatus() == 0 && expireIfStale(orderNo)) {
            return;
        }
        if (order.getStatus() == 2) throw new BusinessException(ResultCode.ORDER_ALREADY_CANCELLED);
        if (order.getStatus() == 3) throw new BusinessException(ResultCode.ORDER_ALREADY_REFUNDED);

        int newStatus = order.getStatus() == 1 ? 3 : 2;

        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            seatMapper.releaseSeat(item.getSeatId());
            ticketMapper.delete(new LambdaQueryWrapper<Ticket>().eq(Ticket::getSeatId, item.getSeatId()));
        }

        order.setStatus(newStatus);
        orderInfoMapper.updateById(order);

        if (redisTemplate != null) {
            String stockKey = SECKILL_STOCK_KEY + order.getEventId();
            redisTemplate.opsForValue().increment(stockKey, items.size());
            String userKey = SECKILL_USER_KEY + order.getEventId();
            redisTemplate.opsForSet().remove(userKey, userId.toString());
        }

        log.info("订单{}成功: orderNo={}, seats={}", newStatus == 3 ? "退款" : "取消", orderNo, items.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(String orderNo, Long userId) {
        // 先检查是否超时
        if (expireIfStale(orderNo)) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL.getCode(), "订单已超过支付时限，已自动取消");
        }
        OrderInfo order = orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);
        if (order.getStatus() != 0) throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL.getCode(), "订单状态不允许支付");

        if (hasPaidOrder(order.getEventId(), userId)) {
            throw new BusinessException("每人每场限成功购票1单，您已购买过该活动");
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        log.info("支付确认成功: orderId={}, orderNo={}", order.getId(), orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPaymentByOrderNo(String orderNo) {
        // 超时作废的不接受支付回调
        if (expireIfStale(orderNo)) {
            log.warn("[支付回调] 订单 {} 已超时作废，忽略", orderNo);
            return;
        }
        OrderInfo order = orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (order.getStatus() != 0) {
            log.warn("[支付回调] 订单 {} 状态为 {}，非待支付状态，跳过", orderNo, order.getStatus());
            return; // 幂等：已支付/已取消则不再处理
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        log.info("[支付回调] 订单 {} 支付确认成功", orderNo);
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        return orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
    }

    /**
     * 支付超时自动作废 — 订单创建超过10分钟且状态仍为"待支付"则自动取消
     * @return true=已作废, false=未到期或已处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean expireIfStale(String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) return false;
        if (order.getStatus() != 0) return false; // 非待支付，不管

        // 检查是否超过支付时限（createTime 为 null 视为未超时，新插入的订单会有自动填充）
        if (order.getCreateTime() == null) return false;
        LocalDateTime deadline = order.getCreateTime().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
        if (LocalDateTime.now().isBefore(deadline)) return false;

        // 超时 → 自动取消
        order.setStatus(2); // 已取消
        orderInfoMapper.updateById(order);

        // 释放座位
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            seatMapper.releaseSeat(item.getSeatId());
            ticketMapper.delete(new LambdaQueryWrapper<Ticket>().eq(Ticket::getSeatId, item.getSeatId()));
        }

        // 恢复 Redis
        if (redisTemplate != null) {
            String stockKey = SECKILL_STOCK_KEY + order.getEventId();
            redisTemplate.opsForValue().increment(stockKey, items.size());
            String userKey = SECKILL_USER_KEY + order.getEventId();
            redisTemplate.opsForSet().remove(userKey, order.getUserId().toString());
        }

        log.info("[支付超时] 订单 {} 超过{}分钟未支付，已自动取消", orderNo, PAYMENT_TIMEOUT_MINUTES);
        return true;
    }

    @Override
    public boolean hasPaidOrder(Long eventId, Long userId) {
        return orderInfoMapper.selectCount(
            new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getEventId, eventId)
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getStatus, 1)
        ) > 0;
    }
}
