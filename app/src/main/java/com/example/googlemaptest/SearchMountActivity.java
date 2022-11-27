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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SearchMountActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mData, mRef;
    private GoogleMap mMap;
    private String MountName;
    private String path;
    private Button startBtn, backBtn;
    private double MaxHeight;
    private TextView info1, info2, info3, name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_mount);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startBtn = findViewById(R.id.startBtn);
        backBtn = findViewById(R.id.backBtn);
        info1 = findViewById(R.id.mount_list_info1);
        info2 = findViewById(R.id.mount_list_info2);
        info3 = findViewById(R.id.mount_list_info3);
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");
        name = findViewById(R.id.mountName);
        name.setText(MountName);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchMountActivity.this, startMountActivity.class);
                intent.putExtra("MountName", MountName+","+MaxHeight);
                startActivity(intent);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchMountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");
        mData = FirebaseDatabase.getInstance().getReference("MountainList");
        mRef = mData.child(MountName);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MountElement mountInfo = snapshot.getValue(MountElement.class);
                String startPoint = mountInfo.starting;
                String endPoint = mountInfo.end;
                String[] startPointSplit = startPoint.split(" ");
                String[] endPointSplit = endPoint.split(" ");
                Log.d("MainActivity", "ValueEventListener : " + endPointSplit[0]);
                Log.d("MainActivity", "ValueEventListener : " + endPointSplit[1]);
                Double lag = Double.parseDouble(endPointSplit[0]);
                Double log = Double.parseDouble(endPointSplit[1]);
                Double slag = Double.parseDouble(startPointSplit[0]);
                Double slog = Double.parseDouble(startPointSplit[1]);
                LatLng latLng = new LatLng(log, lag);
                LatLng latLng1 = new LatLng(slog, slag);
                mMap.addMarker(new MarkerOptions().position(latLng).title(mountInfo.mname).snippet(String.valueOf(mountInfo.maxHeight)));
                mMap.addMarker(new MarkerOptions().position(latLng1).title(mountInfo.mname).snippet(String.valueOf(mountInfo.maxHeight)));
                LatLng midLatlng = new LatLng((log + slog) / 2, (lag + slag) / 2);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLatlng, 15));
                path = mountInfo.path;
                info1.setText("Mountain Name : " + mountInfo.mname);
                info2.setText("Max Height : " + String.valueOf(mountInfo.maxHeight) + "Meter");
                MaxHeight = mountInfo.maxHeight;
                info3.setText("Hiking Distance : " + String.valueOf(mountInfo.length) + "Kilometer");
                Log.d("startpath", "getIntent1 "+ path);
                drawLine(mMap, path);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("cccc", "cccc2");
                Log.d("onmapready", "onMapClick :");
            }
        });
    }

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