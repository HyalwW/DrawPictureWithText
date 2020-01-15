package com.example.viewdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zhy.base.fileprovider.FileProvider7;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private BTTView view;
    private String url = "http://img2.imgtn.bdimg.com/it/u=3510436638,3204214416&fm=26&gp=0.jpg";
    private Button choosePic, takePhoto, drawBtn, go2video;
    private String mTempPhotoPath;
    private Uri imageUri;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (String permission : BASIC_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, BASIC_PERMISSIONS, 111);
                break;
            }
        }
        view = findViewById(R.id.btt_view);
        choosePic = findViewById(R.id.choose_pic);
        takePhoto = findViewById(R.id.take_photo);
        drawBtn = findViewById(R.id.draw);
        choosePic.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        drawBtn.setOnClickListener(this);
        go2video = findViewById(R.id.video);
        go2video.setOnClickListener(this);
        new Thread(() -> {
            view.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gm));
        }).start();
    }

    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, 1);
    }

    private void takePhoto() {
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "photoTest" + File.separator);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File photoFile = new File(fileDir, "photo" + System.currentTimeMillis() + ".jpeg");
        mTempPhotoPath = photoFile.getAbsolutePath();
        imageUri = FileProvider7.getUriForFile(this, photoFile);
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intentToTakePhoto, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    String filePath = FileUtil.getFilePathByUri(this, uri);
                    bitmap = BitmapFactory.decodeFile(filePath);
                    Log.e("wwh", "MainActivity-->onActivityResult(): ");
                    view.setBitmap(bitmap);
                } else {
                    toast("选择的图片不存在");
                }
                break;
            case 2:
                if (!TextUtils.isEmpty(mTempPhotoPath)) {
                    bitmap = BitmapFactory.decodeFile(mTempPhotoPath);
                    if (bitmap != null) {
                        view.setBitmap(bitmap);
                    } else {
                        toast("拍照失败2");
                    }
                } else {
                    toast("拍照失败1");
                }
                break;
        }
    }

    private void toast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_pic:
                choosePhoto();
                break;
            case R.id.take_photo:
                takePhoto();
                break;
            case R.id.draw:
                view.draw();
                break;
            case R.id.video:
                startActivity(new Intent(this, VideoActivity.class));
                break;
        }
    }
}
