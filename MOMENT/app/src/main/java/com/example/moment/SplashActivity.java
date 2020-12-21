package com.example.moment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;

public class SplashActivity extends Activity
{
    private int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    private int READ_EXTERN_STORAGE_PERMISSON=1;
    LocationManager locationManager;
    LocationListener locationListener;
    double latitude; //위치 정보, 위도
    double longitude; //위치 정보, 경도
    List<Address> address = null; //위도와 경도로 얻은 주소
    String loc;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_LOCATION);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            public void onLocationChanged(Location location)
            {
                Geocoder geocoder = new Geocoder(SplashActivity.this);
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                try
                {
                    address = geocoder.getFromLocation(latitude,longitude,10);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                if(address != null)
                {
                    if(address.size() == 0)
                        loc = "위치 정보를 받지 못했습니다.";
                    else
                    {
                        loc = address.get(0).getAddressLine(0);
                        Log.i("주소", "주소" + loc);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("경도", longitude);
                        intent.putExtra("위도", latitude);
                        intent.putExtra("주소", loc);
                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "First enable Location Access in settings.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "First enable Read Access in settings.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "First enable Write Access in settings.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
}
