package com.yourname.ticketflow.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long seatId;
    private BigDecimal price;
    private LocalDateTime createTime;
}
