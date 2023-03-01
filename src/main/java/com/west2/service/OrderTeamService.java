package com.west2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.west2.entity.Order;
import com.west2.entity.OrderTeam;
import com.west2.entity.vo.OrderTeamVO;
import com.west2.mapper.OrderTeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderTeamService extends CrudService<OrderTeamMapper, OrderTeam> {

    @Autowired
    private OrderService orderService;

    /**
     * @desc    根据队伍名称获取队伍id (如不存在 则创建新队伍)
     * @param teamName  队伍名称
     * @return
     */
    public String getTeamId(String teamName) {
        QueryWrapper<OrderTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("name", teamName);
        List<OrderTeam> list = this.findList(wrapper);
//        log.info(list.toString(), list.size());
        if (0 < list.size()) {
            return list.get(0).getId();
        } else {
            OrderTeam orderTeam = new OrderTeam();
            String id = IdWorker.getId(orderTeam) + "";
            orderTeam.setName(teamName);
            orderTeam.setId(id);
            orderTeam.setNewRecord(true);
            this.save(orderTeam);
            return id;
        }
    }

    /**
     * @desc    根据队伍id获取队伍名称
     * @param teamId    队伍id
     * @return
     */
    public String getTeamName(String teamId) {
        OrderTeam orderTeam = this.get(teamId);
        if (orderTeam!=null) {
            return orderTeam.getName();
        } else {
            return "";
        }
    }

    /**
     * @desc    更新队伍名称
     * @param vo
     * @return
     */
    public int updateTeamName(OrderTeamVO vo) {
        Order order = orderService.get(vo.getOrderId());
        if (StringUtils.isNotBlank(vo.getTeamA())) {
            order.setTeamA(this.getTeamId(vo.getTeamA()));
        }
        if (StringUtils.isNotBlank(vo.getTeamB())) {
            order.setTeamB(this.getTeamId(vo.getTeamB()));
        }
        return orderService.save(order);
    }

}
