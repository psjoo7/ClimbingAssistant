package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecordActivity extends AppCompatActivity {
    private String UserID;
    private DatabaseReference mData, mRef;
    private LinearLayout recordlinear;
    private Button mainbtn;
    private Integer count;
    private TextView counttext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        count = 0;
        counttext = findViewById(R.id.count);
        Intent getResultintent = getIntent();
        UserID = getResultintent.getStringExtra("UserID");
        Log.d("userid" , " " + UserID);
        UserID = UserID.split("[.]")[0];
        mRef = FirebaseDatabase.getInstance().getReference("UserRecord");
        mData = mRef.child(UserID);
        recordlinear = findViewById(R.id.recordview);
        mData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserRecord record_each = snapshot.getValue(UserRecord.class);
                    final TextView recordview = new TextView(getBaseContext());
                    recordview.setText(record_each.mname + "    " + record_each.arrivalRate + "%");
                    recordview.setTextSize(20);
                    recordview.setTypeface(null, Typeface.BOLD);
                    recordview.setBackground(getDrawable(R.drawable.textview_custom));

                    recordlinear.addView(recordview);
                    recordview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RecordActivity.this, ResultActivity.class);
                            intent.putExtra("mname", record_each.mname);
                            intent.putExtra("record",record_each.record);
                            intent.putExtra("UserID",UserID);
                            intent.putExtra("level",record_each.level);
                            intent.putExtra("time",record_each.time);
                            intent.putExtra("Rate", record_each.arrivalRate);
                            startActivity(intent);
                            finish();
                        }
                    });
                    count++;
                }
                counttext.setText(" X " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mainbtn = findViewById(R.id.mainBtn);
        mainbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}