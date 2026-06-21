package com.yourname.ticketflow.modules.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.common.exception.BusinessException;
import com.yourname.ticketflow.common.result.ResultCode;
import com.yourname.ticketflow.modules.merchant.entity.Merchant;
import com.yourname.ticketflow.modules.merchant.mapper.MerchantMapper;
import com.yourname.ticketflow.modules.merchant.service.MerchantService;
import com.yourname.ticketflow.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    @Override
    public Page<Merchant> page(int current, int size, String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Merchant::getMerchantName, keyword);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);
        return merchantMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Merchant detail(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商家不存在");
        }
        return merchant;
    }

    @Override
    @Transactional
    public Merchant create(Merchant merchant) {
        merchant.setTenantId(TenantContext.getTenantId());
        merchantMapper.insert(merchant);
        return merchant;
    }

    @Override
    @Transactional
    public Merchant update(Long id, Merchant merchant) {
        Merchant db = detail(id);
        merchant.setId(id);
        merchant.setTenantId(db.getTenantId());
        merchantMapper.updateById(merchant);
        return merchant;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        merchantMapper.deleteById(id);
    }
}
