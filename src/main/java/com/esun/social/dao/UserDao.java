package com.esun.social.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> registerUserSP(String phone, String userName, String hashedPassword) {

        // 放棄使用 SimpleJdbcCall，改用最底層、最直接的 jdbcTemplate.execute
        return jdbcTemplate.execute("{call sp_register_user(?, ?, ?, ?, ?)}", (CallableStatement cs) -> {
            
            // 1. 設定傳給 MySQL 的 3 個輸入參數 (IN)
            cs.setString(1, phone);
            cs.setString(2, userName);
            cs.setString(3, hashedPassword);
            
            // 2. 告訴 MySQL 我們要接收的 2 個輸出參數 (OUT) 的型態
            cs.registerOutParameter(4, Types.INTEGER); // 對應 p_status
            cs.registerOutParameter(5, Types.VARCHAR); // 對應 p_message
            
            // 3. 執行預存程序！
            cs.execute();
            
            // 4. 把 MySQL 吐回來的結果打包成 Map 回傳給 Service
            Map<String, Object> result = new HashMap<>();
            result.put("p_status", cs.getInt(4));
            result.put("p_message", cs.getString(5));
            return result;
        });
    }
    public Map<String, Object> getUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try {
            // 這裡我們直接用簡單的 queryForMap，因為只是單純查詢
            return jdbcTemplate.queryForMap(sql, phone);
        } catch (Exception e) {
            return null; // 找不到人
        }
    }
}