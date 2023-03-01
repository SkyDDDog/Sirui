package com.west2.entity.vo;

import com.west2.entity.AdminUser;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserProjectVO extends AdminUser {

    List<ProjectShowVO> projectList;

}
