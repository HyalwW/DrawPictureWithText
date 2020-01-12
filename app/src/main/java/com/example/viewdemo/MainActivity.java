package com.example.viewdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.INTERNET};
    private BTTView view;
    private String url = "http://img2.imgtn.bdimg.com/it/u=3510436638,3204214416&fm=26&gp=0.jpg";


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
        view.setOnClickListener(v -> view.draw());
//        view.setOnLongClickListener(v -> {
//            view.load(url);
//            return true;
//        });
    }
}
