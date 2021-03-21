package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/07/21:33
 * @Description: 解决跨域问题的过滤器
 ******************************************/
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {

        //cors配置类
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨域访问的域名，*代表所有，但是使用*就不能携带cookie
        corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
        corsConfiguration.addAllowedOrigin("http://gmall.com");
        corsConfiguration.addAllowedOrigin("http://www.gmall.com");
        //是否允许携带cookie
        corsConfiguration.setAllowCredentials(true);
        //允许携带所有的头信息
        corsConfiguration.addAllowedHeader("*");
        //允许所有的请求方式跨域访问
        corsConfiguration.addAllowedMethod("*");

        //添加映射路径，拦截所有请求
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        //注册一个cors配置，并设置对哪些路径访问
        corsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource); //
    }
}
