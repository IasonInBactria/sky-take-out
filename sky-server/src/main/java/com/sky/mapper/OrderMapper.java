package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**

     * 获取orderid的mapper层方法，写在OrderMapper.java文件下

     * @param orderNumber

     * @return

     */

    @Select("select * from orders where number=#{orderNumber}")
    Long getorderId (String orderNumber);

    /**

     * 用于替换微信支付更新数据库状态的问题

     * @param orderStatus

     * @param orderPaidStatus

     */

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus}" +
            " ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);


    /**
     * 分页查询订单信息
     *
     * @param ordersPageQueryDTO 订单分页查询条件传输对象，包含订单状态、创建时间和页码等信息
     * @return 返回一个Page对象，包含符合查询条件的订单分页信息
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Integer countByStatus(Integer toBeConfirmed);
}
