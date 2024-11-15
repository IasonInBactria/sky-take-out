package com.sky.service;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 用户端分页查询
     *
     * @param pageNum     分页对象，用于指定分页查询的页码等信息
     * @param pageSize 每页显示的条数
     * @param status   用户的状态，用于过滤查询结果
     * @return 返回分页查询的结果，包含查询到的数据列表及分页信息
     * 本方法主要用于根据指定的页码、每页条数和用户状态进行用户的分页查询
     * 通过分页查询可以有效地减少数据库的查询压力，并提升用户体验
     * 参数中的page对象包含了当前查询的页码信息，pageSize指定了每页要显示的记录数，
     * 而status参数则用于过滤出符合特定状态的用户
     * 返回的PageResult对象不仅包含了查询到的用户数据，还包含了相关的分页信息，
     * 如总记录数、总页数等，便于前端展示和进一步操作
     */
    PageResult userPageQuery(int pageNum, int pageSize,Integer status);

    /**
     * 根据订单ID获取订单详情
     *
     * @param id 订单的唯一标识符
     * @return 返回一个包含订单详细信息的OrderVO对象
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancelById(Long id) throws Exception;

    /**
     * 用户再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单接口
     *
     * 此接口用于商家确认接单。当商家准备好处理订单时，可以调用此接口来更新订单状态为已接单。
     *
     * @param ordersConfirmDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒绝接单接口
     *
     * 此接口用于商家拒绝接单。当商家无法处理订单时，可以调用此接口来更新订单状态为已拒绝。
     *
     * @param ordersRejectionDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;


    /**
     * 订单取消
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;


    /**
     * 派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id
     *
     *
     **/

    void complete(Long id);
}
