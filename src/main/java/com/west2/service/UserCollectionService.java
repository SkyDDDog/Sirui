package com.west2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.west2.entity.UserCollection;
import com.west2.entity.base.BaseEntity;
import com.west2.mapper.UserCollectionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCollectionService extends CrudService<UserCollectionMapper, UserCollection> {

    /**
     * @desc    收藏订单
     * @param userId    用户id
     * @param orderId   订单id
     * @return
     */
    public boolean collectOrder(String userId, String orderId) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .eq("orderid", orderId);
        Long cnt = mapper.selectCount(wrapper);
        if (0 < cnt) {
            return false;
        } else {
            UserCollection userCollection = new UserCollection();
            userCollection.setOpenid(userId);
            userCollection.setOrderid(orderId);
            this.save(userCollection);
            return true;
        }
    }

    /**
     * @desc    订单是否被收藏
     * @param userId    用户id
     * @param orderId   收藏id
     * @return
     */
    public boolean isCollected(String userId, String orderId) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", userId)
                .eq("orderid", orderId)
                .eq("del_flag", BaseEntity.DEL_FLAG_NORMAL);
        return mapper.selectCount(wrapper)==1;
    }

    /**
     * @desc    删除订单所有被收藏记录
     * @param orderId   订单id
     * @return
     */
    public int deleteOrderCollectByOrder(String orderId) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("orderid", orderId);
        List<UserCollection> list = this.findList(wrapper);
        int result = 0;
        for (UserCollection uc : list) {
            result += this.delete(uc);
        }
        return result;
    }

}
