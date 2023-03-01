package com.west2.entity.vo;

import com.west2.entity.Order;
import lombok.Data;

@Data
public class OrderShowVO extends Order {

    private String replayUrl;

    private Boolean isCollected;

    private String projectName;

    private String overlay;

}
