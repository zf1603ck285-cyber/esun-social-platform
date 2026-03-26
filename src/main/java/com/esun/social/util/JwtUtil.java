package com.esun.social.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 這是一把秘密鑰匙，用來簽名你的通行證（實務上會放在設定檔）
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // 設定通行證有效時間（例如：24 小時）
    private final long expirationTime = 86400000; 

    public String generateToken(String phone, String userName) {
        return Jwts.builder()
                .setSubject(phone)
                .claim("userName", userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }
    
    public String validateTokenAndGetPhone(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // 用同一把鑰匙解密
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // 如果驗證成功，傳回手機號碼
        } catch (Exception e) {
            return null; // 驗證失敗（被竄改過或過期）
        }
    }
}