package com.yourname.ticketflow.modules.seat.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "座位管理", description = "座位查询及批量创建")
@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "活动座位列表")
    @GetMapping("/list/{eventId}")
    public Result<?> list(@PathVariable Long eventId) {
        return Result.ok(seatService.listByEvent(eventId));
    }

    @Operation(summary = "活动座位分页")
    @GetMapping("/page/{eventId}")
    public Result<?> page(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "100") int size,
                          @PathVariable Long eventId) {
        return Result.ok(seatService.pageByEvent(current, size, eventId));
    }

    @Operation(summary = "批量创建座位（管理员/商家）")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyAuthority('seat:manage','event:manage')")
    public Result<Void> batchCreate(@RequestBody Map<String, Object> params) {
        seatService.batchCreate(
                Long.valueOf(params.get("eventId").toString()),
                params.get("sectionName").toString(),
                Integer.parseInt(params.get("rowCount").toString()),
                Integer.parseInt(params.get("seatsPerRow").toString()),
                new BigDecimal(params.get("price").toString())
        );
        return Result.ok();
    }
}
