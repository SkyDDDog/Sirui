package com.west2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.entity.Project;
import com.west2.entity.ProjectShare;
import com.west2.mapper.ProjectMapper;
import com.west2.mapper.ProjectShareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectShareService extends CrudService<ProjectShareMapper, ProjectShare> {

    /**
     * @desc    该用户是否拥有该场地权限
     * @param userId    用户id
     * @param projectId 场地id
     * @return
     */
    public boolean havePermission(String userId, String projectId) {
        QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("project_id", projectId);
        return 0 < this.findList(wrapper).size();
    }

}
