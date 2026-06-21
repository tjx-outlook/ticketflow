package com.yourname.ticketflow.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "秒杀结果")
public class SeckillResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "座位信息")
    private String seatInfo;

    public static SeckillResultVO success(String orderNo, String seatInfo) {
        return new SeckillResultVO(true, "抢票成功，订单处理中", orderNo, seatInfo);
    }

    public static SeckillResultVO fail(String message) {
        return new SeckillResultVO(false, message, null, null);
    }
}
