package com.west2.service;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAConfig;
import com.wechat.pay.java.core.auth.Credential;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.west2.config.RuisConfig;
import com.west2.entity.vo.PayVO;
import com.west2.utils.HttpRequestUtil;
import com.west2.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.HashMap;

/**
 * @desc 微信相关接口调用服务
 * @date 2022/11/26
 */
@Slf4j
@Service
public class WXService {


    /**
     * @desc 通过前端获取的临时code，获取到微信用户信息
     * @param code  临时code
     * @return
     */
    public JSONObject code2Session(String code) {
        StringBuilder urlBuilder = new StringBuilder("https://api.weixin.qq.com/sns/jscode2session");
        urlBuilder.append('?')
                .append("grant_type=").append("authorization_code").append('&')
                .append("appid=").append(RuisConfig.VXConfig.appId).append('&')
                .append("secret=").append(RuisConfig.VXConfig.appSecret).append('&')
                .append("js_code=").append(code);
        JSONObject result = null;
        try {
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
     * @desc    调用微信支付接口，获取prepay_id
     * @param orderId 订单接口
     * @param userId    用户id
     * @param description   商家描述
     * @param total     支付价格
     * @return
     */
    public String prepay(String orderId, String userId, String description, int total) {
        Config config =
                new RSAConfig.Builder()
                        .merchantId(RuisConfig.VXConfig.mchId)
                        .privateKeyFromPath(RuisConfig.VXConfig.privateKeyPath)
                        .merchantSerialNumber(RuisConfig.VXConfig.serialNumber)
                        .wechatPayCertificatesFromPath(RuisConfig.VXConfig.wechatPayCertificatePath)
                        .build();
        JsapiService service = new JsapiService.Builder().config(config).build();

        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(total);
        request.setAmount(amount);
        request.setAppid(RuisConfig.VXConfig.appId);
        request.setMchid(RuisConfig.VXConfig.mchId);
        request.setDescription(description);
        request.setNotifyUrl("https://notify_url");
        request.setOutTradeNo(orderId);
        Payer payer = new Payer();
        payer.setOpenid(userId);
        request.setPayer(payer);
        PrepayResponse resp = service.prepay(request);

        log.info(resp.getPrepayId());

        return resp.getPrepayId();
    }

}
