package com.yourname.ticketflow.modules.event.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 活动/演出
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event")
public class Event extends BaseEntity {

    private String eventName;
    private Long merchantId;
    private String venue;
    private String description;
    private String posterUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime onSaleTime;
    private Integer totalSeats;
    private Integer status;
}
