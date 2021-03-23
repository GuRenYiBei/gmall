package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/22/16:56
 * @Description:
 ******************************************/
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping()
    public String addCart(Cart cart) {
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    //新增购物车成功页面，本质就是查询对应userid下的skuid
    @GetMapping("addCart.html")
    public String queryCart(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = this.cartService.queryCart(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts",carts);
//        model.addAttribute(carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateCartNum(@RequestBody Cart cart) {
        this.cartService.updateCartNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("updateStatus")
    @ResponseBody
    public ResponseVo updateStatus(@RequestBody Cart cart) {
        this.cartService.updateStatus(cart);
        return ResponseVo.ok();
    }
    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }


//    @GetMapping("/test")
//    public String test(HttpServletRequest request) {
//        System.out.println("这是一个handler方法" + "....." + LoginInterceptor.getUserInfo());
////        System.out.println(request.getAttribute("userId"));
//        return "interceptor test";
//    }

//    @GetMapping("/test")
//    @ResponseBody
//    public String test(HttpServletRequest request) throws ExecutionException, InterruptedException {
//        long l = System.currentTimeMillis();
//        System.out.println("controller开始执行");
//        this.cartService.test1().addCallback(new SuccessCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                System.out.println("正常返回回调" + result);
//            }
//        }, new FailureCallback() {
//            @Override
//            public void onFailure(Throwable ex) {
//                System.out.println("异常返回回调" + ex);
//            }
//        });
//        this.cartService.test2().addCallback(new SuccessCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                System.out.println("正常返回回调" + result);
//            }
//        }, new FailureCallback() {
//            @Override
//            public void onFailure(Throwable ex) {
//                System.out.println("异常返回回调" + ex);
//            }
//        });
////        System.err.println(test1.get());
////        System.err.println(test2.get());
//        System.out.println("controller执行完毕" + (System.currentTimeMillis() - l));
//        return "hello test";
//    }
    @GetMapping("/test")
    @ResponseBody
    public String test(HttpServletRequest request) {
        long l = System.currentTimeMillis();
        System.out.println("controller方法开始执行");
        this.cartService.test1();
        System.out.println("controller方法结束执行" + (System.currentTimeMillis() - l));
        return "hello test2";
    }

}
