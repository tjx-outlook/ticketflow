package com.yourname.ticketflow.modules.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.ticketflow.modules.merchant.entity.Merchant;

public interface MerchantService {
    Page<Merchant> page(int current, int size, String keyword);
    Merchant detail(Long id);
    Merchant create(Merchant merchant);
    Merchant update(Long id, Merchant merchant);
    void delete(Long id);
}
