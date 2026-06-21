package com.yourname.ticketflow.modules.merchant.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.merchant.entity.Merchant;
import com.yourname.ticketflow.modules.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家管理")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "商家列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('merchant:manage')")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.ok(merchantService.page(current, size, keyword));
    }

    @Operation(summary = "商家详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(merchantService.detail(id));
    }

    @Operation(summary = "创建商家")
    @PostMapping
    @PreAuthorize("hasAuthority('merchant:manage')")
    public Result<?> create(@RequestBody Merchant merchant) {
        return Result.ok(merchantService.create(merchant));
    }

    @Operation(summary = "更新商家")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('merchant:manage')")
    public Result<?> update(@PathVariable Long id, @RequestBody Merchant merchant) {
        return Result.ok(merchantService.update(id, merchant));
    }

    @Operation(summary = "删除商家")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('merchant:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        merchantService.delete(id);
        return Result.ok();
    }
}
