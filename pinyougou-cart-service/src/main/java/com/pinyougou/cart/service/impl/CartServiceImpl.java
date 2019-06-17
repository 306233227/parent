package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private TbItemMapper itemMapper;
    //根据查询商家ID购物车是否存在商家
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    //创建购物车明细
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if (num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
    //根据商品ID查询购物车明细是否存在商品
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品ID获取商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw  new RuntimeException("商品状态无效");
        }
        //获取商家ID
        String sellerId = item.getSellerId();
        //判断购物车商家是否存在
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart==null){
            //如果不存在创建购物车明细
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
            //如果购物车存在该商家购物车
            //判断明细中是否存在该商品
            TbOrderItem orderItem=searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (orderItem==null){
                 orderItem= createOrderItem(item, num);
                 cart.getOrderItemList().add(orderItem);
            }else {
                //存在就加数量
                orderItem.setNum(num+orderItem.getNum());
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum().doubleValue()));
          //为0后移除
                if (orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }

           if (cart.getOrderItemList().size()==0){
               cartList.remove(cart);
              }
            }

        }
        return cartList;
    }
    /**
     * 从 redis 中查询购物车
     * @param username
     * @return
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从 redis 中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }
    /**
     * 将购物车保存到 redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
           cartList1=addGoodsToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

}
