package com.sky.mapper;


import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);


    /**
     * 根据订单ID获取订单详情
     *
     * @param orderId 订单ID，用于查询对应订单详情
     * @return 返回与指定订单ID相关的订单详情列表
     */
    List<OrderDetail> getByOrderId(Long orderId);
}
