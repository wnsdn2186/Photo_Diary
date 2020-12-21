package com.example.moment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout refreshLayout;
    String loc;
    private int check = 0;
    double latitude;
    double longitude;
    String new_pass;
    String new_pass2;
    // 이미지 불러올 때 필요한 resource
    String[] imgList;
    File imgf[];
    BitmapFactory.Options options;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        File dummypath;
        File[] dummydir = getExternalFilesDirs("Pictures");
        dummypath = dummydir[0];
        File dummyfile = new File(dummypath, "dummy.txt");
        try{
            FileOutputStream dummy_fos = new FileOutputStream(dummyfile, true);
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent from = getIntent();
        loc = from.getStringExtra("주소");
        longitude = from.getDoubleExtra("경도", 0);
        latitude = from.getDoubleExtra("위도", 0);

        options = new BitmapFactory.Options();
        String path = "/sdcard/Android/data/com.example.moment/files/Pictures";
        File list = new File(path);
        imgList = list.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String s) {
                Boolean bOK = false;
                if (s.toLowerCase().endsWith(".png") | s.toLowerCase().endsWith(".jpg"))
                    bOK = true;

                return bOK;
            }
        });
        imgf = list.listFiles();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        GridView gridView = (GridView) findViewById(R.id.GridView01);
        if (imgList.length != 0) {
            gridView.setAdapter(new ImageAdapter(this));
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(getApplicationContext(), newImage.class);
                String path = imgf[imgList.length - position].getAbsolutePath();
                it.putExtra("file_path", path);
                it.putExtra("file_name", imgList[imgList.length - position - 1]);
                startActivity(it);
            }
        });

        refreshLayout.setDistanceToTriggerSync(400);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "촬영 모드", Toast.LENGTH_SHORT).show();
                Intent camera = new Intent(MainActivity.this, CameraRecord.class);
                camera.putExtra("주소", loc);
                camera.putExtra("경도", longitude);
                camera.putExtra("위도", latitude);
                camera.putExtra("디렉토리", new_pass);
                camera.putExtra("문서", new_pass2);
                startActivity(camera);

                refreshLayout.setRefreshing(false);
            }
        });

    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }
        @Override
        public int getCount() {
            return imgList.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            Log.d("Adapter", "pos: " + position);
            if(convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 440));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setTag("sample_" + (position + 1));
            }
            else {
                imageView = (ImageView)convertView;
            }
            options.inSampleSize = 16;
            Bitmap bm = BitmapFactory.decodeFile(imgf[imgList.length - position].getAbsolutePath(), options);
            ExifInterface exif = null;
            try{
                exif = new ExifInterface(imgf[imgList.length - position].getAbsolutePath());
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
            imageView.setImageBitmap(rotate(bm, exifDegree));
            return imageView;
        }
    }

    public void ShowMap(View view){
        Intent intent = new Intent(this, SubActivity.class);
        intent.putExtra("lat", latitude);
        intent.putExtra("lng", longitude);
        startActivity(intent);
    }

    public void Setting(View view) {
        Toast.makeText(this, "Setting Btn",
                Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onRestart() {
        super.onRestart();

        setContentView(R.layout.activity_main);
        options = new BitmapFactory.Options();
        String path = "/sdcard/Android/data/com.example.moment/files/Pictures";
        File list = new File(path);
        imgList = list.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String s) {
                Boolean bOK = false;
                if (s.toLowerCase().endsWith(".png") | s.toLowerCase().endsWith(".jpg"))
                    bOK = true;

                return bOK;
            }
        });
        imgf = list.listFiles();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        GridView gridView = (GridView)findViewById(R.id.GridView01);
        gridView.setAdapter(new ImageAdapter(this));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(getApplicationContext(), newImage.class);
                String path = imgf[imgList.length - position].getAbsolutePath();
                it.putExtra("file_name", imgList[imgList.length - position - 1]);
                it.putExtra("file_path", path);
                it.putExtra("lat", latitude);
                it.putExtra("lng", longitude);
                startActivity(it);
            }
        });


        refreshLayout.setDistanceToTriggerSync(400);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                Toast.makeText(MainActivity.this, "촬영 모드", Toast.LENGTH_SHORT).show();
                Intent camera = new Intent(MainActivity.this, CameraRecord.class);
                camera.putExtra("주소", loc);
                camera.putExtra("경도", longitude);
                camera.putExtra("위도", latitude);
                startActivity(camera);

                refreshLayout.setRefreshing(false);
            }
        });
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
}