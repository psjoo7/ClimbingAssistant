package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {
    private EditText ID, Pwd;
    private Button JoinBtn, LoginBtn, ManageBtn;
    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스(서버에 연결)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserInfo");

        // 타이틀바 삭제


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

                mFirebaseAuth.signInWithEmailAndPassword(UserID, UserPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //로그인 성공
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            Log.d("LoginActivity", "Login Worked");
                            startActivity(intent);
                            finish(); //현재 액티비티 파괴
                        }else {
                            Toast.makeText(LoginActivity.this, "로그인 실패..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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