package com.yourname.ticketflow.modules.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.modules.ticket.entity.Ticket;
import com.yourname.ticketflow.modules.ticket.mapper.TicketMapper;
import com.yourname.ticketflow.modules.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;

    @Override
    public Page<Ticket> page(int current, int size, Long userId) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<Ticket>()
                .eq(userId != null, Ticket::getUserId, userId)
                .orderByDesc(Ticket::getCreateTime);
        return ticketMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Ticket detail(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "票不存在");
        }
        return ticket;
    }

    @Override
    @Transactional
    public void checkIn(String ticketNo) {
        Ticket ticket = ticketMapper.selectOne(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getTicketNo, ticketNo));
        if (ticket == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "票不存在");
        }
        if (ticket.getStatus() != 0) {
            throw new BusinessException("票已使用或已退款");
        }
        ticket.setStatus(1); // 已使用
        ticket.setCheckTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
    }
}
