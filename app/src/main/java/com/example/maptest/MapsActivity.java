package com.example.maptest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener, LocationSource {

    private static final int REQUEST_CODE_CHECK_GOOGLE_PLAY_SERVICES = 1000;
    private GoogleMap mMap;

    private static String[] mLocations = {
            "25.0336110,121.5650000",
            "40.0000350,119.7672800",
            "40.6892490,-74.0445000",
            "48.8582220,2.2945000"};
    private boolean mbIsZoomFirst = true;

    private Marker marker1,marker2,marker3,marker4;
    private Polyline polylineRoute;

    private static final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 1001;
    private LocationManager locationManager;
    private OnLocationChangedListener mLocationChangedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Spinner spnLocation = findViewById(R.id.spnLocation);
        spnLocation.setOnItemSelectedListener(spnLocationOnItemSelected);

        Spinner spnMapType = findViewById(R.id.spnMapType);
        spnMapType.setOnItemSelectedListener(spnMapTypeOnItemSelected);

        Button btn3DMap = findViewById(R.id.btn3DMap);
        btn3DMap.setOnClickListener(this);

        Button btnAddMarker = findViewById(R.id.btnAddMarker);
        btnAddMarker.setOnClickListener(this);

        Button btnRemoveMarker = findViewById(R.id.btnRemoveMarker);
        btnRemoveMarker.setOnClickListener(this);

        Button btnShowRoute = findViewById(R.id.btnShowRoute);
        btnShowRoute.setOnClickListener(this);

        Button btnHideRoute = findViewById(R.id.btnHideRoute);
        btnHideRoute.setOnClickListener(this);

        //建立SupportMapFragment，並設定Map的CallBack
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        supportMapFragment.getMapAsync(this);

        //把SupportMapFragment放到FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frameLayMap,supportMapFragment)
                .commit();

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        checkGooglePlayServices();


    }
    //*App 背景/前景作業;開始/停止定位功能
    @Override
    protected void onStart() {
        super.onStart();

        if(mMap != null)
            checkLocationPermissionAndEnableIt(true);
    }
    @Override
    protected void onStop() {
        super.onStop();

        checkLocationPermissionAndEnableIt(false);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkLocationPermissionAndEnableIt(true);

        mMap.setLocationSource(this);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if(ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,locationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(location != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
                    }else {
                        Toast.makeText(MapsActivity.this,"定位中",Toast.LENGTH_SHORT).show();
                    }

                }

                return false;
            }
        });


        //設定GoogelMap的Info Window(註解)
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window,null);
                TextView tetTitle = v.findViewById(R.id.txtTitle);
                tetTitle.setText(marker.getTitle());
                TextView txtSnippet = v.findViewById(R.id.txtSnippet);
                txtSnippet.setText(marker.getSnippet());
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });

        //建立Polyline  並且將它隱藏
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(10)
                .color(Color.RED);
        ArrayList<LatLng> listLatLng = new ArrayList<LatLng>();
        listLatLng.add(new LatLng(25.031,121.5650000));
        listLatLng.add(new LatLng(25.037,121.5650000));
        listLatLng.add(new LatLng(25.037,121.5600000));
        polylineOptions.addAll(listLatLng);
        polylineRoute = mMap.addPolyline(polylineOptions);
        polylineRoute.setVisible(false);

        //取得上一次定位資料
        if(ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
           == PackageManager.PERMISSION_GRANTED){
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(location != null){
                Toast.makeText(MapsActivity.this,"成功取得上一次定位",Toast.LENGTH_SHORT).show();
                onLocationChanged(location);
            }else
                Toast.makeText(MapsActivity.this,"沒有上一次定位資料",Toast.LENGTH_SHORT).show();
        }
    }


    //*手機定位權限檢查
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //檢查收到的權限要求編號是否和送出相同
        if(requestCode == REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && (grantResults[0]) == PackageManager.PERMISSION_GRANTED){
                // 再檢查一次，就會啟動定位
                checkLocationPermissionAndEnableIt(true);
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //負責取得定位的函式
    private void checkLocationPermissionAndEnableIt(boolean on){
        if(ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            //這項功能尚未取得使用者的同意
            //開始執行徵詢使用者的流程
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("提示");
                builder.setMessage("App需要啟動定位功能");
                builder.setCancelable(false);
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //使用者答覆後執行onRequestPermissionsResult
                        ActivityCompat.requestPermissions(MapsActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                    }
                        });
                builder.show();

                return;
            }else {
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);

                return;
            }
        }
        //根據 on (boolean) 參數的值，啟動或關閉定位
        if(on){
            mMap.setMyLocationEnabled(true);
            //如果GPS功能有開啟，優先使用GPS定位，else使用網路定位
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,this);
                Toast.makeText(MapsActivity.this,"使用GPS定位",Toast.LENGTH_SHORT).show();

            }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,5,this);
                    Toast.makeText(MapsActivity.this,"使用GPS定位",Toast.LENGTH_SHORT).show();

                }
        }else {
            locationManager.removeUpdates(this);
            Toast.makeText(MapsActivity.this,"定位功能已停用",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn3DMap:
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(mMap.getCameraPosition().target)
                                .tilt(60)
                                .zoom(18)
                                .build());
                mMap.animateCamera(cameraUpdate);
                break;

            case R.id.btnAddMarker:
                if(marker1 == null){
                    String[] sLocation = mLocations[0].split(",");
                    double dlat = Double.parseDouble(sLocation[0]);
                    double dlon = Double.parseDouble(sLocation[1]);
                    marker1 = mMap.addMarker(new MarkerOptions()
                            .title("台北101")
                            .snippet("全台灣最高")
                            .position(new LatLng(dlat,dlon))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
                            .anchor(0.5f,0.5f));
                }
                if(marker2 == null){
                    String[] sLocation = mLocations[1].split(",");
                    double dlat = Double.parseDouble(sLocation[0]);
                    double dlon = Double.parseDouble(sLocation[1]);
                    marker2 = mMap.addMarker(new MarkerOptions()
                            .title("中國長城")
                            .snippet("全長6000公里")
                            .position(new LatLng(dlat,dlon))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
                            .anchor(0.5f,0.5f));
                }
                if(marker3 == null){
                    String[] sLocation = mLocations[2].split(",");
                    double dlat = Double.parseDouble(sLocation[0]);
                    double dlon = Double.parseDouble(sLocation[1]);
                    marker3 = mMap.addMarker(new MarkerOptions()
                            .title("紐約自由女神")
                            .snippet("總高93公尺")
                            .position(new LatLng(dlat,dlon))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
                            .anchor(0.5f,0.5f));
                }
                if(marker4 == null){
                    String[] sLocation = mLocations[3].split(",");
                    double dlat = Double.parseDouble(sLocation[0]);
                    double dlon = Double.parseDouble(sLocation[1]);
                    marker4 = mMap.addMarker(new MarkerOptions()
                            .title("巴黎鐵塔")
                            .snippet("又稱艾菲爾鐵塔")
                            .position(new LatLng(dlat,dlon))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
                            .anchor(0.5f,0.5f));
                }break;

            case R.id.btnRemoveMarker:
                if(marker1 !=null){
                    marker1.remove();
                    marker1 = null;
                }
                if(marker2 !=null){
                    marker2.remove();
                    marker2 = null;
                }
                if(marker3 !=null){
                    marker3.remove();
                    marker3 = null;
                }
                if(marker4 !=null){
                    marker4.remove();
                    marker4 = null;
                }break;

            case R.id.btnShowRoute:
                polylineRoute.setVisible(true);
                break;

            case R.id.btnHideRoute:
                polylineRoute.setVisible(false);
                break;
        }
    }


    private AdapterView.OnItemSelectedListener spnLocationOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String[] sLocation = mLocations[position].split(",");
            double dlat = Double.parseDouble(sLocation[0]);
            double dlon = Double.parseDouble(sLocation[1]);
            if(mbIsZoomFirst){
                mbIsZoomFirst = false;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dlat,dlon),15));
            }else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(dlat,dlon)));
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener spnMapTypeOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //更改地圖模式
            switch (position){
                case 0 :
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1 :
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 2 :
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case 3 :
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onLocationChanged(Location location) {
        //把新的位置傳給 Google Map的my-location layer
        if(mLocationChangedListener != null)
            mLocationChangedListener.onLocationChanged(location);

        //地圖移動到新的位置
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(),location.getLongitude())));
    }

    @Override
    public void onStatusChanged(String provider, int i, Bundle extras) {
        //定位狀態監聽
        String str = provider;
        switch (i){
            case LocationProvider.OUT_OF_SERVICE:
                str += "定位功能無法使用";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                str += "暫時無法定位";
                break;//正在定位時會傳入這個值
        }
        Toast.makeText(MapsActivity.this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(MapsActivity.this,provider + "定位功能開啟",Toast.LENGTH_SHORT).show();
        checkLocationPermissionAndEnableIt(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MapsActivity.this,provider + "定位功能已經關閉",Toast.LENGTH_SHORT).show();
        checkLocationPermissionAndEnableIt(false);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationChangedListener = onLocationChangedListener;
        checkLocationPermissionAndEnableIt(true);
        Toast.makeText(MapsActivity.this,"地圖的my-location layer已經啟用",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void deactivate() {
        mLocationChangedListener = null;
        checkLocationPermissionAndEnableIt(false);
        Toast.makeText(MapsActivity.this,"地圖的my-location layer已被關閉",Toast.LENGTH_SHORT).show();
    }



    //*Googel Play Server檢查
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_CHECK_GOOGLE_PLAY_SERVICES:
                //如果使用者取消處理Google Play Services的問題，結束APP
                if (requestCode == RESULT_CANCELED)
                    showDlgGooglePlayServicesFailAndExitApp();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //錯誤的處理方法
    private void showDlgGooglePlayServicesFailAndExitApp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ERROR");
        builder.setMessage("找不到Google Play Services,程式無法執行");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setCancelable(false);
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });builder.show();
    }

    private void checkGooglePlayServices(){
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int resultCode = googleApi.isGooglePlayServicesAvailable(this);
        if(resultCode == ConnectionResult.SUCCESS)
            return;

        //是否使用者可以自行排除錯誤
        if(googleApi.isUserResolvableError(resultCode)){
            googleApi.showErrorDialogFragment(this,resultCode,REQUEST_CODE_CHECK_GOOGLE_PLAY_SERVICES);
            return;
        }
        //使用者無法處理錯誤
        showDlgGooglePlayServicesFailAndExitApp();
    }//...*

    //for mapButtonClickListener
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
