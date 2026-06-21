package com.yourname.ticketflow.modules.statistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM order_info WHERE event_id = #{eventId} AND status IN (0,1) AND deleted = 0")
    Long countOrders(@Param("eventId") Long eventId);

    @Select("SELECT COUNT(*) FROM ticket WHERE event_id = #{eventId} AND deleted = 0")
    Long countTickets(@Param("eventId") Long eventId);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM order_info WHERE event_id = #{eventId} AND status IN (0,1) AND deleted = 0")
    BigDecimal sumRevenue(@Param("eventId") Long eventId);

    @Select("SELECT COUNT(*) FROM order_info WHERE deleted = 0")
    Long countAllOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM order_info WHERE status IN (0,1) AND deleted = 0")
    BigDecimal sumAllRevenue();
}
