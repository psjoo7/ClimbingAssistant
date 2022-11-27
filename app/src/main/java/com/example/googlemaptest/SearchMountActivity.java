package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DatabaseReference;

public class SearchMountActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mData, mRef;
    private GoogleMap mMap;
    private String MountName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_mount);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");
    }
}