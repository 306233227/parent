package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;
    @RequestMapping("/findCartList")

    public List<Cart> findCartList(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

            //从COOK中获取购物车
            String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartList==null||cartList.equals("")){
                cartList="[]";
            }
            List<Cart> cartList_cookie = JSON.parseArray(cartList, Cart.class);
        if (name.equals("")){
            return cartList_cookie;
        }else {

            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            if (cartList_cookie.size()>0) {
                //redis,cookie合并
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除cook
                util.CookieUtil.deleteCookie(request,response,"cartList");
                //存入redis
                cartService.saveCartListToRedis(name,cartList_redis);
            }
            return cartList_redis;
        }
    }
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList=findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (name.equals("")) {
                util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            }else {
            cartService.saveCartListToRedis(name,cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败");
        }
    }


}
