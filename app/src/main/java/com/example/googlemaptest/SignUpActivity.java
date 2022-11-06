package com.example.googlemaptest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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

public class SignUpActivity extends AppCompatActivity {
    private EditText ID, Pwd, CheckPwd, Name, Add;
    private Button checkBtn, signUpBtn, backBtn;
    private AlertDialog dialog;
    private boolean validate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ID = findViewById(R.id.userID);
        Pwd = findViewById(R.id.userPwd);
        CheckPwd = findViewById(R.id.userCPwd);
        Name = findViewById(R.id.userName);
        Add = findViewById(R.id.userAdd);
        checkBtn = findViewById(R.id.CheckButton);
        signUpBtn = findViewById(R.id.signupButton);
        backBtn = findViewById(R.id.BackButton);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserID = ID.getText().toString();
                if (validate) {
                    return; //검증 완료
                }
                if (UserID.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                            if (success) {
                                dialog = builder.setMessage("사용할 수 있는 아이디입니다.").setPositiveButton("확인", null).create();
                                dialog.show();
                                ID.setEnabled(false); //아이디값 고정
                                validate = true; //검증 완료
                                checkBtn.setBackgroundColor(Color.GRAY);
                            }
                            else {
                                dialog = builder.setMessage("이미 존재하는 아이디입니다.").setNegativeButton("확인", null).create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ValidateRequest validateRequest = new ValidateRequest(UserID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);
                queue.add(validateRequest);
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String UserID = ID.getText().toString();
                final String UserPwd = Pwd.getText().toString();
                final String UserName = Name.getText().toString();
                final String checkPwd = CheckPwd.getText().toString();
                final String UserAdd = Add.getText().toString();
                //한 칸이라도 입력 안했을 경우
                if (UserID.equals("") || UserPwd.equals("") || UserName.equals("") || CheckPwd.equals("") || UserAdd.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    dialog = builder.setMessage("모두 입력해주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    dialog = builder.setMessage("중복된 아이디가 있는지 확인하세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject( response );
                            boolean success = jsonObject.getBoolean( "success" );
                            //회원가입 성공시
                            if(UserPwd.equals(checkPwd)) {
                                if (success) {
                                    Toast.makeText(getApplicationContext(), String.format("%s님 가입을 환영합니다.", UserName), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    //회원가입 실패시
                                } else {
                                    Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                dialog = builder.setMessage("비밀번호가 동일하지 않습니다.").setNegativeButton("확인", null).create();
                                dialog.show();
                                return;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                //서버로 Volley를 이용해서 요청
                JoinRequest registerRequest = new JoinRequest( UserID, UserPwd, UserName, UserAdd, responseListener);
                RequestQueue queue = Volley.newRequestQueue( SignUpActivity.this );
                queue.add( registerRequest );


            }
        });
    }
}