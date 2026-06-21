package com.yourname.ticketflow.modules.ticket.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.ticket.service.TicketService;
import com.yourname.ticketflow.security.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "票务管理", description = "票务查询及检票")
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "我的票务列表")
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @AuthenticationPrincipal JwtUserDetails userDetails) {
        return Result.ok(ticketService.page(current, size, userDetails.getUserId()));
    }

    @Operation(summary = "票务详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(ticketService.detail(id));
    }

    @Operation(summary = "检票核销")
    @PutMapping("/check/{ticketNo}")
    @PreAuthorize("hasAuthority('ticket:manage')")
    public Result<Void> checkIn(@PathVariable String ticketNo) {
        ticketService.checkIn(ticketNo);
        return Result.ok();
    }
}
