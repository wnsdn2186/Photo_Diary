package com.example.moment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraRecord extends Activity
{
    TextView photo_date;
    TextView photo_addr;
    EditText photo_diary;
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat fNow = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    String formatDate = fNow.format(date);
    String loc;
    double latitude; //위치 정보, 위도
    double longitude; //위치 정보, 경도
    InputMethodManager imm;
    String strcontent;//일기에 쓴 내용
    String finalstr;//저장할 text합친거
    String strdate;//저장할 날짜
    private String imageFilePath;
    private Uri photoUri;
    static final int REQUEST_IMAGE_CAPTURE=672;
    File image;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        Intent from = getIntent();
        loc = from.getStringExtra("주소");
        longitude = from.getDoubleExtra("경도", 0);
        latitude = from.getDoubleExtra("위도", 0);
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

      sendTakePhotoIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        setContentView(R.layout.cameraresult);

        ((ImageView)findViewById(R.id.photo)).setImageURI(photoUri);//image1
        photo_date = (TextView)findViewById(R.id.photo_date);//dateNow
        photo_addr = (TextView)findViewById(R.id.photo_addr);//status2
        photo_diary = (EditText)findViewById(R.id.photo_diary);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;
            try{
                exif = new ExifInterface(imageFilePath);
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

            ((ImageView)findViewById(R.id.photo)).setImageBitmap(rotate(bitmap,exifDegree));
            photo_date.setText(formatDate);
            photo_addr.setText(loc);
        }
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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = new File(storageDir, imageFileName + ".jpg");
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSave(View view)
    {
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "SDcard is not mounted", Toast.LENGTH_SHORT).show();
            return;
        }

        strcontent = photo_diary.getText().toString();//일기 내용
        strdate = photo_date.getText().toString();//날짜
        finalstr = strdate + "\n" + Double.toString(latitude) +" "+ Double.toString(longitude) +"\n"+ strcontent + "\n";//날짜+경도,위도+일기내용
        photo_diary.setText("");

        File addrpath;
        File[] addrdir = getExternalFilesDirs("Path");
        addrpath = addrdir[0];

        File path;
        File[] dirs = getExternalFilesDirs(strdate);//폴더생성(날짜로)
        path = dirs[0];

        File file = new File(path, "Data.txt");
        File pathfile = new File(addrpath, "Path.txt");

        try{
            FileOutputStream fos = new FileOutputStream(file, true);
            FileOutputStream addrfos = new FileOutputStream(pathfile, true);

            PrintWriter writer = new PrintWriter(fos);//날짜 + 경도,위도 + 내용
            writer.println(finalstr);

            PrintWriter addrwriter = new PrintWriter(addrfos);//파일이름(날짜) 저장
            addrwriter.println(strdate);

            writer.flush();
            writer.close();
            addrwriter.flush();
            addrwriter.close();
            Toast.makeText(this,"SAVED",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        finish();
    }
    public void onClick(View view)
    {
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}