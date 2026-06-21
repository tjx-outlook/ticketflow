package com.yourname.ticketflow.modules.event.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.event.dto.EventDTO;
import com.yourname.ticketflow.modules.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "活动管理", description = "活动/演出的CRUD操作")
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "活动列表（公开）")
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.ok(eventService.page(current, size, keyword));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(eventService.detail(id));
    }

    @Operation(summary = "创建活动（管理员/商家）")
    @PostMapping
    @PreAuthorize("hasAuthority('event:manage')")
    public Result<?> create(@Valid @RequestBody EventDTO dto) {
        return Result.ok(eventService.create(dto));
    }

    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('event:manage')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody EventDTO dto) {
        return Result.ok(eventService.update(id, dto));
    }

    @Operation(summary = "更新活动状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('event:manage')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @RequestBody Map<String, Integer> params) {
        eventService.updateStatus(id, params.get("status"));
        return Result.ok();
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('event:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return Result.ok();
    }
}
