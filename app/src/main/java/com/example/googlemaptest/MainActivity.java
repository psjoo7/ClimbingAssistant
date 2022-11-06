package com.example.googlemaptest;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        LatLng CURRENTLOCATION = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(CURRENTLOCATION);
        markerOptions.title("현재 위치");
        markerOptions.snippet("현재 위치입니다. 위도 :" + latitude + ", 경도 : " + longitude);
        mMap.addMarker(markerOptions);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENTLOCATION, 10));

    }

}