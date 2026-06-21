package com.yourname.ticketflow.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建/更新活动请求")
public class EventDTO {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String eventName;

    @NotNull(message = "商家ID不能为空")
    @Schema(description = "商家ID")
    private Long merchantId;

    @NotBlank(message = "场馆不能为空")
    @Schema(description = "演出场馆")
    private String venue;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "海报URL")
    private String posterUrl;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @NotNull(message = "开售时间不能为空")
    @Schema(description = "开售时间")
    private LocalDateTime onSaleTime;

    @Schema(description = "总座位数")
    private Integer totalSeats;
}
