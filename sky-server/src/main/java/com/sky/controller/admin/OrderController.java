package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端订单管理接口")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @RequestMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult result = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(result);
    }


    /**
     * 各状态下的订单数量统计
     * @return OrderStatisticsVO
     *
     */
    @RequestMapping("/statistics")
    @ApiOperation("各状态下的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }


    /**
     * 订单详情查询
     * @param id
     * @return OrderVO
     */
    @ApiOperation("订单详情查询")
    @RequestMapping("/details/{id}")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }


    /**
     * 接单接口
     *
     * 此接口用于商家确认接单。当商家准备好处理订单时，可以调用此接口来更新订单状态为已接单。
     *
     * @param ordersConfirmDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<OrderVO> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }


    /**
     * 拒单接口
     *
     * 此接口用于商家拒单。当商家拒绝处理订单时，可以调用此接口来更新订单状态为已拒单。
     *
     * @param ordersRejectionDTO 包含订单确认信息的DTO对象
     * @return 操作结果，如果成功则返回成功信息
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }


    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }


    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }


    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
