package com.example.googlemaptest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText ID, Pwd;
    private Button JoinBtn, LoginBtn, ManageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 타이틀바 삭제
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        ID = findViewById(R.id.userID);
        Pwd = findViewById(R.id.userPwd);
        JoinBtn = findViewById(R.id.JoinButton);
        LoginBtn = findViewById(R.id.LoginButton);
        ManageBtn = findViewById(R.id.ManageButton);
        //로그인 버튼 클릭시
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserID = ID.getText().toString();
                String UserPwd = Pwd.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject( response );
                            boolean success = jsonObject.getBoolean( "success" );

                            if(success) {//로그인 성공시

                                String UserID = jsonObject.getString( "UserID" );
                                String UserPwd = jsonObject.getString( "UserPwd" );
                                String UserName = jsonObject.getString( "UserName" );
                                String UserAdd = jsonObject.getString("UserAdd");

                                Toast.makeText( getApplicationContext(), String.format("%s님 환영합니다.", UserName), Toast.LENGTH_SHORT ).show();
                                Intent intent = new Intent( LoginActivity.this, SplashActivity.class );

                                intent.putExtra( "UserID", UserID );
                                intent.putExtra( "UserPwd", UserPwd );
                                intent.putExtra( "UserName", UserName );
                                intent.putExtra("UserAdd", UserAdd);

                                startActivity( intent );

                            } else {//로그인 실패시
                                Toast.makeText( getApplicationContext(), "로그인에 실패하셨습니다.", Toast.LENGTH_SHORT ).show();
                                return;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest( UserID, UserPwd, responseListener );
                RequestQueue queue = Volley.newRequestQueue( LoginActivity.this );
                queue.add( loginRequest );
            }
        });
        //회원가입 버튼 클릭시
        JoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}