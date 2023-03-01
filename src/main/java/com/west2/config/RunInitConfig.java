package com.west2.config;

import com.west2.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @desc 应用启动监听类
 * @date 2022/11/26
 */
@Slf4j
@Component
public class RunInitConfig implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("初始化access-token并存入redis");
        tokenUtil.saveAccessToken();
    }

}
