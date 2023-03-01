package com.west2.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class VodClientConfig {

    @Lazy
    @Bean
    public VodClient vodClient(RuisConfig ruisConfig) {
        log.info("config: secreteId: {}",RuisConfig.TencentCloudConfig.secretId);
        log.info("config: secreteKey: {}",RuisConfig.TencentCloudConfig.secretKey);
        log.info("config: api: {}", RuisConfig.ApiConfig.url);
        Credential cred = new Credential(RuisConfig.TencentCloudConfig.secretId, RuisConfig.TencentCloudConfig.secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vod.tencentcloudapi.com");
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        return new VodClient(cred, "", clientProfile);
    }

}
