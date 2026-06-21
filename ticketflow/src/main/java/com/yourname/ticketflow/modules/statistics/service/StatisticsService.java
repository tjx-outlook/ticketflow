package com.yourname.ticketflow.modules.statistics.service;

import com.yourname.ticketflow.modules.statistics.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    /**
     * 活动销售统计
     */
    public Map<String, Object> salesSummary(Long eventId) {
        return Map.of(
                "totalOrders", statisticsMapper.countOrders(eventId),
                "totalTickets", statisticsMapper.countTickets(eventId),
                "totalRevenue", statisticsMapper.sumRevenue(eventId)
        );
    }

    /**
     * 全部销售统计
     */
    public Map<String, Object> totalSummary() {
        return Map.of(
                "totalOrders", statisticsMapper.countAllOrders(),
                "totalRevenue", statisticsMapper.sumAllRevenue()
        );
    }
}
