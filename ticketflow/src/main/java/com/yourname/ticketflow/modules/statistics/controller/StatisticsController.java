package com.yourname.ticketflow.modules.statistics.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "统计报表", description = "销售、订单等统计数据")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "活动销售统计")
    @GetMapping("/sales/{eventId}")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<?> salesSummary(@PathVariable Long eventId) {
        return Result.ok(statisticsService.salesSummary(eventId));
    }

    @Operation(summary = "全部销售统计")
    @GetMapping("/sales/total")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<?> totalSummary() {
        return Result.ok(statisticsService.totalSummary());
    }
}
