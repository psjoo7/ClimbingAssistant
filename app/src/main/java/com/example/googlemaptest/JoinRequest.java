package com.example.googlemaptest;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinRequest extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://psjoo7pproject.dothome.co.kr/SantaRegister.php";
    private Map<String, String> map;

    public JoinRequest(String UserID, String UserPwd, String UserName, String UserAdd, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("UserID", UserID);
        map.put("UserPwd", UserPwd);
        map.put("UserName", UserName);
        map.put("UserAdd", UserAdd);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
