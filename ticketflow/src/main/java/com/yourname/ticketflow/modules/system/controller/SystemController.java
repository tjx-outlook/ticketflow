package com.yourname.ticketflow.modules.system.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统管理", description = "字典数据等系统配置")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SysDictService dictService;

    @Operation(summary = "字典类型列表")
    @GetMapping("/dict/types")
    public Result<?> dictTypes() {
        return Result.ok(dictService.listTypes());
    }

    @Operation(summary = "根据类型获取字典数据")
    @GetMapping("/dict/data/{dictType}")
    public Result<?> dictData(@PathVariable String dictType) {
        return Result.ok(dictService.listDataByType(dictType));
    }
}
