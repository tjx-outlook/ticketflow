package com.yourname.ticketflow.modules.seat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.seat.entity.Seat;

import java.math.BigDecimal;
import java.util.List;

public interface SeatService {
    Page<Seat> pageByEvent(int current, int size, Long eventId);
    List<Seat> listByEvent(Long eventId);
    void batchCreate(Long eventId, String sectionName, int rowCount, int seatsPerRow, BigDecimal price);
    void updatePrice(Long seatId, BigDecimal price);
    void delete(Long seatId);
}
