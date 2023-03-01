package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("`user_collection`")
@ApiModel(value = "UserCollection", description = "用户-收藏表")
public class UserCollection extends DataEntity<UserCollection> {

    @ApiModelProperty(value = "用户id")
    private String openid;

    @ApiModelProperty(value = "订单id")
    private String orderid;

    @Override
    public String toString() {
        return "UserCollection{" +
                "openid='" + openid + '\'' +
                ", orderid='" + orderid + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
