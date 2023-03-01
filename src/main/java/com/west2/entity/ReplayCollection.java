package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("`replay_collection`")
@ApiModel(value = "ReplayCollection", description = "ReplayCollection")
public class ReplayCollection extends DataEntity<ReplayCollection> {

    @ApiModelProperty(value = "用户id")
    private String openid;

    @ApiModelProperty(value = "order_replay id")
    private String replayId;

    @Override
    public String toString() {
        return "ReplayCollection{" +
                "openid='" + openid + '\'' +
                ", replayId='" + replayId + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
