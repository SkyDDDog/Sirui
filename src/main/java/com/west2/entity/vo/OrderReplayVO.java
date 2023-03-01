package com.west2.entity.vo;

import com.west2.entity.OrderReplay;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class OrderReplayVO{

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "文件id")
    private String fileId;

    @ApiModelProperty(value = "视频标题")
    private String title;

    @ApiModelProperty(value = "回放地址")
    private String replay;

    @ApiModelProperty(value = "回放类型")
    private String type;

    @ApiModelProperty(value = "主队")
    private String teamA;

    @ApiModelProperty(value = "客队")
    private String teamB;

    @ApiModelProperty(value = "比赛名称")
    private String gameName;

    @ApiModelProperty(value = "比赛开始时间")
    private String startTime;

    @ApiModelProperty(value = "是否收藏")
    private Boolean isCollected;

    @Override
    public String toString() {
        return "OrderReplayVO{" +
                "id='" + id + '\'' +
                ", orderId='" + orderId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", title='" + title + '\'' +
                ", replay='" + replay + '\'' +
                ", type='" + type + '\'' +
                ", teamA='" + teamA + '\'' +
                ", teamB='" + teamB + '\'' +
                ", gameName='" + gameName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", isCollected=" + isCollected +
                '}';
    }
}
