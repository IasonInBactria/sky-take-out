package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 获取购物车列表
     * 此方法用于根据当前购物车的信息获取一系列购物车对象，便于进行后续的处理或展示
     *
     * @param shoppingCart 当前购物车对象，可能用于查询或筛选其他相关购物车
     * @return 返回一个ShoppingCart对象列表，包含所有相关的购物车信息
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);


    /**
     * 根据购物车项的ID更新其数量
     * 此方法使用MyBatis的@Update注解，执行SQL更新操作，根据提供的购物车项ID，将其数量更新为指定的新数量
     * 主要用于处理购物车中商品数量的变更，如增减购买数量等
     *
     * @param shoppingCart 购物车对象，其中包含需要更新的购物车项的ID和新的数量
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车项
     *
     * @param shoppingCart 要插入的购物车项，包含菜品名称、图片、用户ID、菜品ID、套餐ID、菜品口味、数量、金额和创建时间等信息
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUES (#{name}, #{image} ,#{userId}, #{dishId},#{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据购物车项的ID删除购物车项
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteByUserId(Long currentId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
