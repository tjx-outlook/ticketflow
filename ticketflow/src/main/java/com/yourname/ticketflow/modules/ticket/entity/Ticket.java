package com.yourname.ticketflow.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 票务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket")
public class Ticket extends BaseEntity {

    private String ticketNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long eventId;
    private Long seatId;
    private String qrCode;
    private Integer status;
    private LocalDateTime checkTime;
}
