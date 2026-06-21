package com.yourname.ticketflow.modules.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourname.ticketflow.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private String merchantName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String description;
    private Integer status;
}
