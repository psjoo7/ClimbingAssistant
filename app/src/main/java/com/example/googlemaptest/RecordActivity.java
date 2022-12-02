package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent getResultintent = getIntent();
        UserID = getResultintent.getStringExtra("UserID");

        mRef = FirebaseDatabase.getInstance().getReference("UserRecord");
        mData = mRef.child(UserID);
        recordlinear = findViewById(R.id.recordview);
        mData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserRecord record_each = snapshot.getValue(UserRecord.class);
                    final TextView recordview = new TextView(getBaseContext());
                    recordview.setText(record_each.mname);
                    recordview.setTextSize(20);
                    recordview.setTypeface(null, Typeface.BOLD);
                    recordview.setBackground(getDrawable(R.drawable.textview_custom));

                    recordlinear.addView(recordview);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}