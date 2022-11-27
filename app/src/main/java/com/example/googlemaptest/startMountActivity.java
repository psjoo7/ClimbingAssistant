package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class startMountActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String MountName;
    private DatabaseReference mData, mRef;
    private String path;
    private GoogleMap mMap;
    private TextView mname;
    private String userpath;
    private Location location;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView ArrivalRate;
    public String elevation = "";
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    public boolean isend = false;
    private Chronometer stopWatch;
    LinkedList<Location> location_info = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mount);
        Intent getMainIntent = getIntent();
        MountName = getMainIntent.getStringExtra("MountName");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mname = findViewById(R.id.start);
        mname.setText(MountName);
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
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ArrivalRate = findViewById(R.id.arrival_rate);

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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                path = mountInfo.path;
                Log.d("startpath", "getIntent1 "+ path);
                drawLine(mMap, path);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            Log.d("cccc", "cccc3");

            if (locationList.size() > 0) {

                location = locationList.get(locationList.size() - 1);
                if(location != null)
                {
                    userpath+=convertLocToString(location);

                }
                Log.d("userPath","userPath : " + userpath);

                location_info.add(location);
                //location = locationList.get(0);
                LatLng currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("current Location"," : " + currentPosition.latitude + currentPosition.longitude );

                new Thread(() -> {
                    Looper.prepare();
                    String ele = getElevation(location.getLatitude(), location.getLongitude());
                    Handler handler = new Handler();
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    bundle.putString("elevation", ele);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    Log.d("elevation", " : "+ ele);
                    elevation = ele;

                }).start();
                if(elevation.length()>10)
                {
                    ArrivalRate.setText(elevation.substring(0,5));

                }
                else
                {
                    ArrivalRate.setText("0");
                }
                String Lat1 = String.valueOf(location.getLatitude());
                String Long1 = String.valueOf(location.getLongitude());


                String.valueOf(location.getLatitude());

                PolylineOptions polylineOptions = new PolylineOptions();

            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();

        Log.d("TAG", "onStart");
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
        Log.d("Start","위치시작");
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        if (checkPermission()) {
//
//            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
//
//            if (mMap!=null)
//                mMap.setMyLocationEnabled(true);
//        }
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        //다음 화면으로 전환
        //다음 화면에 넘길 것들 : 경로(userPath) , 고도(elevation) , 시간
    }

    private String convertLocToString(Location location)
    {
        String coordinate = String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude())+"/";
        return coordinate;
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

}