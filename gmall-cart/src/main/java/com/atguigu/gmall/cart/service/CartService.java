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
        //1、先判断用户的登录状态，如果登录就是userid，如果没有就是userkey
        String userId = getUserId();
        System.err.println(userId);
        //获取内层map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        //判断购物车中是否有该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        //2、判断是更新还是添加
        if (hashOps.hasKey(skuId)) {

            //包含则更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            //写入redis
            hashOps.put(skuId, JSON.toJSONString(cart));
            //写入数据库
//            this.cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
            //使用springtask异步任务
            //如果只是讲代码抽取成方法在本类中加@Async注解，那么this调用时是本类对象，不是代理对象，该注解不会生效
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

            //查询库存信息
            ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));

            }

            //销售属性
            ResponseVo<List<SkuAttrValueEntity>> attrValuesBySkuId = this.pmsClient.queryAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> attrValueEntityList = attrValuesBySkuId.getData();
            cart.setSaleAttrs(JSON.toJSONString(attrValueEntityList));

            //营销信息
            ResponseVo<List<ItemSaleVo>> salesBySkuId = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> sales = salesBySkuId.getData();
            cart.setSales(JSON.toJSONString(sales));

            cart.setCheck(true);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));

//            this.cartMapper.insert(cart);
            //使用springtask异步任务
            this.cartAsyncService.insertCart(cart);
            //添加价格缓存
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
        //获取内层map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("此用户不存在本条购物信息记录");
    }

    //查询购物车
    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        //1、先查询未登录状态下的购物车
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        //拿到内层map的value的集合，如果不为空则反序列化
        List<Object> cartJsons = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unLoginCarts = cartJsons.stream().map(cartJson->{
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                //设置实时价格
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        //2、判定是否登录，如果未登录直接返回
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unLoginCarts;
        }

        //3、如果登录则查询登录状态下的购物车，并合并未登录的购物车
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)) {
                    //如果登录后购物车中存在就更新数量
                    String cartJson = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));

                    this.cartAsyncService.updateCart(userId.toString(),cart);

                } else {
                    //如果登陆后不存在该记录，则直接新插入一条，但是要注意id
                    cart.setUserId(userId.toString());
                    this.cartAsyncService.insertCart(cart);
                }
                //放到redis中
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });
        }
        //4、清空未登录的购物车
        this.redisTemplate.delete(KEY_PREFIX + userKey); //从redis中删除
        this.cartAsyncService.deleteCart(userKey); //从数据库中删除
        //5、查询登录下的所有购物车并序列化返回
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
            System.out.println("test1方法开始执行");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("test1方法执行完毕");
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
            System.out.println("test2方法开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("test2方法执行完毕");
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
        throw new CartException("该用户不包含该条记录");
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
        throw new CartException("该用户不包含该条记录");
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
            this.cartAsyncService.deleteCartBySkuIdAndUserId(skuId, userId);
            return;
        }
        throw new CartException("该用户不包含该条记录");
    }
}
