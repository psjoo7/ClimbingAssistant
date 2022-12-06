package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class startMountActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    LinkedList<Location> location_info = new LinkedList<>();
    private static final int UPDATE_INTERVAL_MS = 3000;  // 3초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 3000; // 3초
    private String MountName, UserID;
    private DatabaseReference mData, mRef;
    private String path;
    private GoogleMap mMap;
    private TextView mname;
    private TextView arrivalRate;
    private Chronometer stopWatch;
    private Marker currentMarker = null;
    private Button stop, mainBtn;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private double CurElevation;
    private double GoalElevation;
    private double CurRate;
    private String[] record;
    private String userpath = "";
    private String currentLevel;
    private TextView levelView;
    Location mCurrentLocatiion;
    LatLng currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mount);

        Intent getSearchIntent = getIntent();
        MountName = getSearchIntent.getStringExtra("MountName");
        UserID = getSearchIntent.getStringExtra("UserID");
        GoalElevation = Double.parseDouble(getSearchIntent.getStringExtra("MaxHeight"));
        currentLevel = getSearchIntent.getStringExtra("level");
        Log.d("level",currentLevel);
        Log.d("MaxHeight", "elevation : "+GoalElevation);
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
        arrivalRate = findViewById(R.id.arrival_rate);
        arrivalRate.setText("0");
        mname = findViewById(R.id.start);
        mname.setText(MountName);
//        levelView= findViewById(R.id.levelview);
//        levelView.setText(currentLevel);
        //xml 추가 후 이거
        Chronometer stopWatch  = (Chronometer) findViewById(R.id.chronometer);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                cArg.setText(timeHandler(time,stopWatch));
            }
        });
        long currentTime =  SystemClock.elapsedRealtime() - stopWatch.getBase();
        String time1 = timeHandler(currentTime, stopWatch);
        stopWatch.setBase(SystemClock.elapsedRealtime());
        stopWatch.start();
        stop = findViewById(R.id.stopBtn);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recordtime = timeHandler(currentTime, stopWatch);
                Intent intent = new Intent(startMountActivity.this, ResultActivity.class);
                intent.putExtra("time", recordtime);
                intent.putExtra("mname", MountName);
                intent.putExtra("record", userpath);
                intent.putExtra("UserID", UserID);
                intent.putExtra("Rate",CurRate);// 현재 달성률 전달
                startActivity(intent);
            }
        });

        mainBtn = findViewById(R.id.mainBtn);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(startMountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public String timeHandler(long time, Chronometer stopWatch)
    {
        long currentTime =  SystemClock.elapsedRealtime() - stopWatch.getBase();

        int h = (int)(currentTime / 3600000);
        int m = (int)(currentTime - h * 3600000) / 60000;
        int s = (int)(currentTime - h * 3600000 - m * 60000) / 1000 ;

        String hh = h < 10 ? "0"+h: h+"";
        String mm = m < 10 ? "0"+m: m+"";
        String ss = s < 10 ? "0"+s: s+"";
        return hh+":"+mm+":"+ss;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");
        Log.d("startMountAct", "getIntent"+MountName);
        mData = FirebaseDatabase.getInstance().getReference("MountainList");
        mRef = mData.child(MountName.split(",")[0]);
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
                LatLng midLatlng = new LatLng((log + slog) / 2, (lag + slag) / 2);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLatlng, 15));
                path = mountInfo.path;
                Log.d("startpath", "getIntent1 "+ path);
                drawLine(mMap, path);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.52487, 126.92723)));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("cccc", "cccc2");
                Log.d("onmapready", "onMapClick :");
            }
        });
    }


    public String getElevation(double latitude, double longitude) {
        try {
            String key = "AIzaSyCGQ8fB1BVIr4mq-lHuxjaQy4EvFomeXaQ";
            String url = "https://maps.googleapis.com/maps/api/elevation/json";
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/elevation/json?locations=" + latitude + "%2C" + longitude + "&key=" + key)
                    .method("GET", null)
                    .build();

            Response response = client.newCall(request).execute();
            String msg = response.body().string();
//            System.out.println(msg);
            JSONObject object = new JSONObject(msg);
            String results = object.getString("results");
            JSONArray objectArr = new JSONArray(results);
            JSONObject result = new JSONObject(objectArr.getString(0));
            String ele = result.getString("elevation");
            return ele;
        } catch (Exception e) {
            return (e.toString());
        }
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
        Log.d("cccc", "cccc4");
        mMap.setMyLocationEnabled(true);
    }
    @Override
    protected void onStart() {
        super.onStart();

        Log.d("cccc", "cccc5");
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
        if (mMap!=null)
            mMap.setMyLocationEnabled(true);
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onStop() {

        super.onStop();
        Log.d("cccc", "cccc6");
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            Log.d("cccc", "cccc3");

            if (locationList.size() > 0) {

                location = locationList.get(locationList.size() - 1);
                location_info.add(location);
                //location = locationList.get(0);
                String a = convertLocToString(location);
                userpath += a;

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                new Thread(() -> {
                    Looper.prepare();
                    String ele = getElevation(location.getLatitude(), location.getLongitude());
                    Handler handler = new Handler();
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    bundle.putString("elevation", ele);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    CurElevation = Double.parseDouble(ele);
                    Log.d("ele", "ele : " + getElevation(location.getLatitude(), location.getLongitude()));

                }).start();
                CurRate = Math.floor(CurElevation/GoalElevation*100);
                if(CurRate >= 100)
                {
                    CurRate = 100;
                }
                Log.d("Rate",CurRate+"");
                arrivalRate.setText(CurRate+"%");

                //현재 위치에 마커 생성하고 이동

                mCurrentLocatiion = location;

                String.valueOf(location.getLatitude());

                drawLine(mMap, location_info);
                Log.d("locationcallback", "onLocationResult: 현재경로 표시");

            }
        }
    };

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        Log.d("cccc", "cccc7");
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
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
            Log.d("get endCap", String.valueOf(polyline.getEndCap()));
            Log.d("get endCap", String.valueOf(polyline.getJointType()));
            Log.d("get endCap", String.valueOf(polyline.getPoints()));
            Log.d("get endCap", String.valueOf(polyline.getZIndex()));
        }
        catch (Exception e){}
    }

    public void drawLine(GoogleMap map, LinkedList<Location> l)
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        Log.d("cccc", "cccc10");
        for(int i=0; i<l.size() ; i++)
        {
            polylineOptions.add(new LatLng(l.get(i).getLatitude(),l.get(i).getLongitude()));
        }
        Polyline polyline = map.addPolyline(polylineOptions);
        polyline.setColor(0xffff0000);

    }
    private String convertLocToString(Location location)
    {
        String coordinate = String.valueOf(location.getLatitude())+" "+ String.valueOf(location.getLongitude())+",";
        return coordinate;
    }
}