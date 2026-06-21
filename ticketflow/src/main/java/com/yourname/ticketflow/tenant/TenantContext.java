package com.yourname.ticketflow.tenant;

/**
 * 租户上下文 - 通过 ThreadLocal 存储当前请求的租户ID
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }

    /**
     * 清除租户ID（防止内存泄漏）
     */
    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
