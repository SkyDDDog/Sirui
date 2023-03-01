package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import com.west2.entity.vo.OrderShowVO;
import com.west2.utils.BeanCustomUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName("`order`")
@ApiModel(value = "Order", description = "Order")
public class Order extends DataEntity<Order> {

    @ApiModelProperty(value = "父订单id(直播增加时长订单用)")
    private String preOrderId;

    @ApiModelProperty(value = "用户唯一标识")
    private String openid;

    @ApiModelProperty(value = "场地id")
    private String projectId;

    @ApiModelProperty(value = "赛事名称")
    private String gameName;

    @ApiModelProperty(value = "队伍A名称")
    private String teamA;

    @ApiModelProperty(value = "队伍B名称")
    private String teamB;

    @ApiModelProperty(value = "队伍A比分", example = "2")
    private Integer scoreA;

    @ApiModelProperty(value = "队伍B比分", example = "3")
    private Integer scoreB;

    @ApiModelProperty(value = "开启时间(时间戳)")
    private String startTime;

    @ApiModelProperty(value = "结束时间(时间戳)")
    private String stopTime;

    @ApiModelProperty(value = "支付金额(分/CNY)")
    private String total;

    @ApiModelProperty(value = "支付成功标志")
    private String payFlag;

    @TableField(exist = false)
    public static String PAYED = "1";
    @TableField(exist = false)
    public static String UNPAYED = "0";
    @TableField(exist = false)
    public static String PAYFAIL = "2";
    @TableField(exist = false)
    public static String STOPLIVE = "3";

    @Override
    public String toString() {
        return "Order{" +
                "preOrderId='" + preOrderId + '\'' +
                ", openid='" + openid + '\'' +
                ", projectId='" + projectId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", teamA='" + teamA + '\'' +
                ", teamB='" + teamB + '\'' +
                ", scoreA=" + scoreA +
                ", scoreB=" + scoreB +
                ", startTime='" + startTime + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", total='" + total + '\'' +
                ", payFlag='" + payFlag + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
