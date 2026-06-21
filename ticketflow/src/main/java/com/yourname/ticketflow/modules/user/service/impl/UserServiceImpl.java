package com.yourname.ticketflow.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.common.utils.JwtUtils;
import com.yourname.ticketflow.modules.user.dto.*;
import com.yourname.ticketflow.modules.user.entity.SysUser;
import com.yourname.ticketflow.modules.user.mapper.SysUserMapper;
import com.yourname.ticketflow.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(),
                Map.of("tenantId", user.getTenantId()));

        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        user.setTenantId(1L);

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        SysUser user = getById(userId);
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = userMapper.selectPermissionCodesByUserId(userId);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public SysUser getById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public Page<SysUser> page(int current, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or()
                   .like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return userMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户 {} 修改密码成功", userId);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
