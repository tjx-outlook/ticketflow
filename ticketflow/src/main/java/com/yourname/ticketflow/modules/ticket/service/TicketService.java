package com.yourname.ticketflow.modules.ticket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.ticket.entity.Ticket;

public interface TicketService {
    Page<Ticket> page(int current, int size, Long userId);
    Ticket detail(Long id);
    void checkIn(String ticketNo);
}
