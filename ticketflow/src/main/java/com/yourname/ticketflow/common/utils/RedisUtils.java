package com.yourname.ticketflow.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类 — 仅在 RedisTemplate 可用时加载
 */
@Component
@ConditionalOnBean(RedisTemplate.class)
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(@Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ============ String 操作 ============

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.valueOf(value.toString());
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // ============ 计数器 ============

    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long incrBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ============ Set 操作 ============

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ============ Hash 操作 ============

    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Collection<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    // ============ 批量删除 ============

    public Long deleteByPattern(String pattern) {
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0L;
    }
}
