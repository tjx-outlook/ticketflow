package com.yourname.ticketflow.modules.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String roleName;
    private String roleCode;
    private String description;
    private Integer sort;
    private Integer status;
}
