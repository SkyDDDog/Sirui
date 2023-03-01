package com.west2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @desc 封装并读取wx&stream&api配置文件
 * @date 2022/11/27
 */
//@Component
@Configuration
public class RuisConfig {

    @Component
    public static class ApiConfig {

        public static String url;
        public static String username;
        public static String pwd;
        public static String statusUrl;
        public static String overSoccerURL;
        public static String overBasketURL;


        @Value("${ruis.api.url}")
        public void setUrl(String url) {
            ApiConfig.url = url;
        }
        @Value("${ruis.api.username}")
        public void setUsername(String username) {
            ApiConfig.username = username;
        }
        @Value("${ruis.api.pwd}")
        public void setPwd(String pwd) {
            ApiConfig.pwd = pwd;
        }
        @Value("${ruis.api.status}")
        public void setStatusUrl(String statusUrl) {
            ApiConfig.statusUrl = statusUrl;
        }
        @Value("${ruis.api.overSoccerURL}")
        public void setOverSoccerURL(String overSoccerURL) {
            ApiConfig.overSoccerURL = overSoccerURL;
        }
        @Value("${ruis.api.overBasketURL}")
        public void setOverBasketURL(String overBasketURL) {
            ApiConfig.overBasketURL = overBasketURL;
        }
    }

    @Component
    public static class StreamConfig {

        public static String key;
        public static String protocol;
        public static String timeshiftURL;
        public static String pushURL;
        public static String playURL;
        public static String suffix;

        @Value("${ruis.stream.key}")
        public void setKey(String key) {
            StreamConfig.key = key;
        }
        @Value("${ruis.stream.protocol}")
        public void setProtocol(String protocol) {
            StreamConfig.protocol = protocol;
        }
        @Value("${ruis.stream.timeshiftURL}")
        public void setTimeshiftURL(String timeshiftURL) {
            StreamConfig.timeshiftURL = timeshiftURL;
        }
        @Value("${ruis.stream.pushURL}")
        public void setPushURL(String pushURL) {
            StreamConfig.pushURL = pushURL;
        }
        @Value("${ruis.stream.playURL}")
        public void setPlayURL(String playURL) {
            StreamConfig.playURL = playURL;
        }
        @Value("${ruis.stream.suffix}")
        public void setSuffix(String suffix) {
            StreamConfig.suffix = suffix;
        }
    }

    @Component
    public static class VXConfig {

        public static String appId;
        public static String appSecret;
        public static String mchId;
        public static String serialNumber;
        public static String APIv3;
        public static String privateKeyPath;
        public static String wechatPayCertificatePath;

        @Value("${ruis.wx.app.id}")
        public void setAppId(String appId) {
            VXConfig.appId = appId;
        }
        @Value("${ruis.wx.app.secret}")
        public void setAppSecret(String appSecret) {
            VXConfig.appSecret = appSecret;
        }
        @Value("${ruis.wx.pay.mchid}")
        public void setMchId(String mchId) {
            VXConfig.mchId = mchId;
        }
        @Value("${ruis.wx.pay.serialNumber}")
        public void setSerialNumber(String serialNumber) {
            VXConfig.serialNumber = serialNumber;
        }
        @Value("${ruis.wx.pay.APIv3}")
        public void setAPIv3(String APIv3) {
            VXConfig.APIv3 = APIv3;
        }
        @Value("${ruis.wx.pay.privateKeyPath}")
        public void setPrivateKeyPath(String privateKeyPath) {
            VXConfig.privateKeyPath = privateKeyPath;
        }
        @Value("${ruis.wx.pay.wechatPayCertificatePath}")
        public void setWechatPayCertificatePath(String wechatPayCertificatePath) {
            VXConfig.wechatPayCertificatePath = wechatPayCertificatePath;
        }
    }

    @Component
    public static class KuaishouConfig {
        public static String appId;
        public static String appSecret;

        @Value("${ruis.kuaishou.app.id}")
        public void setAppId(String appId) {
            KuaishouConfig.appId = appId;
        }
        @Value("${ruis.kuaishou.app.secret}")
        public void setAppSecret(String appSecret) {
            KuaishouConfig.appSecret = appSecret;
        }
    }

    @Component
    public static class TencentCloudConfig {

        public static String secretId;
        public static String secretKey;

        @Value("${ruis.tencentCloud.SecretId}")
        public void setSecretId(String secretId) {
            TencentCloudConfig.secretId = secretId;
        }

        @Value("${ruis.tencentCloud.SecretKey}")
        public void setSecretKey(String secretKey) {
            TencentCloudConfig.secretKey = secretKey;
        }
    }




}
