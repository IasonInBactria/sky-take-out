package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车, 商品信息为:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
