package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\Sgg\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\Sgg\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        this.testGetRsa();

        String token = JwtUtils.generateToken(map, privateKey, 2);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTYzMTY4MzJ9.CFoxE_pb3R3KEhh0ODkVzn-qmFsP4OjaQYgsH5jzWj7_lGNNPMXHDFWRh_qOHpDR8EKj7Jguc7xqy9hSijBJfqXwTkb4s5AwWADP9XV-5A3qC7Po0fJ6CCLIU3jFEQiNCSDgCHT6MsQwn7vcISM4h_epoJbsJYBSCLgae07MGwjVA1H_F9rEEGu6iuc9VyDoAbPw8y07qx_G-BuQr1D_dp-HB562INiDVX_cON7UEyDOwqUo75hJTcwREUin0o8MJc5tTUwiDrflSZKp0On-MKWHPo-kXCBOFQVfnQ2a-zBO3eoNlL574EBd-4FymM0Falh3H8fP08LsuaUbmAmjxg";

        // 解析token
        this.testGetRsa();
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}