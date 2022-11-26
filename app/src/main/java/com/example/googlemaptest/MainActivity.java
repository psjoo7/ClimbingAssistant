package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;



import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener{
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private AlertDialog dialog;
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    LinkedList<Location> location_info = new LinkedList<>();




    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    LatLng currentPosition;

    public Location loc_current;
    private Button updateBtn;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private FragmentManager fragmentmanager;
    private FragmentTransaction transaction;
    private TextView location_log;
    private TextView isOK;
    private Button done;
    private DatabaseReference mMount;
    public List<MountElement> mount = new ArrayList<>();

    private ChildEventListener mChildEventListener;
    Marker marker;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        location_log = findViewById(R.id.Location_log);
        mLayout = findViewById(R.id.layout_main);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMount = FirebaseDatabase.getInstance().getReference("MountainList");
        ChildEventListener mChildEventListener;
        mMount.push().setValue(marker);
        LocationManager lm =(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        updateBtn = findViewById(R.id.setLocationBtn);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION","위치권한을 확인하세요");
        }
        else
        {
            loc_current = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }

        // 1. updateBtn 클릭하면 현재 내 좌표 받아온다. (lat, lng)
        // 2. 산 리스트에 있는 모든 산과 거리 비교 후 산 리스트를 거리순으로 정렬
        updateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                double lat = loc_current.getLatitude();
                double lng = loc_current.getLongitude();
                Log.d("LLL","lat : "+lat +" lng : "+lng);
                int idx = 0;
                for(int i=0; i<mount.size(); i++)
                {
                    mount.get(i).setDistance(lat,lng);
                    Log.d("disdis","distance : "+ mount.get(i).distance + " index : "+ idx++);

                }
                Log.d("before",mount.toString());
                Collections.sort(mount);
                Log.d("after",mount.toString());

                //여기에 카드뷰 추가하는 코드 추가하면 끝날듯.
            }
        });
    }







    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        Log.d("cccc", "cccc1");
        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        startLocationUpdates(); // 3. 위치 업데이트 시작
        mMount.addListenerForSingleValueEvent(new ValueEventListener() {
            int x = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MountElement mount_each = snapshot.getValue(MountElement.class);
                    MountElement eMount = new MountElement(mount_each.end, mount_each.length, mount_each.maxHeight, mount_each.mname, mount_each.path, mount_each.starting);
                    try {
                        mount.add(eMount);
                    } catch (NullPointerException e) {
                    }
                    Log.d("MainActivity", "ValueEventListener : " + mount_each.mname);
                    String endPoint = mount_each.end;
                    String[] endPointSplit = endPoint.split(" ");
                    Log.d("MainActivity", "ValueEventListener : " + endPointSplit[0]);
                    Log.d("MainActivity", "ValueEventListener : " + endPointSplit[1]);
                    Double lag = Double.parseDouble(endPointSplit[0]);
                    Double log = Double.parseDouble(endPointSplit[1]);
                    LatLng latLng = new LatLng(log, lag);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(mount_each.mname).snippet(String.valueOf(mount_each.maxHeight)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));                }

                    //100개의 산을 모두 불러오기 전 까지 updata 중지
                    updateBtn.setEnabled(true);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.52487, 126.92723)));
        mMap.setOnMarkerClickListener(this);



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
        Log.d("cccc", "cccc5");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mMap!=null)
            mMap.setMyLocationEnabled(true);
    }




    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 5);
        mMap.moveCamera(cameraUpdate);

    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(marker.getTitle() + "\n").setMessage("해당산을 등산하겠습니까?");
        builder.setNegativeButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, startMountActivity.class);
                intent.putExtra("MountName", String.valueOf(marker.getTitle()));
                Log.d("onMarkerClick", "touchedMountainName : " + marker.getTitle());
                startActivity(intent);
            }
        });
        builder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(),"산을 선택해주세요.", Toast.LENGTH_LONG).show();
            }
        });
        dialog = builder.create();
        dialog.show();
        return true;
    }
}