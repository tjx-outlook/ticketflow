package com.yourname.ticketflow.modules.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限关联
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long roleId;
    private Long permissionId;
    private LocalDateTime createTime;
}
