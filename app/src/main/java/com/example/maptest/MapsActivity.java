package com.example.maptest;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_CHECK_GOOGLE_PLAY_SERVICES = 1000;
    private GoogleMap mMap;

    private static String[] mLocations = {
            "25.0336110,121.5650000",
            "40.0000350,119.7672800",
            "40.6892490,-74.0445000",
            "48.8582220,2.2945000"};
    private boolean mbIsZoomFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Spinner spnLocation = findViewById(R.id.spnLocation);
        spnLocation.setOnItemSelectedListener(spnLocationOnItemSelected);

        Spinner spnMapType = findViewById(R.id.spnMapType);
        spnMapType.setOnItemSelectedListener(spnMapTypeOnItemSelected);

        Button btn3DMap = findViewById(R.id.btn3DMap);
        btn3DMap.setOnClickListener(btn3DMapOnClick);

        //建立SupportMapFragment，並設定Map的CallBack
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        supportMapFragment.getMapAsync(this);

        //把SupportMapFragment放到FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frameLayMap,supportMapFragment)
                .commit();

        checkGooglePlayServices();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

    private View.OnClickListener btn3DMapOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .tilt(60)
                    .zoom(18)
                    .build());
            mMap.animateCamera(cameraUpdate);
        }
    };

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
    }
}
