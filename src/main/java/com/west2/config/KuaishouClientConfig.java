package com.west2.config;

import com.github.kwai.open.api.KwaiOpenLiveApi;
import com.github.kwai.open.api.KwaiOpenOauthApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KuaishouClientConfig {

    @Bean
    public KwaiOpenOauthApi kwaiOpenOauthApi() {
        return KwaiOpenOauthApi.init(RuisConfig.KuaishouConfig.appId);
    }

    @Bean
    public KwaiOpenLiveApi kwaiOpenLiveApi() {
        return KwaiOpenLiveApi.init(RuisConfig.KuaishouConfig.appId);
    }


}
