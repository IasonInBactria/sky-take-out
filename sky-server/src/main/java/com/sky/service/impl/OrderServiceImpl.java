package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserMapper userMapper;
    private final WeChatPayUtil weChatPayUtil;
    /**
     * 用户下单
     *
     * @param orderSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {

        //处理业务异常
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if (Objects.isNull(addressBook)) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId =  BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (Objects.isNull(shoppingCartList) || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getDetail());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向订单明细表填入多条数据
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //封装VO返回结果
        return OrderSubmitVO.builder().id(orders.getId()).orderTime(orders.getOrderTime()).orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        /*
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;

         */

        paySuccess(ordersPaymentDTO.getOrderNumber());
        String orderNumber = ordersPaymentDTO.getOrderNumber(); //订单号
        Long orderid = orderMapper.getorderId(orderNumber);//根据订单号查主键
        JSONObject jsonObject = new JSONObject();//本来没有2
        jsonObject.put("code", "ORDERPAID"); //本来没有3
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderid);

        return vo;  //  修改支付方法中的代码
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders order = new Orders();
        order.setId(ordersDB.getId());
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setPayStatus(Orders.PAID);
        order.setCheckoutTime(LocalDateTime.now());

        orderMapper.update(order);
    }

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
    @Override
    public PageResult userPageQuery(int pageNum, int pageSize,Integer status) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (!Objects.isNull(page) && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();
                //查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 根据订单ID获取订单详情
     *
     * @param id 订单的唯一标识符
     * @return 返回一个包含订单详细信息的OrderVO对象
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        //根据ID查询订单
        Orders orders = orderMapper.getById(id);

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancelById(Long id) throws Exception {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        if (Objects.isNull(ordersDB)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 判断订单状态, 只有未支付订单可以取消，已支付订单不能取消
        if (ordersDB.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders curOrder = new Orders();
        curOrder.setId(id);

        // 若订单已支付，需要调用微信支付接口来退款
        if (Objects.equals(ordersDB.getPayStatus(), Orders.TO_BE_CONFIRMED)) {
            //调用微信支付接口退款
            String refund = weChatPayUtil.refund(ordersDB.getNumber(), ordersDB.getNumber(),
                    new BigDecimal("0.01"), ordersDB.getAmount());
            log.info("申请退款：{}", refund);

            //订单支付状态已退款
            curOrder.setPayStatus(Orders.REFUND);
        }

        curOrder.setStatus(Orders.CANCELLED);
        curOrder.setCancelReason("用户取消");
        curOrder.setCancelTime(LocalDateTime.now());
        orderMapper.update(curOrder);
    }

    /**
     * 用户再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        //查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //将订单详情转换成购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .id(x.getId())
                    .createTime(LocalDateTime.now())
                    .userId(BaseContext.getCurrentId()).build();
            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量插入到购物车表
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //部分订单状态，需要额外返回订单菜品信息，将Orders转为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        //根据数据结构，分别查询待接单数量、待派送数量、派送中数量
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.countByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 接单接口
     * <p>
     * 此接口用于商家确认接单。当商家准备好处理订单时，可以调用此接口来更新订单状态为已接单。
     *
     * @param ordersConfirmDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    /**
     * 拒绝接单接口
     * <p>
     * 此接口用于商家拒绝接单。当商家无法处理订单时，可以调用此接口来更新订单状态为已拒绝。
     *
     * @param ordersRejectionDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //根据id查询订单
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        //判断订单状态，只有待接单状态的订单才能进行接单操作
        if (Objects.isNull(orders) || Objects.equals(orders.getStatus(), Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //更新支付状态
        Integer payStatus = orders.getPayStatus();
        if (Objects.equals(payStatus, Orders.PAID)) {
            //若客户已经支付，需要退款
            //调用微信支付接口退款
            String refund = weChatPayUtil.refund(orders.getNumber(), orders.getNumber(),
                    new BigDecimal("0.01"), new BigDecimal("0.01"));
            log.info("申请退款：{}", refund);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders curOrder = new Orders();
        curOrder.setId(orders.getId());
        curOrder.setStatus(Orders.CANCELLED);
        curOrder.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        curOrder.setCancelTime(LocalDateTime.now());
        orderMapper.update(curOrder);
    }

    /**
     * 订单取消
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //根据ID查询订单
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = orders.getPayStatus();
        if (Objects.equals(payStatus, Orders.PAID)) {
            //若客户已经支付，需要退款
            //调用微信支付接口退款
            String refund = weChatPayUtil.refund(orders.getNumber(), orders.getNumber(),
                    new BigDecimal("0.01"), new BigDecimal("0.01"));
            log.info("申请退款：{}", refund);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders curOrder = new Orders();
        curOrder.setId(ordersCancelDTO.getId());
        curOrder.setStatus(Orders.CANCELLED);
        curOrder.setCancelReason(ordersCancelDTO.getCancelReason());
        curOrder.setCancelTime(LocalDateTime.now());
        orderMapper.update(curOrder);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);
        //判断订单状态，只有待派送状态的订单才能进行派送操作
        if (Objects.isNull(orders) || Objects.equals(orders.getStatus(), Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 派送订单，根据订单id更新订单状态
        Orders curOrder = new Orders();
        curOrder.setId(id);
        curOrder.setStatus(Orders.DELIVERY_IN_PROGRESS);
        curOrder.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(curOrder);
    }

    /**
     * 完成订单
     *
     * @param id
     **/
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> records = page.getResult();
        //避免空指针异常
        if (!CollectionUtils.isEmpty(records)) {
            for (Orders orders : records) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                Long orderId = orders.getId();
                //根据订单id查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                orderVO.setOrderDetailList(orderDetailList);
                //获取菜品信息字符串
                orderVO.setOrderDishes(getOrderDishesStr(orderDetailList));
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    private String getOrderDishesStr(List<OrderDetail> orderDetailList) {
        return orderDetailList.stream().map(x -> x.getNumber() + "份" + x.getName()).collect(Collectors.joining(","));
    }

}
