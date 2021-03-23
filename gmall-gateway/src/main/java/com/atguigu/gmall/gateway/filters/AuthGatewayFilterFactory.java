package com.atguigu.gmall.gateway.filters;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import com.google.common.net.HttpHeaders;
import com.sun.org.apache.regexp.internal.RE;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/21/22:58
 * @Description: 自定义局部过滤器，优先级高于全局过滤器
 ******************************************/
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties jwtProperties;


    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    //如果有多个参数，我们传递一个list，需要用该方法指定接收的类型
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("我是自定义局部过滤器，我只拦截特定的请求");
                System.err.println(config.getPaths());
                //执行业务逻辑

                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //1、判断要拦截的请求是否在我们的名单中，不在直接放行
                String curPath = request.getURI().getPath();
                List<String> paths = config.getPaths();
                if (!paths.stream().anyMatch(path -> curPath.startsWith(path))) {
                    return chain.filter(exchange);
                }
                //2、获取token信息，同步从cookie中获取，异步从请求头中获取
                String token = request.getHeaders().getFirst("token");
                //异步获取，如果为空，则尝试从cookie中获取
                if (StringUtils.isEmpty(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())) {
                        token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }
                //3、判断token是否为空，为空直接拦截
                if (StringUtils.isBlank(token)) {
                    //重定向到登录，使用response重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // request.getURI()获取一个包含协议请求头的完整的路径
                    return response.setComplete(); // 拦截后续业务逻辑
                }

                try {
                    //4、解析jwt，有异常直接拦截，重定向到登录页
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    //5、拿到载荷中的ip和当前ip比较
                    String ip = map.get("ip").toString();
                    String ipAddressAtGateway = IpUtils.getIpAddressAtGateway(request);
                    if (!StringUtils.equals(ip, ipAddressAtGateway)) {
                        //重定向到登录，使用response重定向
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        // request.getURI()获取一个包含协议请求头的完整的路径
                        return response.setComplete(); // 拦截后续业务逻辑
                    }
                    //6、把解析到的登录信息传递给后续服务
                    request.mutate().header("userId", map.get("userId").toString()).build();
                    exchange.mutate().request(request).build();

                    //7、放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    //重定向到登录，使用response重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // request.getURI()获取一个包含协议请求头的完整的路径
                    return response.setComplete(); // 拦截后续业务逻辑
                }
            }
        };
    }

    @Data
    public static class PathConfig {
        private List<String> paths;
//        private String key;
//        private String value;
    }
}
