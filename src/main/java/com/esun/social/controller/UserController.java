package com.esun.social.controller;

import com.esun.social.dto.UserRegisterRequest;
import com.esun.social.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * [POST] /api/users/register
     * 使用者註冊接口
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody UserRegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = userService.register(request);
            response.put("success", true);
            response.put("message", message);
        } catch (Exception e) {
            // 保持良好的除錯習慣，印出堆疊訊息
            e.printStackTrace(); 
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * [POST] /api/users/login
     * 使用者登入接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 從 JSON 中取得手機與密碼
            String phone = loginRequest.get("phone");
            String password = loginRequest.get("password");

            // 呼叫 Service 驗證並取得 JWT Token
            String token = userService.login(phone, password);

            response.put("success", true);
            response.put("token", token); // 回傳這串最重要的通行證
            response.put("message", "登入成功！");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}