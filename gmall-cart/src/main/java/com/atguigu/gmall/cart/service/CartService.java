package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import springfox.documentation.spring.web.json.Json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/22/16:57
 * @Description:
 ******************************************/
@Service
public class CartService {

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LoginInterceptor loginInterceptor;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        //1??????????????????????????????????????????????????????userid?????????????????????userkey
        String userId = getUserId();
        System.err.println(userId);
        //????????????map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        //????????????????????????????????????
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        //2??????????????????????????????
        if (hashOps.hasKey(skuId)) {

            //?????????????????????
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            //??????redis
            hashOps.put(skuId, JSON.toJSONString(cart));
            //???????????????
//            this.cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
            //??????springtask????????????
            //???????????????????????????????????????????????????@Async???????????????this?????????????????????????????????????????????????????????????????????
            this.cartAsyncService.updateCart( userId,cart);
        } else {
            cart.setUserId(userId);

            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return;
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());

            //??????????????????
            ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));

            }

            //????????????
            ResponseVo<List<SkuAttrValueEntity>> attrValuesBySkuId = this.pmsClient.queryAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> attrValueEntityList = attrValuesBySkuId.getData();
            cart.setSaleAttrs(JSON.toJSONString(attrValueEntityList));

            //????????????
            ResponseVo<List<ItemSaleVo>> salesBySkuId = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> sales = salesBySkuId.getData();
            cart.setSales(JSON.toJSONString(sales));

            cart.setCheck(true);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));

//            this.cartMapper.insert(cart);
            //??????springtask????????????
            this.cartAsyncService.insertCart(cart);
            //??????????????????
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());

        }


    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() == null) {
            return userInfo.getUserKey();
        }
        return userInfo.getUserId().toString();
    }

    public Cart queryCart(Long skuId) {
        String userId = this.getUserId();
        //????????????map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("??????????????????????????????????????????");
    }

    //???????????????
    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        //1??????????????????????????????????????????
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        //????????????map???value??????????????????????????????????????????
        List<Object> cartJsons = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unLoginCarts = cartJsons.stream().map(cartJson->{
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                //??????????????????
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        //2???????????????????????????????????????????????????
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unLoginCarts;
        }

        //3????????????????????????????????????????????????????????????????????????????????????
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)) {
                    //????????????????????????????????????????????????
                    String cartJson = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));

                    this.cartAsyncService.updateCart(userId.toString(),cart);

                } else {
                    //??????????????????????????????????????????????????????????????????????????????id
                    cart.setUserId(userId.toString());
                    this.cartAsyncService.insertCart(cart);
                }
                //??????redis???
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });
        }
        //4??????????????????????????????
        this.redisTemplate.delete(KEY_PREFIX + userKey); //???redis?????????
        this.cartAsyncService.deleteCart(userKey); //?????????????????????
        //5??????????????????????????????????????????????????????
        List<Object> loginCarts = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCarts)) {
            return loginCarts.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Async
//    public ListenableFuture<String> test1() {
    public String test1() {
        try {
            System.out.println("test1??????????????????");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("test1??????????????????");
            int i = 1 / 0;
//            return AsyncResult.forValue("test1");
            return "test1";
        } catch (InterruptedException e) {
            e.printStackTrace();
//            return AsyncResult.forExecutionException(e);
        }
        return null;
//        return AsyncResult.forValue("test1");
    }
    @Async
    public ListenableFuture<String> test2() {
        try {
            System.out.println("test2??????????????????");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("test2??????????????????");
            return AsyncResult.forValue("test2");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return AsyncResult.forExecutionException(e);
        }
//        return AsyncResult.forValue("test2");
    }


    public void updateCartNum(Cart cart) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            BigDecimal count = cart.getCount();
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            this.cartAsyncService.updateCart(userId, cart);
            return;
        }
        throw new CartException("??????????????????????????????");
    }

    public void updateStatus(Cart cart) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            Boolean check = cart.getCheck();
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCheck(check);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            this.cartAsyncService.updateCart(userId, cart);
            return;
        }
        throw new CartException("??????????????????????????????");
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
            this.cartAsyncService.deleteCartBySkuIdAndUserId(skuId, userId);
            return;
        }
        throw new CartException("??????????????????????????????");
    }
}
