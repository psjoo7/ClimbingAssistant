package com.example.googlemaptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;


public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        startLoading();

        /* 이거 애니메이션 버전인데 이거 오늘까지 수정함 일단 합치기 위해 올림
        Intent intent = new Intent(LoadingActivity.this,LoginActivity.class);
        startActivity(intent);
        //다음 화면에 실행할 애니메이션, 현재 화면에 실행할 애니메이션
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish(); */

    }
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
