package com.yourname.ticketflow.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yourname.ticketflow.modules.order.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT COUNT(*) FROM order_info WHERE user_id = #{userId} AND event_id = #{eventId} AND status IN (0,1) AND deleted = 0")
    int countUserEventOrders(@Param("userId") Long userId, @Param("eventId") Long eventId);
}
