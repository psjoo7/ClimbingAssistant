package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String time, record, mname, UserID;
    private Double ArrivalRate;

    private TextView weather_result;
    private TextView ArrivalRate_result;
    private TextView time_result;
    private TextView level_result;

    private Task<Void> mData;
    private DatabaseReference mRef;
    private Button TraceButton,mainBtn;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        weather_result = findViewById(R.id.weather);
        ArrivalRate_result = findViewById(R.id.Arrival_rate_result);
        time_result = findViewById(R.id.time_result);
        level_result = findViewById(R.id.level);

        Intent getRecords = getIntent();
        time = getRecords.getStringExtra("time");
        record = getRecords.getStringExtra("record");
        mname = getRecords.getStringExtra("mname");
        UserID = getRecords.getStringExtra("UserID");
        level = getRecords.getStringExtra("level");
        Log.d("currentLLLL1",level);
        ArrivalRate = getRecords.getDoubleExtra("Rate",0);
        Log.d("RRRRR",ArrivalRate+"%");
        Log.d("userID",UserID+"");
//        UserID = UserID.split("[.]")[0];
        ArrivalRate_result.setText(ArrivalRate+"");
//        ArrivalRate_result.setText(String.format("%.2f",ArrivalRate)+"");
        time_result.setText(time);
        level_result.setText(level);
        //weather_result.setText(
//        Record userRecord = new Record(time, record, mname, UserID, ArrivalRate, level);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        mRef = FirebaseDatabase.getInstance().getReference("UserRecord");
//        mData = mRef.child(UserID).push().setValue(userRecord);
        TraceButton = findViewById(R.id.TraceButton);
        TraceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, RecordActivity.class);
                intent.putExtra("UserID", UserID);
                startActivity(intent);
                finish();
            }
        });
        mainBtn = findViewById(R.id.mainBtn);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.putExtra("UserID", UserID);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        drawLine(mMap, record);
        String location = record.split(",")[0];
        LatLng latlng = new LatLng(Double.parseDouble(location.split(" ")[1]), Double.parseDouble(location.split(" ")[0]));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 20));
    }

    // polyline을 그리는 함수, path를 받아온다.
    public void drawLine(GoogleMap Map , String path){
        PolylineOptions polylineOptions = new PolylineOptions();
        try {
            String[] path_list = path.split(",");
            for (int i = 0; i < path_list.length; i++) {
                path_list[i] = path_list[i].trim();
                Double lat = Double.parseDouble(path_list[i].split(" ")[0]);
                Double lng = Double.parseDouble(path_list[i].split(" ")[1]);
                polylineOptions.add(new LatLng(lng, lat));
                polylineOptions.width(10);
                polylineOptions.color(Color.BLUE);
            }
            Polyline polyline = Map.addPolyline(polylineOptions);

        }
        catch (Exception e){}
    }
}