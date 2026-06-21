package com.yourname.ticketflow.modules.user.controller;

import com.yourname.ticketflow.common.result.Result;
import com.yourname.ticketflow.modules.user.dto.*;
import com.yourname.ticketflow.modules.user.service.UserService;
import com.yourname.ticketflow.security.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> info(@AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) {
            return Result.fail(401, "未登录");
        }
        return Result.ok(userService.getCurrentUser(userDetails.getUserId()));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal JwtUserDetails userDetails,
                                        @RequestBody Map<String, String> params) {
        userService.updatePassword(userDetails.getUserId(),
                params.get("oldPassword"), params.get("newPassword"));
        return Result.ok();
    }

    @Operation(summary = "用户列表（管理员）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(current, size, keyword));
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/status/{userId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<Void> updateStatus(@PathVariable Long userId,
                                      @RequestBody Map<String, Integer> params) {
        userService.updateStatus(userId, params.get("status"));
        return Result.ok();
    }
}
