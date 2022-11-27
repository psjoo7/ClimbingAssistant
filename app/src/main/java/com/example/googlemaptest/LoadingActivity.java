package com.example.googlemaptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent intent = new Intent(LoadingActivity.this,LoginActivity.class);
        startActivity(intent);
        //다음 화면에 실행할 애니메이션, 현재 화면에 실행할 애니메이션
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();

    }
}
