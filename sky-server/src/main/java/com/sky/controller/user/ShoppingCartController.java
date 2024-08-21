package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车接口")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    /**
     * 添加购物车项
     * 此方法通过POST请求接收一个ShoppingCartDTO对象，将购物车信息添加到系统中
     * 目前该方法仅返回成功添加的结果，不处理具体的添加逻辑或错误情况
     *
     * @param shoppingCartDTO 购物车数据传输对象，包含购物车的相关信息
     * @return Result 成功添加购物车后的结果对象，目前仅表示操作成功，不包含其他信息
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车, 商品信息为:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 减少购物车项
     * 此方法通过POST请求接收一个ShoppingCartDTO对象，将购物车信息从系统中删除
     * 目前该方法仅返回成功删除的结果，不处理具体的删除逻辑或错误情况
     *
     * @param shoppingCartDTO 购物车数据传输对象，包含购物车的相关信息
     * @return Result 成功删除购物车后的结果对象，目前仅表示操作成功，不包含其他信息
     */
    @PostMapping("/sub")
    @ApiOperation(value = "删除购物车中商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车, 商品信息为:{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车接口
     * 该接口用于用户查看其购物车中的商品列表
     * 通过调用shoppingCartService.showShoppingCart()方法来获取购物车内容
     *
     * @return 返回一个Result对象，其中包含购物车的商品列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "查看购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");
        return Result.success(shoppingCartService.showShoppingCart());
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
