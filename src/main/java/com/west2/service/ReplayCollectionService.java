package com.west2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.entity.OrderReplay;
import com.west2.entity.ReplayCollection;
import com.west2.entity.UserCollection;
import com.west2.entity.base.BaseEntity;
import com.west2.mapper.ReplayCollectionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReplayCollectionService extends CrudService<ReplayCollectionMapper, ReplayCollection> {

    @Autowired
    private OrderReplayService orderReplayService;

    /**
     * @desc    收藏视频
     * @param userId    用户id
     * @param playId    视频id
     * @return
     */
    public boolean collectReplay(String userId, String playId) {
        QueryWrapper<ReplayCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .eq("replay_id", playId);
        List<ReplayCollection> list = this.findList(wrapper);
        log.info(list.size()+"");
        ReplayCollection replay = null;
        if (0 < list.size()) {
            return false;
        } else {
            replay = new ReplayCollection();
            replay.setOpenid(userId)
                    .setReplayId(playId);
            return 0< this.save(replay);
        }
    }

    /**
     * @desc    取消收藏视频
     * @param userId    用户id
     * @param playId    视频id
     * @return
     */
    public boolean uncollectReplay(String userId, String playId) {
        QueryWrapper<ReplayCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .eq("replay_id", playId);
        List<ReplayCollection> list = this.findList(wrapper);
        ReplayCollection replay = null;
        if (0 < list.size()) {
            return 0 < this.delete(list.get(0));
        } else {
            return false;
        }
    }

    /**
     * @desc    视频是否被收藏
     * @param userId    用户id
     * @param replayId  回放id
     * @return
     */
    public boolean isCollected(String userId, String replayId) {
        QueryWrapper<ReplayCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .eq("replay_id", replayId)
                .eq("del_flag", BaseEntity.DEL_FLAG_NORMAL);
        return mapper.selectCount(wrapper)==1;
    }

    /**
     * @desc    获取该用户收藏的视频
     * @param userId    用户id
     * @return
     */
    public List<OrderReplay> getReplaysByUser(String userId) {
        QueryWrapper<ReplayCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .orderByDesc("update_date");
        List<ReplayCollection> list = this.findAllList(wrapper);
        ArrayList<OrderReplay> result = new ArrayList<>(list.size());
        for (ReplayCollection rc : list) {
            OrderReplay replay = orderReplayService.get(rc.getReplayId());
            result.add(replay);
        }
        return result;
    }

    /**
     * @desc    删除某订单下所有的收藏记录
     * @param orderId   订单id
     * @return
     */
    public int deleteReplayCollectByOrder(String orderId) {
        QueryWrapper<OrderReplay> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderReplay> list = orderReplayService.findList(wrapper);
        int result = 0;
        for (OrderReplay replay : list) {
            result += this.deleteReplayCollectByReplayId(replay.getId());
        }
        return result;
    }

    /**
     * @desc 批量删除某订单下所有收藏记录
     * @param replayId  订单id
     * @return
     */
    public int deleteReplayCollectByReplayId(String replayId) {
        QueryWrapper<ReplayCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("replay_id", replayId);
        List<ReplayCollection> list = this.findList(wrapper);
        int result = 0;
        for (ReplayCollection rc : list) {
            result += this.delete(rc);
        }
        return result;
    }

}
