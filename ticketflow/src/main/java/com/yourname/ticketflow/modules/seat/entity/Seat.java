package com.yourname.ticketflow.modules.seat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 座位
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat")
public class Seat extends BaseEntity {

    private Long eventId;
    private String sectionName;
    private String rowNum;
    private String seatNum;
    private BigDecimal price;
    private Integer status;
}
