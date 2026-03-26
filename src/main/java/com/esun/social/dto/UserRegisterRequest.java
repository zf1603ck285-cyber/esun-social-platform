package com.esun.social.dto;

public class UserRegisterRequest {
    // 註冊時，前端只需要傳這三個最重要的欄位給我們
    private String phone;
    private String userName;
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}