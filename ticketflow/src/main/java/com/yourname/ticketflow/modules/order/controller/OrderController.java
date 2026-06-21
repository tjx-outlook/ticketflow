package com.yourname.ticketflow.modules.order.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.order.dto.SeckillResultVO;
import com.yourname.ticketflow.modules.order.service.OrderService;
import com.yourname.ticketflow.security.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "订单查询及秒杀抢票")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "秒杀抢票", description = "可指定座位ID列表(1-5个)，不指定则自动选最优1个")
    @PostMapping("/seckill/{eventId}")
    public Result<SeckillResultVO> seckill(@PathVariable Long eventId,
                                            @RequestParam(required = false) List<Long> seatIds,
                                            @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) {
            return Result.fail(401, "请先登录");
        }
        SeckillResultVO result = orderService.seckill(eventId, userDetails.getUserId(), seatIds);
        if (Boolean.TRUE.equals(result.getSuccess())) {
            return Result.ok(result);
        }
        return Result.fail(409, result.getMessage());
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @AuthenticationPrincipal JwtUserDetails userDetails) {
        return Result.ok(orderService.page(current, size, userDetails.getUserId()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @Operation(summary = "取消/退款订单")
    @PutMapping("/cancel/{orderNo}")
    public Result<?> cancel(@PathVariable String orderNo,
                            @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) return Result.fail(401, "请先登录");
        orderService.cancelByOrderNo(orderNo, userDetails.getUserId());
        return Result.ok();
    }

    @Operation(summary = "支付确认（模拟/真实）")
    @PutMapping("/pay/{orderNo}")
    public Result<?> confirmPay(@PathVariable String orderNo,
                                @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) return Result.fail(401, "请先登录");
        orderService.confirmPayment(orderNo, userDetails.getUserId());
        return Result.ok();
    }

    @Operation(summary = "检查是否有已支付订单")
    @GetMapping("/check-paid")
    public Result<Boolean> checkPaid(@RequestParam Long eventId,
                                     @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) return Result.ok(false);
        return Result.ok(orderService.hasPaidOrder(eventId, userDetails.getUserId()));
    }
}
