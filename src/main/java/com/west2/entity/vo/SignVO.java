package com.west2.entity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SignVO {

    String nonceStr;
    String paySign;
    String signType;
    String prepay;
    long timeStamp;
    String pack;

}