package com.west2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.west2.entity.base.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @desc 场地列表实体类
 * @date 2022/12/7
 */
@Data
@Accessors(chain = true)
@TableName("`project`")
@ApiModel(value = "Project", description = "Project")
public class Project extends DataEntity<Project> {

    @ApiModelProperty(value = "project_id")
    private String projectId;

    @ApiModelProperty(value = "name")
    private String name;

    @ApiModelProperty(value = "price")
    private String price;

    @ApiModelProperty(value = "overlay_flag")
    private String overlayFlag;

    // 显示计分板 (1-足球 2-篮球)
    public static String OVERSOCCER = "1";
    public static String OVERBASKET = "2";
    // 不显示计分板
    public static String UNOVERLAY = "0";

    public static boolean isOverlayed(String overlayFlag) {
        return OVERSOCCER.equals(overlayFlag) || OVERBASKET.equals(overlayFlag);
    }

    public static boolean isOverBasket(String overlayFlag) {
        return OVERBASKET.equals(overlayFlag);
    }

    public static boolean isOverSoccer(String overlayFlag) {
        return OVERSOCCER.equals(overlayFlag);
    }



    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", id='" + id + '\'' +
                ", isNewRecord=" + isNewRecord +
                ", remarks='" + remarks + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }

}
