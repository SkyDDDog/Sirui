package com.west2.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Accessors(chain = true)
@ApiModel(value = "OrderRecordVO", description = "OrderRecordVO")
public class OrderRecordVO {

    @ApiModelProperty(value = "订单id")
    private String id;
    @ApiModelProperty(value = "用户微信openid")
    private String openid;
    @ApiModelProperty(value = "场地projectId")
    private String projectId;
    @ApiModelProperty(value = "场地名称")
    private String projectName;
    @ApiModelProperty(value = "队伍A名称")
    private String teamA;
    @ApiModelProperty(value = "队伍B名称")
    private String teamB;
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "封面url")
    private String coverUrl;
    @ApiModelProperty(value = "回放url")
    private String replayUrl;
    @ApiModelProperty(value = "是否支付完成")
    private Boolean payFlag;
    @ApiModelProperty(value = "下单时间")
    private String orderDate;

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(date);
    }


}
