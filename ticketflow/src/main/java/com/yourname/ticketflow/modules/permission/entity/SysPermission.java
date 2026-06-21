package com.yourname.ticketflow.modules.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private Long parentId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private String url;
    private String method;
    private String icon;
    private Integer sort;
    private Integer status;
}
