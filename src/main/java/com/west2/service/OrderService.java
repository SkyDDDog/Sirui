package com.west2.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.config.RuisConfig;
import com.west2.entity.Order;
import com.west2.entity.OrderReplay;
import com.west2.entity.Project;
import com.west2.entity.vo.OrderShowVO;
import com.west2.entity.vo.PlayingListVO;
import com.west2.entity.vo.ScoreboardVO;
import com.west2.mapper.OrderMapper;
import com.west2.utils.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderService extends CrudService<OrderMapper, Order> {

    @Autowired
    private OrderReplayService orderReplayService;
    @Autowired
    private OrderTeamService orderTeamService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserCollectionService userCollectionService;
    @Autowired
    private ProjectService projectService;

    /**
     * @desc 使用甲方api进行推流
     * @param order 订单数据
     * @return api返回结果
     */
    public JSONObject savePushStream(Order order) {
        return this.savePushStream(order, false);
    }

    /**
     * @desc 使用甲方api进行推流
     * @param order     订单数据
     * @param timeshiftOpened   是否开启时移服务(false-不开启, true-开启)
     * @return
     */
    public JSONObject savePushStream(Order order, boolean timeshiftOpened) {
        // 获取旧的commit
        Map<String, Object> old = (Map<String, Object>) (Object) redisUtil.hmget(order.getId());
        HashMap<String, Object> map;
        // 如果为空说明是第一次commit
        if (old!=null && !old.isEmpty()) {
            map = (HashMap<String, Object>) old;
        } else {
            map = new HashMap<>();
            // 初始化队伍颜色
            map.put("colorA", "#FFFFFF");
            map.put("colorB", "#FFFFFF");
        }
        String matchID = (String) map.get("matchId");
        String streamUrl = "";
        if (matchID==null) {
            if (order.getStopTime()==null) {
                streamUrl = (String) map.get("pushUrl");
            } else {
                if (timeshiftOpened) {
                    log.info("开启时移功能");
                    streamUrl = "rtmp://"+RuisConfig.StreamConfig.timeshiftURL+order.getId()+'?'+StreamUtil.getSafeUrl(order.getId(), Long.parseLong(order.getStopTime()));
                } else {
                    log.info("未开启时移功能");
                    streamUrl = "rtmp://"+RuisConfig.StreamConfig.pushURL+order.getId()+'?'+StreamUtil.getSafeUrl(order.getId(), Long.parseLong(order.getStopTime()));
                }
            }
            map.put("pushUrl", streamUrl);
        }
        String playUrl = RuisConfig.StreamConfig.protocol+RuisConfig.StreamConfig.playURL+order.getId()+RuisConfig.StreamConfig.suffix;
        map.put("playUrl", playUrl);
        map.put("show", true);

        long saveTime = Long.parseLong(order.getStopTime())-DateTimeUtil.nowTimeStamp();
//        order.setTeamA(orderTeamService.getTeamName(order.getTeamA()));
//        order.setTeamB(orderTeamService.getTeamName(order.getTeamB()));
        JSONObject jsonObject = pushStream(order, matchID, streamUrl);
        matchID = jsonObject.getString("matchID");
        if (matchID!=null && !matchID.isEmpty()) {
            map.put("matchId",matchID);
        }
        map.put("aCuts", 0);
        map.put("bCuts", 0);
        redisUtil.hmset(order.getId(), map, saveTime);
        return jsonObject;
    }

    /**
     * @desc 使用甲方api进行推流
     * @param order 订单id
     * @param matchID 更新模式用
     * @param pushUrl   推流地址
     * @return
     */
    private JSONObject pushStream(Order order, String matchID, String pushUrl) {
        Project project = projectService.getProjectById(order.getProjectId());
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", RuisConfig.ApiConfig.username);
        params.add("pwd", RuisConfig.ApiConfig.pwd);
        if (matchID!=null) {
            params.add("matchID", matchID);
        } else {
            params.add("streamURL", pushUrl);
            if (StringUtils.hasLength(order.getProjectId())) {
                params.add("projectID", order.getProjectId());
            }
        }
        if (StringUtils.hasLength(order.getStartTime())) {
            params.add("starttime", order.getStartTime());
        }
        if (StringUtils.hasLength(order.getStopTime())) {
            params.add("stoptime", order.getStopTime());
        }
        if (StringUtils.hasLength(order.getTeamA())) {
            params.add("teamA", orderTeamService.getTeamName(order.getTeamA()));
        }
        if (StringUtils.hasLength(order.getTeamB())) {
            params.add("teamB", orderTeamService.getTeamName(order.getTeamB()));
        }
        if (order.getProjectId()!=null && Project.isOverlayed(project.getOverlayFlag())) {
            log.info("使用自制记分牌");
            String overlayUrl = null;
            if (Project.isOverBasket(project.getOverlayFlag())) {
                overlayUrl = RuisConfig.ApiConfig.overBasketURL+"?orderId="+order.getId();
            } else if (Project.isOverSoccer(project.getOverlayFlag())) {
                overlayUrl = RuisConfig.ApiConfig.overSoccerURL+"?orderId="+order.getId();
            }
            if (StringUtils.hasLength(overlayUrl)) {
                params.add("overlayURL", overlayUrl);
            }
        }

        JSONObject result = null;
        try {
            log.info(params.toString());
            result = HttpRequestUtil.postFormUnlencoded(RuisConfig.ApiConfig.url, params);
            log.info(result.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @desc 调用甲方api主动断流并删除订单
     * @param orderId   订单id
     * @return
     */
    public JSONObject deletePushStream(String orderId) {
        Map<String, Object> map = (Map<String, Object>) (Object) redisUtil.hmget(orderId);
        String matchID = (String) map.get("matchId");
        Order order = new Order();
        order.setId(orderId);
        order.setStartTime(DateTimeUtil.nowTimeStamp()+2+"");
        order.setStopTime(DateTimeUtil.nowTimeStamp()+3+"");
//        order.setTeamA(orderTeamService.getTeamName(order.getTeamA()));
//        order.setTeamB(orderTeamService.getTeamName(order.getTeamB()));
        redisUtil.del(orderId);
        Order dbOrder = this.get(orderId);
        dbOrder.setStopTime(DateTimeUtil.nowTimeStamp()+3+"");
        this.save(dbOrder);
        return this.pushStream(order,matchID,null);
    }

    /**
     * @desc 调用甲方接口主动断流
     * @param orderId   订单id
     * @return
     */
    public int stopOrder(String orderId) {
        Order order = this.get(orderId);
        order.setStopTime(DateTimeUtil.nowTimeStamp()+"");
        order.setPayFlag(Order.STOPLIVE);
        return this.save(order);
    }


    /**
     * @desc    获取订单直播时长(每0.5h)
     * @param orderId 订单id
     * @return
     */
    public Integer getOrderDuration(String orderId) {
        Order order = this.get(orderId);
        if (order==null) {
            return 0;
        } else {
            return this.getOrderDuration(order.getStartTime(), order.getStopTime());
        }
    }

    /**
     * @desc    获取订单直播时长(每0.5h)
     * @param startTime     直播开始时间戳
     * @param stopTime      直播结束时间戳
     * @return
     */
    public Integer getOrderDuration(String startTime, String stopTime) {
        Integer start = Integer.valueOf(startTime);
        Integer stop = Integer.valueOf(stopTime);
        // second
        int result = stop-start;
        // half minute (向上取整)
        result = result / (60*30) + (result % (60*30) == 0 ? 0 : 1);
        return result;
    }

    /**
     * @desc 计算支付金额
     * @param orderId   订单id
     * @return
     */
    public Integer caculatePrice(String orderId) {
//        String price = redisUtil.getPrice();
        Order order = this.get(orderId);
        String projectId = order.getProjectId();
        Project project = projectService.getProjectById(projectId);
        Integer per = Integer.valueOf(project.getPrice());
        return this.getOrderDuration(orderId)*per;
    }

    /**
     * @desc    计算支付金额
     * @param projectId 场地id
     * @param startTime 直播开始时间
     * @param stopTime  直播结束时间
     * @return
     */
    public Integer caculatePrice(String projectId,String startTime, String stopTime) {
        Project project = projectService.getProjectById(projectId);
        Integer per = Integer.valueOf(project.getPrice());
        return this.getOrderDuration(startTime, stopTime)*per;
    }

    /**
     * @desc 根据后台管理用户id获取该用户所有场地
     * @param userId    后台管理用户id
     * @param pageNum   第?页
     * @param pageSize  一页?条数据
     * @return
     */
    public List<Order> getByProjectOwner(String userId, Integer pageNum, Integer pageSize) {

        return mapper.getByProjectOwner(userId,(pageNum - 1) * pageSize,pageSize);
    }

    /**
     * @desc    数据库实体转为前端展示类
     * @param order 数据库实体
     * @param userId    用户id
     * @return
     */
    public List<OrderShowVO> order2ShowVO(List<Order> order, String userId) {
        ArrayList<OrderShowVO> result = new ArrayList<>(order.size());
        for (Order o : order) {
            OrderShowVO vo = new OrderShowVO();
            BeanCustomUtil.copyProperties(o, vo);
            vo.setReplayUrl(orderReplayService.getReplayByOrderId(o.getId()));
            if (userId!=null && !userId.isEmpty()) {
                vo.setIsCollected(userCollectionService.isCollected(userId, vo.getId()));
                vo.setProjectName(projectService.getNameByProjectId(o.getProjectId()));
            }
            vo.setTeamA(orderTeamService.getTeamName(o.getTeamA()));
            vo.setTeamB(orderTeamService.getTeamName(o.getTeamB()));
            vo.setOverlay(projectService.getOverlay(o.getProjectId()));
            result.add(vo);
        }
        return result;
    }

    /**
     * @desc    数据库实体转直播列表展示类
     * @param source 数据库实体
     * @return
     */
    public PlayingListVO order2PlayingVO(Order source) {
        PlayingListVO target = new PlayingListVO();
        BeanCustomUtil.copyProperties(source,target);
        target.setStart(source.getStartTime().compareTo(DateTimeUtil.nowTimeStamp() + "") <= 0);
        target.setPlayUrl(RuisConfig.StreamConfig.protocol+RuisConfig.StreamConfig.playURL+source.getId()+RuisConfig.StreamConfig.suffix);
        return target;
    }

    /**
     * @desc 批量数据库实体转直播列表展示类
     * @param source    数据库实体
     * @return
     */
    public List<PlayingListVO> order2PlayingVO(List<Order> source) {
        ArrayList<PlayingListVO> target = new ArrayList<>(source.size());
        for (Order order : source) {
            PlayingListVO vo = this.order2PlayingVO(order);
            vo.setTeamA(orderTeamService.getTeamName(order.getTeamA()));
            vo.setTeamB(orderTeamService.getTeamName(order.getTeamB()));
            target.add(vo);
        }
        return target;
    }

    /**
     * @desc    开始计分板计时操作
     * @param orderId   订单id
     * @param startTime 初始化开始时间
     */
    public void startTick(String orderId, String startTime) {
        long stamp = DateTimeUtil.nowTimeStamp();
        if (StringUtils.hasLength(startTime)) {
            stamp = DateTimeUtil.nowTimeStamp() - Integer.parseInt(startTime);
        }
        redisUtil.hset(orderId,"timestamp", stamp);
    }

    /**
     * @desc    暂停计分板计时操作
     * @param orderId   订单id
     */
    public void stopTick(String orderId) {
//        Order order = this.get(orderId);
        Integer startStamp = (Integer) redisUtil.hget(orderId, "timestamp");
        long duration = DateTimeUtil.nowTimeStamp() - startStamp;
        redisUtil.hset(orderId,"duration", duration);
        redisUtil.hdel(orderId,"timestamp");
    }

    /**
     * @desc    重新开始计分板计时操作
     * @param orderId   订单id
     * @param startTime 重新开始时的当前时间戳
     */
    public void restartTick(String orderId, String startTime) {
        long now = DateTimeUtil.nowTimeStamp();
        Integer stopStamp = (Integer) redisUtil.hget(orderId, "timestamp");
        Integer duration = (Integer) redisUtil.hget(orderId, "duration");
        long re = now - duration;
        if (StringUtils.hasLength(startTime)) {
            re = DateTimeUtil.nowTimeStamp() - Integer.parseInt(startTime);
        }
        redisUtil.hset(orderId,"timestamp", re);
        redisUtil.hdel(orderId,"duration");
    }

    /**
     * @desc    结束计分板计时
     * @param orderId   订单id
     */
    public void endTick(String orderId) {
        Integer startStamp = (Integer) redisUtil.hget(orderId, "timestamp");
        long duration = DateTimeUtil.nowTimeStamp() - startStamp;
        redisUtil.hset(orderId,"duration", duration);
        redisUtil.hdel(orderId,"timestamp");
    }

    /**
     * @desc    计算计分板计分时长
     * @param orderId   订单id
     * @return
     */
    public String caculateTick(String orderId) {
        Integer timestamp = (Integer) redisUtil.hget(orderId, "timestamp");
        Integer duration = (Integer) redisUtil.hget(orderId, "duration");
        if (duration!=null) {
            return duration.toString();
        }
        if (timestamp!=null) {
            return (DateTimeUtil.nowTimeStamp()-timestamp)+"";
        }
        return "";
    }

    /**
     * @desc    计时是否暂停
     * @param orderId   订单id
     * @return
     */
    public boolean isTickStoped(String orderId) {
        Integer timestamp = (Integer) redisUtil.hget(orderId, "timestamp");
        Integer duration = (Integer) redisUtil.hget(orderId, "duration");
        if (timestamp!=null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @desc    计时是是否开始
     * @param orderId   订单id
     * @return
     */
    public boolean isTickStarted(String orderId) {
        Integer timestamp = (Integer) redisUtil.hget(orderId, "timestamp");
        Integer duration = (Integer) redisUtil.hget(orderId, "duration");
        return !(timestamp==null && duration==null);
    }

    /**
     * @desc    篮球场地专用，节数增加1
     * @param orderId   订单id
     * @return
     */
    public Integer courseInc(String orderId) {
        Integer course = (Integer) redisUtil.hget(orderId, "course");
        if (course==null || course==0) {
            course = 1;
        } else {
            course += 1;
        }
        redisUtil.hset(orderId, "course", course);
        return course;
    }

    /**
     * @desc 篮球场地专用，节数减少1
     * @param orderId 订单id
     * @return
     */
    public Integer courseDecr(String orderId) {
        Integer course = (Integer) redisUtil.hget(orderId, "course");
        if (course==null || course==0 || course==1) {
            course = 1;
        } else {
            course -= 1;
        }
        redisUtil.hset(orderId, "course", course);
        return course;
    }

    /**
     * @desc    获取篮球比赛目前节数
     * @param orderId   订单id
     * @return
     */
    public String getCourse(String orderId) {
        Integer course = (Integer) redisUtil.hget(orderId, "course");
        if (course==null || course==0) {
            course = 1;
        }
        return ScoreboardVO.getCourse(course);
    }

    /**
     * @desc    是否显示比分
     * @param orderId   订单id
     * @return
     */
    public Boolean getShowed(String orderId) {
        Object show = redisUtil.hget(orderId, "show");
        return show != null;
    }

    /**
     * @desc    显示比分
     * @param orderId   订单id
     */
    public void showScore(String orderId) {
        redisUtil.hset(orderId,"show",true);
    }

    /**
     * @desc    隐藏比分
     * @param orderId   订单id
     */
    public void unshowScore(String orderId) {
        redisUtil.hdel(orderId, "show");
    }

    public Order getPreOrder(String orderId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("pre_order_id", orderId);
        List<Order> list = this.findList(wrapper);
        if (list.size()==0) {
            return null;
        } else {
            return list.get(0);
        }

    }

}
