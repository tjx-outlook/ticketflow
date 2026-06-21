package com.yourname.ticketflow.common.result;

import lombok.Getter;

/**
 * 统一响应状态码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXIST(1004, "用户名已存在"),
    TOKEN_INVALID(1005, "Token无效"),
    TOKEN_EXPIRED(1006, "Token已过期"),

    ROLE_NOT_FOUND(2001, "角色不存在"),
    PERMISSION_DENIED(2002, "权限不足"),

    EVENT_NOT_FOUND(3001, "活动不存在"),
    EVENT_NOT_ON_SALE(3002, "活动未开售"),
    EVENT_SOLD_OUT(3003, "活动已售罄"),

    SEAT_NOT_FOUND(4001, "座位不存在"),
    SEAT_SOLD(4002, "座位已售出"),

    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_CREATE_FAIL(5002, "订单创建失败"),
    ORDER_ALREADY_CANCELLED(5003, "订单已取消"),
    ORDER_ALREADY_REFUNDED(5004, "订单已退款"),
    ORDER_CANNOT_CANCEL(5005, "订单不可取消"),

    SECKILL_STOCK_EMPTY(6001, "库存不足"),
    SECKILL_REPEATED(6002, "您已抢过该活动的票"),
    SECKILL_LOCK_FAIL(6003, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
