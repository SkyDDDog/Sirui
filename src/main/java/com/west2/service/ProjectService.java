package com.west2.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.config.RuisConfig;
import com.west2.entity.Order;
import com.west2.entity.Project;
import com.west2.entity.ProjectShare;
import com.west2.entity.vo.OrderVO;
import com.west2.entity.vo.ProjectShowVO;
import com.west2.mapper.ProjectMapper;
import com.west2.utils.BeanCustomUtil;
import com.west2.utils.DateTimeUtil;
import com.west2.utils.HttpRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProjectService extends CrudService<ProjectMapper, Project> {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProjectShareService projectShareService;

    /**
     * @desc    根据project_id获取场地
     * @param projectId 场地id(甲方给的场地id,一般是4位数字)
     * @return
     */
    public Project getProjectById(String projectId) {
        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId);
        List<Project> list = this.findList(wrapper);
        if (0 < list.size()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * @desc 获取场地列表
     * @return
     */
    public List<ProjectShowVO> getProjects() {
        List<Project> list = this.findList(new QueryWrapper<>());
        return list2VO(list);
    }

    /**
     * @desc    根据场地id获取场地展示类
     * @param projectId 场地id
     * @return
     */
    public ProjectShowVO getProjectShowById(String projectId) {
        Project project = this.getProjectById(projectId);
        ProjectShowVO vo = new ProjectShowVO();
        if (project!=null) {
            BeanCustomUtil.copyProperties(project, vo);
            JSONObject jsonObject = this.getVenueState(projectId);
            if (jsonObject.getJSONObject("error").getIntValue("code")==0) {
                JSONObject data = jsonObject.getJSONObject("data");
                vo.setStatus(data.getString("state"))
                .setLastUpdate(data.getString("lastUpdate"));
            }
        }
        return vo;
    }

    /**
     * @desc    获取场地列表
     * @param wrapper
     * @return
     */
    public List<ProjectShowVO> getProjectList(QueryWrapper<Project> wrapper) {
        List<Project> list = this.findList(wrapper);
        return this.list2VO(list);
    }

    /**
     * @desc 数据库实体转展示类
     * @param list  数据库实体
     * @return
     */
    private List<ProjectShowVO> list2VO(List<Project> list) {
        JSONObject jsonObject = null;
        ArrayList<ProjectShowVO> result = new ArrayList<>();
        for (Project project : list) {
            jsonObject = this.getVenueState(project.getProjectId());
            ProjectShowVO vo = new ProjectShowVO();
            BeanCustomUtil.copyProperties(project, vo);
            vo.setProjectId(project.getProjectId());
            if (jsonObject.getJSONObject("error").getIntValue("code")==0) {
                JSONObject data = jsonObject.getJSONObject("data");
                vo.setStatus(data.getString("state"))
                        .setLastUpdate(data.getString("lastUpdate"));
            } else {
                vo.setStatus(jsonObject.getJSONObject("error").getString("message"))
                        .setLastUpdate(DateTimeUtil.nowTimeStamp()+"");
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * @desc    调用甲方api获取设备状态
     * @param projectId 场地id
     * @return
     */
    private JSONObject getVenueState (String projectId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", RuisConfig.ApiConfig.username);
        params.add("pwd", RuisConfig.ApiConfig.pwd);
        params.add("projectID", projectId);
        JSONObject jsonObject = null;
        try {
            jsonObject = HttpRequestUtil.postFormUnlencoded(RuisConfig.ApiConfig.statusUrl, params);
//            log.info(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            log.info("查询设备状态接口异常");
        }
        return jsonObject;
    }

    /**
     * @desc    设备是否可用
     * @param vo
     * @return
     */
    public boolean isProjectAvailable(OrderVO vo) {
        // 设备是否可用
        JSONObject venueState = this.getVenueState(vo.getProjectId());
        int code = venueState.getJSONObject("error").getIntValue("code");
        boolean result = true;
        if (code==0) {
            String state = venueState.getJSONObject("data").getString("state");
            result = "ready".equals(state) || "idle".equals(state) || "recording".equals(state) || "Online".equals(state);
        } else {
            return false;
        }
        // 是否已有人下单
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.isNull("pre_order_id");
        wrapper.eq("project_id",vo.getProjectId()).eq("pay_flag", Order.PAYED);
        List<Order> list = orderService.findList(wrapper);
        String start = vo.getStartTime();
        String stop = vo.getStopTime();
        boolean orderResult = true;
        for (Order order : list) {
            // 开始时间有直播
            if (order.getStartTime().compareTo(start) <= 0 && 0 <= order.getStopTime().compareTo(start)) {
                log.info("1");
                orderResult = false;
                break;
            }
            // 结束时间有直播
            if (order.getStartTime().compareTo(stop) <= 0 && 0 <= order.getStopTime().compareTo(stop)) {
                log.info("2");
                orderResult = false;
                break;
            }
            // 开始时间和结束时间之间有直播
            if (0 <= order.getStartTime().compareTo(start) && order.getStopTime().compareTo(stop) <= 0) {
                log.info("3");
                orderResult = false;
                break;
            }
        }
        return result && orderResult;
    }

    /**
     * @desc    该用户是否为场地主任
     * @param userId    用户id
     * @param projectId 场地id
     * @return
     */
    public boolean isOwner(String userId, String projectId) {
        QueryWrapper<ProjectShare> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId).eq("project_id", projectId);
        return projectShareService.findList(wrapper).size()>0;
    }

    /**
     * @desc    通过场地id获取场地名称
     * @param id    场地id
     * @return
     */
    public String getNameByProjectId(String id) {
        Project project = this.getProjectById(id);
        if (project!=null) {
            return project.getName();
        } else {
            return null;
        }
    }

    /**
     * @desc    获取场地计分板类型
     * @param projectId 场地id
     * @return  0-默认,1-足球,2-篮球
     */
    public String getOverlay(String projectId) {
        Project project = this.getProjectById(projectId);
        if (project!=null) {
            return project.getOverlayFlag();
        } else {
            return null;
        }

    }

}
