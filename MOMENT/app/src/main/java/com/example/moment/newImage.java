package com.example.moment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class newImage extends AppCompatActivity {
    String t_path = "/sdcard/Android/data/com.example.moment/files/";
    String line = null; // 한줄씩 읽기
    String strdiary;//수정된 내용
    EditText tv_text;//내용
    String fname;//파일 이름
    String f_tag;//파일 경로
    String strdate;//날짜
    String strloc;//주소
    double lat;
    double lng;
    TextView dateText;
    double info_lat;
    double info_lang;
    List<Address> address = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desc);

        Intent it = getIntent();
        f_tag = it.getStringExtra("file_path");
        fname = it.getStringExtra("file_name");
        lat = it.getDoubleExtra("lat", 0);
        lng = it.getDoubleExtra("lng", 0);

        Toast.makeText(this, f_tag, Toast.LENGTH_SHORT).show();
        File img_file = new File(f_tag);

        Bitmap bm = BitmapFactory.decodeFile(img_file.getAbsolutePath());
        ExifInterface exif= null;
        try{
            exif = new ExifInterface(f_tag);
        }catch (IOException e){
            e.printStackTrace();
        }

        int exifOrientation;
        int exifDegree;

        if(exif != null){
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        }else{
            exifDegree = 0;
        }
        ImageView iv_picture = (ImageView)findViewById(R.id.picture);
        iv_picture.setImageBitmap(rotate(bm, exifDegree));


        // text file
        String temp = fname.replaceAll(".jpg", "");
        String text_p = t_path + temp + "/Data.txt";
        File text_file = new File(text_p);
        int count = 0;
        StringBuffer sb = new StringBuffer();
        TextView tv_date;
        TextView tv_location;

        try {
            BufferedReader buf = new BufferedReader(new FileReader(text_file));
            while((line=buf.readLine())!=null){
                if(count == 0) {
                    tv_date = (TextView)findViewById(R.id.date);
                    tv_date.setText("날짜 : " + line);
                    count++;
                } else if(count == 1) {
                    Geocoder geocoder = new Geocoder(newImage.this);
                    String[] info_loc = line.split(" ");
                    info_lat = Double.parseDouble(info_loc[0]);
                    info_lang = Double.parseDouble(info_loc[1]);
                    address = geocoder.getFromLocation(info_lat,info_lang,10);
                    strloc = address.get(0).getAddressLine(0);
                    tv_location = (TextView)findViewById(R.id.loc);
                    tv_location.setText("주소 : " + strloc);
                    count++;
                } else {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv_text = (EditText)findViewById(R.id.content);
        tv_text.setText(sb);
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void onTouch(View view){
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onText(View view){
        strdiary = tv_text.getText().toString();//일기 내용
        dateText = (TextView)findViewById(R.id.date);//날짜
        strdate = dateText.getText().toString();//날짜 문자열로
        String[] strdatearr = strdate.split(" ");
        strdiary = strdatearr[2] + "\n" + Double.toString(lat) + " " + Double.toString(lng) + "\n" + strdiary + "\n";
        File path;
        File[] dirs = getExternalFilesDirs(strdatearr[2]);

        path = dirs[0];

        File file = new File(path, "Data.txt");
        if(file.exists())
            file.delete();
        try{
            FileOutputStream fos = new FileOutputStream(file, true);
            PrintWriter writer = new PrintWriter(fos);//날짜 + 경도,위도 + 내용
            writer.println(strdiary);
            writer.flush();
            writer.close();
            Toast.makeText(this,"SAVED",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        finish();
    }
}