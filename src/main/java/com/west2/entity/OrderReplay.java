package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Maps;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@TableName("`order_replay`")
@ApiModel(value = "OrderReplay", description = "OrderReplay")
public class OrderReplay extends DataEntity<OrderReplay> {

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

    // 直播全程回放
    @TableField(exist = false)
    public static String ORIGIN = "0";
    // A队(主队)单点剪辑
    @TableField(exist = false)
    public static String ACUTS = "1";
    // B队(客队)单点剪辑
    @TableField(exist = false)
    public static String BCUTS = "2";
    // A队(主队)精彩集锦
    @TableField(exist = false)
    public static String ACOMPOSE = "3";
    // B队(客队)精彩集锦
    @TableField(exist = false)
    public static String BCOMPOSE = "4";
    // A队进球片段
    @TableField(exist = false)
    public static String ASCORE = "5";
    // B队进球片段
    @TableField(exist = false)
    public static String BSCORE = "6";
    // 进球集锦
    @TableField(exist = false)
    public static String SCORECOLLECTION = "7";
    // 全场集锦
    @TableField(exist = false)
    public static String ALLCOLLECTION = "8";

    @Override
    public String toString() {
        return "OrderReplay{" +
                "replay='" + replay + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
