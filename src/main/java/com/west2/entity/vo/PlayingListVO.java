package com.west2.entity.vo;


import com.west2.entity.Order;
import lombok.Data;

@Data
public class PlayingListVO extends Order {

    private boolean isStart;
    private String playUrl;


}
