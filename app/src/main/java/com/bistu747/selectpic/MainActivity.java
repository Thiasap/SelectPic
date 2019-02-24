package com.bistu747.selectpic;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bm.library.PhotoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private PhotoView photoView;
    //获取sharedPreferences对象
    private SharedPreferences sharedPreferences;
    private String imgPath;
    private TextView FirstStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


    }
    void init() {
        //获取sharedPreferences对象
        sharedPreferences = getSharedPreferences("ImgPath", Context.MODE_PRIVATE);
        FirstStart = (TextView) findViewById(R.id.FirstText);
        photoView = (PhotoView) findViewById(R.id.Pic);
        checkPermissions(MainActivity.this);
        photoView.enable();// 启用图片缩放功能
        photoView.setMaxScale(4);
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openAlbum();
                return true;
            }
        });
        load();
    }
    void load(){
        imgPath = sharedPreferences.getString("Path", null);
        if(fileIsExists(imgPath)){
            //FirstStart.setVisibility(View.INVISIBLE);
            displayImage(imgPath);
        }else {
            FirstStart.setVisibility(View.VISIBLE);
        }

    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static void checkPermissions(Activity activity) {
        try {
            //检测是否有读的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1); // 打开相册
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            if(!(imagePath.equals(imgPath))){
                SharedPreferences.Editor editor = sharedPreferences.edit();//存下地址
                //Log.e("imgPath","Saving");
                editor.putString("Path", imagePath);
                editor.apply();
            }
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            photoView.setImageBitmap(bitmap);
            //picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to load image", Toast.LENGTH_SHORT).show();
        }
        FirstStart.setVisibility(View.INVISIBLE);
    }
}