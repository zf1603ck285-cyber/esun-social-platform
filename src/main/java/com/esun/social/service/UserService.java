package com.esun.social.service;

import com.esun.social.dao.UserDao;
import com.esun.social.dto.UserRegisterRequest;
import com.esun.social.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // --- 註冊邏輯 ---
    @Transactional
    public String register(UserRegisterRequest request) throws Exception {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Map<String, Object> result = userDao.registerUserSP(
                request.getPhone(),
                request.getUserName(),
                hashedPassword
        );

        Integer status = (Integer) result.get("p_status");
        String message = (String) result.get("p_message");

        if (status == 0) {
            throw new Exception(message);
        }
        return message;
    }

    // --- 登入邏輯 (請保留這個，刪除那個 return null 的) ---
    public String login(String phone, String rawPassword) throws Exception {
        // 1. 找人
        Map<String, Object> user = userDao.getUserByPhone(phone);
        
        if (user == null) {
            throw new Exception("帳號或密碼錯誤！");
        }

        // 2. 比對密碼
        String hashedPassword = (String) user.get("password");
        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            throw new Exception("帳號或密碼錯誤！");
        }

        // 3. 成功後核發 Token
        String userName = (String) user.get("user_name");
        return jwtUtil.generateToken(phone, userName);
    }
}