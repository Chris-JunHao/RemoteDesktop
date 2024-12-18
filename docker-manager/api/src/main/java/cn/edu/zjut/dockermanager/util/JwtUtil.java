package cn.edu.zjut.dockermanager.util;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JwtUtil {

    // 使用 MD5 加密的字符串作为密钥生成 HMAC-SHA 密钥
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(MD5Util.md5("XanderYe").getBytes());

    /**
     * 编码用户名为 JWT
     * 使用 HMAC-SHA 加密算法对用户名进行签名，并使用 GZIP 压缩编码。
     * @param username 用户名
     * @return 编码后的 JWT 字符串
     * @author XanderYe
     * @date 2020/11/25
     */
    public static String encode(String username){
        return Jwts.builder()
                .setSubject(username) // 设置主题（即用户名）
                .compressWith(CompressionCodecs.GZIP) // 使用 GZIP 压缩
                .signWith(SECRET_KEY) // 使用密钥签名
                .compact(); // 创建 JWT 字符串
    }

    /**
     * 解码 JWT 获取用户名
     * 解析传入的 JWT 字符串，验证签名后，获取其中的用户名（主题）。
     * @param jwt JWT 字符串
     * @return 解码后的用户名
     * @throws ExpiredJwtException 如果 JWT 已过期，抛出此异常
     * @author XanderYe
     * @date 2020/11/25
     */
    public static String decode(String jwt) throws ExpiredJwtException{
        return Jwts.parser() // 创建 JWT 解析器
                .setSigningKey(SECRET_KEY) // 设置签名密钥
                .parseClaimsJws(jwt) // 解析 JWT
                .getBody() // 获取声明部分（payload）
                .getSubject(); // 返回用户名（subject）
    }
}
