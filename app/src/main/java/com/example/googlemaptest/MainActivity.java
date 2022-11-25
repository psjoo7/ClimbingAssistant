package com.example.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;
import okhttp3.Request;
import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {
    LinkedList<Location> location_info = new LinkedList<>();
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private AlertDialog dialog;
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    int Goal = 6;
    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private FragmentManager fragmentmanager;
    private FragmentTransaction transaction;
    private TextView location_log;
    private TextView isOK;
    private Button done;
    private DatabaseReference mMount;
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


    }

    int JSONParse(String jsonStr) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int elevation = jsonObject.getInt("elevation");
                return elevation;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
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
            List<MountElement> mount = new ArrayList<>();
            int x = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MountElement mount_each = snapshot.getValue(MountElement.class);
                    MountElement eMount = new MountElement(mount_each.end, mount_each.length, mount_each.maxHeight, mount_each.mname, mount_each.path, mount_each.starting, x++);
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                    System.out.println("aaaaaaaa1");
                    for(int i = 0 ; i < mount.size(); i++){
                        System.out.print(mount.get(i).mname + " ");
                    }
                    System.out.println();

                    System.out.println();
                    Collections.sort(mount);
                System.out.println("aaaaaaaa2" + mount);
                    System.out.println(mount.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.52487, 126.92723)));
        //marker click event --> onMarkerClick func
        mMap.setOnMarkerClickListener(this);
        /**
         * 퍼미션은 모두 주석으로
         */
//        //런타임 퍼미션 처리
//        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
//        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
//                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
//
//            // 2. 이미 퍼미션을 가지고 있다면
//            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
//
//            startLocationUpdates(); // 3. 위치 업데이트 시작
//            mMount.addListenerForSingleValueEvent(new ValueEventListener() {
//                List<MountElement> mount = new ArrayList<>();
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                        MountElement mount_each = snapshot.getValue(MountElement.class);
//                        try {
//                            mount.add(mount_each);
//                        }
//                        catch (NullPointerException e){}
//                        Log.d("MainActivity", "ValueEventListener : " + mount_each.mname);
//                        String endPoint = mount_each.end;
//                        String[] endPointSplit = endPoint.split(" ");
//                        Log.d("MainActivity", "ValueEventListener : " + endPointSplit[0]);
//                        Log.d("MainActivity", "ValueEventListener : " + endPointSplit[1]);
//                        Double lag = Double.parseDouble(endPointSplit[0]);
//                        Double log = Double.parseDouble(endPointSplit[1]);
//                        LatLng latLng = new LatLng(log, lag);
//                        mMap.addMarker(new MarkerOptions().position(latLng).title(mount_each.mname).snippet(String.valueOf(mount_each.maxHeight)));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//                    }
////                    System.out.println("aaaaaaaa");
////                    for(int i = 0 ; i < mount.size(); i++){
////                        System.out.print(mount.get(i).mname + " ");
////                    }
////                    System.out.println();
////                    try {
////                        Arrays.sort(new List[]{mount});
////                    }
////                    catch (NullPointerException e){
////                        System.out.println(e);
////                    }
////                    for(int i = 0 ; i < mount.size(); i++){
////                        System.out.print(mount.get(i).mname + " ");
////                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//
//            });
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.52487,126.92723)));
//            //marker click event --> onMarkerClick func
//            mMap.setOnMarkerClickListener(this);
//
//        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
//
//            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
//
//                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
//                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
//                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View view) {
//
//                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
//                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
//                                PERMISSIONS_REQUEST_CODE);
//                    }
//                }).show();
//            } else {
//                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
//                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
//                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
//                        PERMISSIONS_REQUEST_CODE);
//            }
//        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("cccc", "cccc2");
                Log.d(TAG, "onMapClick :");
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
                location_info.add(location);
                //location = locationList.get(0);

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
                    double ele1 = Double.parseDouble(ele);

                }).start();

                String Lat1 = String.valueOf(location.getLatitude());
                String Long1 = String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;

                String.valueOf(location.getLatitude());

                PolylineOptions polylineOptions = new PolylineOptions();
                if (location_info.size() > 20) {
                    drawLine(mMap, location_info);
                    Log.d(TAG, "onLocationResult: 그림그렸다");
                }
            }
        }
    };

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

//        if (!checkLocationServicesStatus()) {
//
//            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
//            //퍼미션 주석 처리
////            showDialogForLocationServiceSetting();
//        }else {
//
//            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION);
//            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION);
//
//            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
//                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {
//
//                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
//                return;
//            }
//
//            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
//
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//
//            if (checkPermission())
//                mMap.setMyLocationEnabled(true);
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
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
    protected void onStop() {

        super.onStop();
        Log.d("cccc", "cccc6");
        mFusedLocationClient.removeLocationUpdates(locationCallback);
//        if (mFusedLocationClient != null) {
//
//            Log.d(TAG, "onStop : call stopLocationUpdates");
//            mFusedLocationClient.removeLocationUpdates(locationCallback);
//        }
    }

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
//퍼미션 주석 처리
//    public boolean checkLocationServicesStatus() {
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();
        Log.d("cccc", "cccc7");
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";
        Log.d("cccc", "cccc8");

//        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 5);
        mMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    //퍼미션 주석 처리
//    private boolean checkPermission() {
//        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION);
//        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
//                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
//            return true;
//        }
//        return false;
//    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        startLocationUpdates();
        Log.d("cccc", "cccc9");
//        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
//
//            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
//
//            boolean check_result = true;
//
//
//            // 모든 퍼미션을 허용했는지 체크합니다.
//
//            for (int result : grandResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    check_result = false;
//                    break;
//                }
//            }
//
//
//            if (check_result) {
//
//                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
//                startLocationUpdates();
//            } else {
//                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
//
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
//                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
//
//
//                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
//                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
//                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View view) {
//
//                            finish();
//                        }
//                    }).show();
//
//                } else {
//
//
//                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
//                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
//                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View view) {
//
//                            finish();
//                        }
//                    }).show();
//                }
//            }
//
//        }
    }


    /**
     * 퍼미션 처리 주석
     * */
    //여기부터는 GPS 활성화를 위한 메소드들
//    private void showDialogForLocationServiceSetting() {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("위치 서비스 비활성화");
//        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
//                + "위치 설정을 수정하실래요?");
//        builder.setCancelable(true);
//        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                Intent callGPSSettingIntent
//                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
//            }
//        });
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//            }
//        });
//        builder.create().show();
//    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case GPS_ENABLE_REQUEST_CODE:
//                //사용자가 GPS 활성 시켰는지 검사
//                if (checkLocationServicesStatus()) {
//                    if (checkLocationServicesStatus()) {
//                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
//                        needRequest = true;
//                        return;
//                    }
//                }
//                break;
//        }
//    }

    public void drawLine(GoogleMap map, LinkedList<Location> l)
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        Log.d("cccc", "cccc10");
        for(int i=0; i<l.size() ; i++)
        {
            polylineOptions.add(new LatLng(l.get(i).getLatitude(),l.get(i).getLongitude()));
        }
        Polyline polyline = map.addPolyline(polylineOptions);
    }
    //marker클릭시 생기는 이벤트 처리
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