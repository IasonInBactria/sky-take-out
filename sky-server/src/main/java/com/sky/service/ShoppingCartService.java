package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {

    /**
     * 将商品添加到购物车
     * 该方法负责将用户选择的商品添加到购物车中，是电子商务系统中处理用户购物行为的关键步骤
     *
     * @param shoppingCartDTO 购物车数据传输对象，包含了需要添加到购物车的商品信息
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
