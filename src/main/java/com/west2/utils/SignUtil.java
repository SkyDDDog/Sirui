package com.west2.utils;


import com.wechat.pay.java.core.cipher.Verifier;
import com.wechat.pay.java.core.util.PemUtil;
import com.west2.config.RuisConfig;
import com.west2.entity.vo.SignVO;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SignUtil {

    public SignVO getSign(String prepayId) throws Exception {
        String appId = RuisConfig.VXConfig.appId;
        long timeStamp = DateTimeUtil.nowTimeStamp();
        String nonceStr = String.valueOf(UUID.randomUUID()).replace("-","");
        String pack = "prepay_id="+prepayId;
        String sign = getSign(appId, timeStamp, nonceStr, pack);
        SignVO vo = new SignVO();
        vo.setNonceStr(nonceStr)
                .setPaySign(sign)
                .setSignType("SHA256withRSA")
                .setPrepay(prepayId)
                .setTimeStamp(timeStamp)
                .setPack(pack);
        return vo;
    }

    /**
     * 作用：使用字段appId、timeStamp、nonceStr、package计算得出的签名值
     * 场景：根据微信统一下单接口返回的 prepay_id 生成调启支付所需的签名值
     * @param appId
     * @param timestamp
     * @param nonceStr
     * @param pack package
     * @return
     * @throws Exception
     */
    public String getSign(String appId, long timestamp, String nonceStr, String pack) throws Exception{
        String message = buildMessage(appId, timestamp, nonceStr, pack);
        String paySign= sign(message.getBytes("utf-8"));
        return paySign;
    }

    private String buildMessage(String appId, long timestamp, String nonceStr, String pack) {
        return appId + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + pack + "\n";
    }
    private String sign(byte[] message) throws Exception{
        Signature sign = Signature.getInstance("SHA256withRSA");
        //这里需要一个PrivateKey类型的参数，就是商户的私钥。
//        sign.initSign(this.getPrivateKeyByStr(this.getPrivateKey()));
        sign.initSign(this.getPrivateKey());
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    private PrivateKey getPrivateKey() {
        return PemUtil.loadPrivateKeyFromPath(RuisConfig.VXConfig.privateKeyPath);
    }

}
