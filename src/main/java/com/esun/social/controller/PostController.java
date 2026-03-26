package com.esun.social.controller;

import com.esun.social.service.PostService;
import com.esun.social.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private JwtUtil jwtUtil;

    // 輔助方法：從 Header 驗證並取得手機號碼
    private String getPhoneFromToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) throw new Exception("請先登入");
        String phone = jwtUtil.validateTokenAndGetPhone(authHeader.substring(7));
        if (phone == null) throw new Exception("無效的通行證");
        return phone;
    }

 // 在 PostController.java 中修改
    @PostMapping
    public Map<String, Object> create(
        @RequestHeader(value = "Authorization", required = false) String token, // 加入 required = false
        @RequestBody Map<String, String> body) {
        
        Map<String, Object> res = new HashMap<>();
        try {
            if (token == null) throw new Exception("請先登入（Header 缺少 Authorization）");
            postService.createPost(getPhoneFromToken(token), body.get("content"));
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false); 
            res.put("message", e.getMessage());
        }
        return res;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return postService.getAllPosts();
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@RequestHeader("Authorization") String token, @PathVariable int id, @RequestBody Map<String, String> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            postService.updatePost(id, getPhoneFromToken(token), body.get("content"));
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false); res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/{id}/comments")
    public Map<String, Object> comment(@RequestHeader("Authorization") String token, @PathVariable int id, @RequestBody Map<String, String> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            postService.addComment(id, getPhoneFromToken(token), body.get("content"));
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false); res.put("message", e.getMessage());
        }
        return res;
    }
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable int id) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 1. 驗證身分
            String phone = getPhoneFromToken(token);
            
            // 2. 呼叫 Service 執行刪除
            postService.deletePost(id, phone);
            
            res.put("success", true);
        } catch (Exception e) {
            // 如果不是本人的文章，Service 會拋出 Exception，這裡就會接到並回傳錯誤訊息
            res.put("success", false); 
            res.put("message", e.getMessage());
        }
        return res;
    }
}