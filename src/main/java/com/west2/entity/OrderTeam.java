package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("`order_team`")
@ApiModel(value = "OrderTeam", description = "OrderTeam")
public class OrderTeam extends DataEntity<OrderTeam> {

    @ApiModelProperty(value = "队伍名称")
    private String name;

    @Override
    public String toString() {
        return "OrderTeam{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
