package com.yourname.ticketflow.modules.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.user.dto.*;
import com.yourname.ticketflow.modules.user.entity.SysUser;

/**
 * 用户服务接口
 */
public interface UserService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    UserVO getCurrentUser(Long userId);

    SysUser getById(Long userId);

    Page<SysUser> page(int current, int size, String keyword);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateStatus(Long userId, Integer status);
}
