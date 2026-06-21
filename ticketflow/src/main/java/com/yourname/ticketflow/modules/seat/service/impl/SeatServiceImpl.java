package com.yourname.ticketflow.modules.seat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.modules.seat.entity.Seat;
import com.yourname.ticketflow.modules.seat.mapper.SeatMapper;
import com.yourname.ticketflow.modules.seat.service.SeatService;
import com.yourname.ticketflow.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public SeatServiceImpl(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @Override
    public Page<Seat> pageByEvent(int current, int size, Long eventId) {
        return seatMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Seat>().eq(Seat::getEventId, eventId));
    }

    @Override
    public List<Seat> listByEvent(Long eventId) {
        return seatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getEventId, eventId));
    }

    @Override
    @Transactional
    public void batchCreate(Long eventId, String sectionName,
                            int rowCount, int seatsPerRow, BigDecimal price) {
        List<Seat> seats = new ArrayList<>();
        Long tenantId = TenantContext.getTenantId();

        for (int r = 1; r <= rowCount; r++) {
            for (int s = 1; s <= seatsPerRow; s++) {
                Seat seat = new Seat();
                seat.setEventId(eventId);
                seat.setSectionName(sectionName);
                seat.setRowNum(String.valueOf(r));
                seat.setSeatNum(String.valueOf(s));
                seat.setPrice(price);
                seat.setStatus(0);
                seat.setTenantId(tenantId);
                seats.add(seat);
            }
        }
        for (Seat seat : seats) {
            seatMapper.insert(seat);
        }
        if (redisTemplate != null) {
            String stockKey = "seckill:stock:" + eventId;
            redisTemplate.opsForValue().increment(stockKey, seats.size());
        }
    }

    @Override
    @Transactional
    public void updatePrice(Long seatId, BigDecimal price) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        seat.setPrice(price);
        seatMapper.updateById(seat);
    }

    @Override
    @Transactional
    public void delete(Long seatId) {
        seatMapper.deleteById(seatId);
    }
}
