package com.west2.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.west2.entity.Order;
import com.west2.entity.OrderReplay;
import com.west2.entity.vo.LiveCutVO;
import com.west2.entity.vo.OrderReplayVO;
import com.west2.entity.vo.RecordCallbackVO;
import com.west2.mapper.OrderReplayMapper;
import com.west2.utils.BeanCustomUtil;
import com.west2.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderReplayService extends CrudService<OrderReplayMapper, OrderReplay> {

    @Autowired
    private OrderTeamService orderTeamService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserCollectionService userCollectionService;
    @Autowired
    private VodService vodService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReplayCollectionService replayCollectionService;

    /**
     * @desc    获取所有回放
     * @param orderId 订单id
     * @return
     */
    public List<OrderReplay> getFullReplay(String orderId) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).
                eq("type", OrderReplay.ORIGIN);
        return this.findList(wrapper);
    }

    /**
     * @desc    保存回调中数据至数据库
     * @param vo    回调返回实体
     * @return
     */
    public int saveReplayVideoInfo(RecordCallbackVO vo) {
        String videoUrl = vo.getVideo_url();
        videoUrl = videoUrl.replaceAll("http", "https");
        String id = vo.getStream_id();
        OrderReplay orderReplay = new OrderReplay();
        orderReplay.setOrderId(id);
        orderReplay.setNewRecord(true);
        orderReplay.setReplay(videoUrl);
        orderReplay.setFileId(vo.getFile_id())
                .setType(OrderReplay.ORIGIN);
        return this.save(orderReplay);
    }

    /**
     * @desc    保存剪辑后视频至数据库
     * @param vo    剪辑数据实体
     * @return
     */
    public int saveCuts(LiveCutVO vo) {
        OrderReplay orderCut = vodService.liveRealTimeClip(vo.getOrderId(), vo.getType());

        if (orderCut!=null) {
            orderCut.setTitle(vo.getTitle());
            orderCut.setReplay(orderCut.getReplay().replaceAll("http", "https"));
            String id = IdWorker.getId(orderCut) + "";
            orderCut.setId(id);
            orderCut.setNewRecord(true);
            int save = this.save(orderCut);
            vodService.liveCutTranscode(id);
            return save;
        } else {
            return 0;
        }
    }

    /**
     * @desc    根据订单id获取某类型精彩片段
     * @param orderId   订单id
     * @param type  精彩片段类型(OrderReplay.常数)
     * @return
     */
    public List<OrderReplay> getCutsByOrder(String orderId, String ...type) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        if (type.length == 1) {
            wrapper.eq("type", type[0]);
        } else if (type.length > 1) {
            wrapper.and(i -> {
                    for (int j = 0; j < type.length-1; j++) {
                        i.eq("type",type[j]).or();
                    }
                    i.eq("type",type[type.length-1]);
                }
            );
        }

        wrapper.orderByAsc("create_date");
        return this.findList(wrapper);
    }

    /**
     * @desc    通过订单id获取全程回放
     * @param orderId   订单id
     * @return
     */
    public OrderReplay getByOrderId(String orderId) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).eq("type", OrderReplay.ORIGIN);
        List<OrderReplay> list = this.findList(wrapper);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * @desc    视频收藏列表
     * @param orderId   订单id
     * @param type 0-全程回放, 1-进球集锦, 2-主队集锦, 3-客队集锦, 4-全场集锦
     * @return
     */
    public OrderReplay collectionList(String orderId, String type) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).eq("type", type).orderByDesc("create_date");
        List<OrderReplay> list = this.findList(wrapper);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public List<OrderReplay> replayList(String orderId, String ...type) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.and(i -> {
                    for (int j = 0; j < type.length-1; j++) {
                        i.eq("type",type[j]).or();
                    }
                    i.eq("type",type[type.length-1]);
                }
        );
        wrapper.orderByDesc("create_date");
        return this.findList(wrapper);
    }

    /**
     * @desc    获取某订单所有精彩视频
     * @param orderId   订单id
     * @return
     */
    public List<OrderReplay> detailList(String orderId) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId)
                .ne("type", OrderReplay.ORIGIN)
                .ne("type", OrderReplay.ACOMPOSE)
                .ne("type", OrderReplay.BCOMPOSE)
                .ne("type", OrderReplay.SCORECOLLECTION)
                .ne("type", OrderReplay.ALLCOLLECTION);
        return this.findList(wrapper);
    }

    public int saveTransCallback(JSONObject jsonObject) {
        JSONObject taskType = jsonObject.getJSONObject("ProcedureStateChangeEvent")
                .getJSONArray("MediaProcessResultSet").getJSONObject(0)
                .getJSONObject("TranscodeTask");
        if (!taskType.isEmpty()) {
            String replayUrl = taskType
                    .getJSONObject("Output")
                    .getString("Url");
            String orderId = jsonObject.getJSONObject("ProcedureStateChangeEvent")
                    .getString("FileName")
                    .substring(0,19);
            OrderReplay orderReplay = new OrderReplay();
            orderReplay.setId(orderId);
            orderReplay.setNewRecord(true);
            orderReplay.setReplay(replayUrl)
                    .setType(OrderReplay.ORIGIN);
            return this.save(orderReplay);
        }
        return 0;
    }

    /**
     * @desc    获取某订单的全程回放
     * @param orderId
     * @return
     */
    public String getReplayByOrderId(String orderId) {
        OrderReplay replay = this.getByOrderId(orderId);
        if (replay==null) {
            return null;
        } else {
            return replay.getReplay();
        }
    }

    /**
     * @desc    数据库实体转前端展示类
     * @param orderReplay   数据库实体
     * @param userId    调用用户id
     * @return
     */
    public OrderReplayVO orderReplay2VO(OrderReplay orderReplay, String userId) {
        OrderReplayVO vo = new OrderReplayVO();
        BeanCustomUtil.copyProperties(orderReplay, vo);
        log.info(vo.toString());
        Order order = orderService.get(orderReplay.getOrderId());
        if (order!=null) {
            if (StringUtils.isBlank(order.getGameName())) {
                vo.setGameName(projectService.getNameByProjectId(order.getProjectId()));
            } else {
                vo.setGameName(order.getGameName());
            }
            vo.setTeamA(orderTeamService.getTeamName(order.getTeamA()));
            vo.setTeamB(orderTeamService.getTeamName(order.getTeamB()));
            vo.setStartTime(DateTimeUtil.stampToFormatDate(order.getStartTime(), "yyyy/MM/dd HH:mm"));
            vo.setId(orderReplay.getId());
            vo.setIsCollected(replayCollectionService.isCollected(userId, vo.getId()));
            return vo;
        } else {
            return null;
        }
    }

    /**
     * @desc    批量数据库实体转前端展示类
     * @param orderReplayList 数据库实体
     * @param userId    调用用户id
     * @return
     */
    public List<OrderReplayVO> orderReplayList2VO(List<OrderReplay> orderReplayList, String userId) {
        ArrayList<OrderReplayVO> list = new ArrayList<>(orderReplayList.size());
        for (OrderReplay replay : orderReplayList) {
            list.add(this.orderReplay2VO(replay, userId));
        }
        return list;
    }

    /**
     * @desc 通过订单id批量删除数据库视频记录
     * @param orderId   订单id
     * @return
     */
    public int deleteOrderReplayByOrderId(String orderId) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderReplay> list = this.findList(wrapper);
        int result = 0;
        for (OrderReplay replay : list) {
            result += this.delete(replay);
        }
        return result;
    }


}
