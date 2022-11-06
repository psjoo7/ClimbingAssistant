package com.example.googlemaptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        startLoading();
    }

    //로딩화면 먼저 키고 딜레이 5초 주고 화면 종료 -> 메인액티비로 화면 전환
    private void startLoading() {
        Handler handle = new Handler();
        handle.postDelayed(new Runnable(){
            @Override
            public void run() {
                finish();
            }
        },3000);
    }
}
