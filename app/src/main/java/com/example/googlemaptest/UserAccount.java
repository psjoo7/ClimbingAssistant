package com.example.googlemaptest;

public class UserAccount {
    private String idToken;
    private String emailId; // 이메일 아이디
    private String password; // 비밀번호
    private String name; // 사용자 이름
    private String address; // 사용자 주소

    public UserAccount(){}

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
