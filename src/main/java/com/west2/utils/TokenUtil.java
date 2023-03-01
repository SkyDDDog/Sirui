package com.west2.utils;

import com.alibaba.fastjson.JSONObject;
import com.west2.config.RuisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @desc access-token工具类
 * @date 2022/11/26
 */
@Slf4j
@Component
public class TokenUtil {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @desc 请求token
     * @return
     */
    private JSONObject requestAccessToken() {
        JSONObject result = null;
        try {
            StringBuilder urlBuilder = new StringBuilder("https://api.weixin.qq.com/cgi-bin/token");
            String grant_type = "client_credential";
            String appId = RuisConfig.VXConfig.appId;
            String appSecret = RuisConfig.VXConfig.appSecret;
            urlBuilder.append('?')
                    .append("grant_type=").append(grant_type).append('&')
                    .append("appid=").append(appId).append('&')
                    .append("secret=").append(appSecret);
            String url = urlBuilder.toString();
            log.info("请求路径: " + url);
            result = HttpRequestUtil.get(url, null);
            log.info("请求结果: "+result);
        } catch (IOException e) {
            log.error("请求异常");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @desc 请求token并存入redis
     * @return 是否请求成功
     */
    public boolean saveAccessToken() {
        JSONObject json = this.requestAccessToken();
        if (json==null || json.isEmpty()) {
            return false;
        } else {
            String accessToken = (String) json.get("access_token");
            long expires = Long.parseLong(json.get("expires_in").toString());
            this.saveAccessToken(accessToken, expires);
            return true;
        }
    }

    /**
     * @desc 存储access-token 2h
     * @param token access-token
     * @return
     */
    public boolean saveAccessToken(String token, long time) {
        return redisUtil.set("access-token", token, time);
    }

    /**
     * @desc 获取当前access-token
     * @return token
     */
    public String getAccessToken() {
        return (String) redisUtil.get("access-token");
    }

}
