package com.west2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.west2.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT o.* FROM `order` o\n" +
            "INNER JOIN `project_share` p\n" +
            "ON p.project_id = o.project_id \n" +
            "WHERE p.user_id = #{userId} AND o.del_flag = 0 AND p.del_flag = 0 \n" +
            "ORDER BY o.update_date DESC \n" +
            "LIMIT #{pageNum} , #{pageSize}")
    public List<Order> getByProjectOwner(String userId, Integer pageNum, Integer pageSize);

}
