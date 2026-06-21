package com.yourname.ticketflow.modules.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yourname.ticketflow.modules.seat.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    @Select("SELECT * FROM seat WHERE event_id = #{eventId} AND status = 0 AND deleted = 0 ORDER BY price ASC LIMIT 1")
    Seat selectOneAvailable(@Param("eventId") Long eventId);

    @Update("UPDATE seat SET status = 1 WHERE id = #{seatId} AND status = 0")
    int lockSeat(@Param("seatId") Long seatId);

    @Update("UPDATE seat SET status = 2 WHERE id = #{seatId}")
    int markSold(@Param("seatId") Long seatId);

    @Update("UPDATE seat SET status = 0 WHERE id = #{seatId}")
    int releaseSeat(@Param("seatId") Long seatId);
}
