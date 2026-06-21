package com.yourname.ticketflow.modules.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.modules.event.dto.EventDTO;
import com.yourname.ticketflow.modules.event.entity.Event;
import com.yourname.ticketflow.modules.event.mapper.EventMapper;
import com.yourname.ticketflow.modules.event.service.EventService;
import com.yourname.ticketflow.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public EventServiceImpl(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    @Override
    public Page<Event> page(int current, int size, String keyword) {
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Event::getEventName, keyword);
        }
        wrapper.orderByDesc(Event::getCreateTime);
        return eventMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Event detail(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException(ResultCode.EVENT_NOT_FOUND);
        }
        return event;
    }

    @Override
    @Transactional
    public Event create(EventDTO dto) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event);
        event.setStatus(0);
        event.setTenantId(TenantContext.getTenantId());
        eventMapper.insert(event);

        if (redisTemplate != null && event.getTotalSeats() != null && event.getTotalSeats() > 0) {
            redisTemplate.opsForValue().set("seckill:stock:" + event.getId(), event.getTotalSeats());
        }
        return event;
    }

    @Override
    @Transactional
    public Event update(Long id, EventDTO dto) {
        Event event = detail(id);
        BeanUtils.copyProperties(dto, event, "id", "tenantId", "createTime");
        eventMapper.updateById(event);
        return event;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Event event = detail(id);
        event.setStatus(status);
        eventMapper.updateById(event);

        if (redisTemplate != null && status == 1) {
            String stockKey = "seckill:stock:" + id;
            if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
                redisTemplate.opsForValue().set(stockKey, event.getTotalSeats());
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        eventMapper.deleteById(id);
        if (redisTemplate != null) {
            redisTemplate.delete("seckill:stock:" + id);
            redisTemplate.delete("seckill:users:" + id);
            redisTemplate.delete("seckill:lock:" + id);
        }
    }
}
