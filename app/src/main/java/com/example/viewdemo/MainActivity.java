package com.example.viewdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import com.example.viewdemo.videos.VideoInfo;
import com.example.viewdemo.videos.VideoTextView;
import com.zhy.base.fileprovider.FileProvider7;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private BTTView view;
    private String url = "http://img2.imgtn.bdimg.com/it/u=3510436638,3204214416&fm=26&gp=0.jpg";
    private Button choosePic, takePhoto, drawBtn;
    private String mTempPhotoPath;
    private Uri imageUri;
    private Bitmap bitmap;
    private VideoTextView videoTextView;
    private static final String[] sLocalVideoColumns = {
            MediaStore.Video.Media._ID, // 视频id
            MediaStore.Video.Media.DATA, // 视频路径
            MediaStore.Video.Media.SIZE, // 视频字节大小
            MediaStore.Video.Media.DISPLAY_NAME, // 视频名称 xxx.mp4
            MediaStore.Video.Media.TITLE, // 视频标题
            MediaStore.Video.Media.DATE_ADDED, // 视频添加到MediaProvider的时间
            MediaStore.Video.Media.DATE_MODIFIED, // 上次修改时间，该列用于内部MediaScanner扫描，外部不要修改
            MediaStore.Video.Media.MIME_TYPE, // 视频类型 video/mp4
            MediaStore.Video.Media.DURATION, // 视频时长
            MediaStore.Video.Media.ARTIST, // 艺人名称
            MediaStore.Video.Media.ALBUM, // 艺人专辑名称
            MediaStore.Video.Media.RESOLUTION, // 视频分辨率 X x Y格式
            MediaStore.Video.Media.DESCRIPTION, // 视频描述
            MediaStore.Video.Media.IS_PRIVATE,
            MediaStore.Video.Media.TAGS,
            MediaStore.Video.Media.CATEGORY, // YouTube类别
            MediaStore.Video.Media.LANGUAGE, // 视频使用语言
            MediaStore.Video.Media.LATITUDE, // 拍下该视频时的纬度
            MediaStore.Video.Media.LONGITUDE, // 拍下该视频时的经度
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
    };
    private static final String[] sLocalVideoThumbnailColumns = {
            MediaStore.Video.Thumbnails.DATA, // 视频缩略图路径
            MediaStore.Video.Thumbnails.VIDEO_ID, // 视频id
            MediaStore.Video.Thumbnails.KIND,
            MediaStore.Video.Thumbnails.WIDTH, // 视频缩略图宽度
            MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
    };
    private List<VideoInfo> mVideoInfos;


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
        view.setListener(new BaseSurfaceView.LifecycleListener() {
            @Override
            public void onCreate() {

            }

            @Override
            public void onChanged() {

            }

            @Override
            public void onDestroy() {

            }
        });
        choosePic = findViewById(R.id.choose_pic);
        takePhoto = findViewById(R.id.take_photo);
        drawBtn = findViewById(R.id.draw);
        choosePic.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        drawBtn.setOnClickListener(this);
        mVideoInfos = new ArrayList<>();
        videoTextView = findViewById(R.id.video);
        new Thread(() -> {
            view.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gm));
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    VideoInfo videoInfo = new VideoInfo();

                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                    long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                    String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));
                    String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                    String description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION));
                    int isPrivate = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE));
                    String tags = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS));
                    String category = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                    int dateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
                    int miniThumbMagic = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC));
                    String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                    String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    int bookmark = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK));

                    Cursor thumbnailCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                            MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                    if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                        do {
                            String thumbnailData = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                            int kind = thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND));
                            long width = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH));
                            long height = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT));

                            videoInfo.thumbnailData = thumbnailData;
                            videoInfo.kind = kind;
                            videoInfo.width = width;
                            videoInfo.height = height;
                        } while (thumbnailCursor.moveToNext());

                        thumbnailCursor.close();
                    }

                    videoInfo.id = id;
                    videoInfo.data = data;
                    videoInfo.size = size;
                    videoInfo.displayName = displayName;
                    videoInfo.title = title;
                    videoInfo.dateAdded = dateAdded;
                    videoInfo.dateModified = dateModified;
                    videoInfo.mimeType = mimeType;
                    videoInfo.duration = duration;
                    videoInfo.artist = artist;
                    videoInfo.album = album;
                    videoInfo.resolution = resolution;
                    videoInfo.description = description;
                    videoInfo.isPrivate = isPrivate;
                    videoInfo.tags = tags;
                    videoInfo.category = category;
                    videoInfo.latitude = latitude;
                    videoInfo.longitude = longitude;
                    videoInfo.dateTaken = dateTaken;
                    videoInfo.miniThumbMagic = miniThumbMagic;
                    videoInfo.bucketId = bucketId;
                    videoInfo.bucketDisplayName = bucketDisplayName;
                    videoInfo.bookmark = bookmark;
                    mVideoInfos.add(videoInfo);
                    Log.e("wwh", "MainActivity --> onCreate: " + videoInfo.toString());
                } while (cursor.moveToNext());
                videoTextView.setFile(mVideoInfos.get(0).data);
                cursor.close();
            }
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
                if (videoTextView.isDecoreDone()) {
                    videoTextView.start();
                } else {
                    Toast.makeText(getApplicationContext(), "正在加载", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
