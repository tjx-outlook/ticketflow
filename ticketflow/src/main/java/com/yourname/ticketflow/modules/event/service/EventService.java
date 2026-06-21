package com.yourname.ticketflow.modules.event.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.event.dto.EventDTO;
import com.yourname.ticketflow.modules.event.entity.Event;

public interface EventService {
    Page<Event> page(int current, int size, String keyword);
    Event detail(Long id);
    Event create(EventDTO dto);
    Event update(Long id, EventDTO dto);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
}
