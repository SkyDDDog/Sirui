package com.west2.utils;

import com.alibaba.fastjson.JSONObject;
import com.west2.config.RuisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class StreamUtil {

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static boolean pushRemoteStream(String projectId, String teamA, String teamB, String streamName, long startTime, long stopTime) {
        String streamUrl = StreamUtil.getSafeUrl(streamName, stopTime);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        params.add("username", RuisConfig.ApiConfig.username);
        params.add("pwd", RuisConfig.ApiConfig.pwd);
        params.add("projectID", projectId);
        params.add("starttime", startTime+"");
        params.add("stoptime", stopTime+"");
        params.add("teamA", teamA);
        params.add("teamB", teamB);
        params.add("streamURL", streamUrl);
        JSONObject json = null;
        try {
            json = HttpRequestUtil.postFormUnlencoded(RuisConfig.ApiConfig.url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json != null && (int) json.get("error.code") == 0;
    }


    public static String getSafeUrl(String streamName, long txTime) {
        return StreamUtil.getSafeUrl(RuisConfig.StreamConfig.key,streamName,txTime);
    }

    /**
     * @desc 生成推流地址
     * @param key 鉴权密钥
     * @param streamName 流名称
     * @param txTime    过期时间戳
     * @return 推流地址
     */
    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().
                append(key).
                append(streamName).
                append(Long.toHexString(txTime).toUpperCase()).toString();

        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret  = byteArrayToHexString(
                    messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return txSecret == null ? "" :
                new StringBuilder().
                        append("txSecret=").
                        append(txSecret).
                        append("&").
                        append("txTime=").
                        append(Long.toHexString(txTime).toUpperCase()).
                        toString();
    }

    /**
     * @desc 字节数组转十六进制
     * @param data 字节数组
     * @return 十六进制
     */
    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

}
