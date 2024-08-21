package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    /**
     * 将商品添加到购物车
     * 该方法负责将用户选择的商品添加到购物车中，是电子商务系统中处理用户购物行为的关键步骤
     *
     * @param shoppingCartDTO 购物车数据传输对象，包含了需要添加到购物车的商品信息
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断商品是否已经存在，如果存在，则数量加1，否则添加新的商品
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (!Objects.isNull(shoppingCartList) && !shoppingCartList.isEmpty()) {
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
             //判断添加到购物车的是菜品还是套餐
             Long dishId = shoppingCart.getDishId();
             Long setmealId = shoppingCart.getSetmealId();
             if (!Objects.isNull(dishId)) {
                 //本次添加的是菜品
                 Dish dish = dishMapper.getById(dishId);
                 shoppingCart.setName(dish.getName());
                 shoppingCart.setImage(dish.getImage());
                 shoppingCart.setAmount(dish.getPrice());
             } else {
                 //本次添加的是套餐
                 shoppingCart.setName(setmealMapper.getById(setmealId).getName());
                 shoppingCart.setAmount(setmealMapper.getById(setmealId).getPrice());
             }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }
    }

    /**
     * 减少购物车中商品的数量
     *
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (!Objects.isNull(shoppingCartList) && !shoppingCartList.isEmpty()) {
            ShoppingCart cart = shoppingCartList.get(0);
            Integer number = cart.getNumber();
           if (number == 1) {
               shoppingCartMapper.deleteById(cart.getId());
           } else {
               cart.setNumber(number - 1);
               shoppingCartMapper.updateNumberById(cart);
           }
        }
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.list(shoppingCart);
    }
}
