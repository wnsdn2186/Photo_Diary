package com.example.moment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SubActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    String t_path = "/sdcard/Android/data/com.example.moment/files/Path/Path.txt";
    String f_path = "/sdcard/Android/data/com.example.moment/files/";
    String line = null;
    String infO_line = null;
    String info_path;
    double info_lat;
    double info_lang;
    LatLng new_image;
    LatLng current;
    String[] array;
    int cnt;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Intent intent = getIntent();
        longitude = intent.getDoubleExtra("lng", 0);
        latitude = intent.getDoubleExtra("lat", 0);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        current = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

        try {
            BufferedReader buf = new BufferedReader(new FileReader(t_path));
            while((line=buf.readLine())!=null){
                array = line.split("\\n");
                info_path = f_path + array[0] + "/Data.txt";
                try {
                    BufferedReader info_buf = new BufferedReader(new FileReader(info_path));
                    cnt = 1;
                    while((infO_line=info_buf.readLine())!=null){
                        if(cnt == 2)
                        {
                            String[] info_loc = infO_line.split(" ");
                            info_lat = Double.parseDouble(info_loc[0]);
                            info_lang = Double.parseDouble(info_loc[1]);
                            new_image = new LatLng(info_lat, info_lang);
                            MarkerOptions makerOptions = new MarkerOptions();
                            makerOptions
                                    .position(new_image)
                                    .title(array[0])
                                    .alpha(0.5f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                            mMap.addMarker(makerOptions);

                        }
                        cnt = cnt + 1;
                    }
                    info_buf.close();
                } catch (
                        FileNotFoundException e) {
                    e.printStackTrace();
                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }
            buf.close();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        try{
            mMap.setOnMarkerClickListener(this);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        Intent intent = new Intent(SubActivity.this,newImage.class);
        intent.putExtra("file_path", f_path + "Pictures/" +  marker.getTitle()+".jpg");
        intent.putExtra("file_name",  marker.getTitle()+".jpg");
        startActivity(intent);
        return false;
    }
}