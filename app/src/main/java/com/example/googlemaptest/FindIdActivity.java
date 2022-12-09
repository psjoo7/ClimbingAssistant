package com.example.googlemaptest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindIdActivity extends AppCompatActivity {
    private DatabaseReference mRef;
    private EditText name;
    private ImageButton FindBtn;
    private LinearLayout findlinear;
    private Button BackBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        name = findViewById(R.id.userName);
        findlinear = findViewById(R.id.linearLayout);
        FindBtn = findViewById(R.id.FindButton);
        BackBtn = findViewById(R.id.BackButton);
        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindIdActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        FindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("working", "::::");
                final String userName = name.getText().toString();
                mRef = FirebaseDatabase.getInstance().getReference("UserInfo");
                Query mData = mRef.child("UserAccount").orderByChild("name").equalTo(userName);
                mData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            int textColor = ContextCompat.getColor(getApplicationContext(), R.color.black);
                            UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                            final TextView linearLayout = new TextView(getBaseContext());
                            linearLayout.setText("아이디 : "+userAccount.getEmailId() + "\n\n"
                            + "비밀번호 : " + userAccount.getPassword() + "\n\n"
                            + "이름 : " + userAccount.getPhoneNum());
                            Log.d("working", "::::" + userAccount.getPassword());
                            linearLayout.setTextColor(textColor);
                            linearLayout.setTextSize(20);
                            linearLayout.setTypeface(null, Typeface.BOLD);
                            linearLayout.setBackground(getDrawable(R.drawable.textview_custom));
                            findlinear.addView(linearLayout);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

}
