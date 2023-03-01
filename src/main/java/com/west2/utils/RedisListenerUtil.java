package com.west2.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @desc redis监听工具类
 * @date 2022/11/26
 */
@Slf4j
@Component
public class RedisListenerUtil extends KeyExpirationEventMessageListener {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TokenUtil tokenUtil;

    public RedisListenerUtil(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String key = new String(message.getBody(), StandardCharsets.UTF_8);
            if ("access-token".equals(key)) {
                log.info("access-token更新中...");
                tokenUtil.saveAccessToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
