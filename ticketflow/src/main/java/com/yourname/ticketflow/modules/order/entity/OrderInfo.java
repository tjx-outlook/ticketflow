package com.yourname.ticketflow.modules.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderInfo extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long eventId;
    private BigDecimal totalAmount;
    private Integer seatCount;
    private Integer status;
    private LocalDateTime payTime;
    private String remark;
}
