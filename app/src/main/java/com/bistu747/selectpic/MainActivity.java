package com.bistu747.selectpic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.widget.AppCompatImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import com.bistu747.selectpic.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import permison.FloatWindowManager;
import permison.PermissonUtil;
import permison.listener.PermissionListener;

public class MainActivity extends AppCompatActivity {

    final private int MAX_PIC = 3;
    final private int MAX_SCALE = 4;
    private TextView FirstStart;
    private SharedPreferences sharedPreferences;
    private ViewPager mPager;
    private String[] ImgPaths = new String[MAX_PIC];
    private int lastPosition;
    PhotoView[] view = new PhotoView[3];
    private Intent serviceIntent;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    void init(){
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        mPager = findViewById(R.id.pager);
        FirstStart = findViewById(R.id.FirstStart);
        if(sharedPreferences.getString("BackRun","").equals("true")) {
            FloatWindowManager.getInstance().applyOrShowFloatWindow(MainActivity.this);
            serviceIntent = new Intent(MainActivity.this,MainService.class);
            startService(serviceIntent);
        }
        PermissonUtil.checkPermission(MainActivity.this, new PermissionListener() {
            @Override
            public void havePermission() {
            }
            @Override
            public void requestPermissionFail() {
                Toast.makeText(MainActivity.this, "权限获取失败，程序可能无法使用", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        load();
    }
    void load() {
        for (int i = 0; i < 3; i++) {
            String str = sharedPreferences.getString("Path" + i, null);
            ImgPaths[i] = str;
            view[i]= new PhotoView(MainActivity.this);
            view[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showListDialog();
                    return true;
                }
            });
        }
        lastPosition = sharedPreferences.getInt("lastPosition", 0);
        if(ImgPaths[lastPosition]!=null){
            FirstStart.setVisibility(View.INVISIBLE);
            mlog(ImgPaths[lastPosition]);
        }
        else FirstStart.setVisibility(View.VISIBLE);
        mlog("last position is :"+lastPosition);
        mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return ImgPaths.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                mlog( "now create position " + position);
                if (sharedPreferences.getBoolean("zoom", true)){
                    view[position].enableRotate();
                    view[position].enable();
                }
                else{
                    view[position].disableRotate();
                    view[position].disable();
                }
                view[position].setScaleType(AppCompatImageView.ScaleType.FIT_CENTER);
                displayImage(ImgPaths[position],position);
                view[position].setMaxScale(MAX_SCALE);
                container.addView(view[position]);
                return view[position];
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        mPager.setCurrentItem(lastPosition, false);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                lastPosition = position;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("lastPosition", position);
                mlog("save position "+position);
                editor.apply();
                if(ImgPaths[position]!=null){
                    FirstStart.setVisibility(View.INVISIBLE);
                    mlog(ImgPaths[position]);
                }
                else FirstStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
    }
    //dialog
    private void showListDialog() {
        final String[] items = { this.getString(R.string.Settings),
                this.getString(R.string.ChangePic),
                this.getString(R.string.SharePic),
                this.getString(R.string.DeletePic)};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        //listDialog.setTitle("选择...");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                // ...To-do
                switch (which){
                    case 0:
                        Intent intent = new Intent(MainActivity.this, MySettings.class);
                        //Intent intent = new Intent(MainActivity.this, mSettings.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Matisse.from(MainActivity.this)
                                .choose(MimeType.ofImage(), false)//图片类型
                                .countable(false)//true:选中后显示数字;false:选中后显示对号
                                .maxSelectable(1)//可选的最大数
                                .capture(false)//选择照片时，是否显示拍照
                                .thumbnailScale(0.87f)//缩略图的清晰程度(与内存占用有关)
                                .forResult(1);
                        break;
                    case 2:
                        //share
                        if(ImgPaths[lastPosition] == null){
                            Toast.makeText(MainActivity.this,
                                    "当前没有图片可分享！",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        // 指定发送的内容 (EXTRA_STREAM 对于文件 Uri )
                        shareIntent.putExtra(Intent.EXTRA_STREAM, ImgPaths[lastPosition]);
                        shareIntent.setType("image/jpeg");
                        startActivity(Intent.createChooser(shareIntent,"分享图片到..."));
                        break;
                    case 3:
                        context.deleteFile(lastPosition+".jpg");
                        SharedPreferences.Editor editor = sharedPreferences.edit();//存下地址
                        editor.remove("Path"+lastPosition);
                        editor.apply();
                        break;
                    default:
                        break;
                }
            }
        });
        // 将对话框的大小按屏幕大小的百分比设置
        AlertDialog dialog = listDialog.create();
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.gravity = Gravity.CENTER;
        lp.width =  (int)(dm.widthPixels*0.7);//宽高可设置具体大小
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.dialog);
    }

    //选择图片回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= 23&&!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, MainService.class));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<Uri> result = Matisse.obtainResult(data);
            Uri path = result.get(0);
            String FPath = context.getFilesDir().getPath()+"/"+lastPosition+".jpg";
            copyFile(path,lastPosition+".jpg");
            displayImage(FPath,lastPosition);
            mlog("uri path "+path+"\n path = "+FPath);
        }
    }
    //自定义log
    private void mlog(String log){
        String TAG = "ViewDebug";
        Log.d(TAG,log);
    }
    //展示图片
    //private void displayImage(String imagePath, int position) {
    private void displayImage(String imagePath, int position) {
        mlog("Display image "+imagePath+" position is "+position);
        if(imagePath != null){
            if (!imagePath.equals(ImgPaths[position])){
                SharedPreferences.Editor editor = sharedPreferences.edit();//存下地址
                editor.putString("Path"+position, imagePath);
                editor.apply();
                ImgPaths[position] = imagePath;
            }
            mlog("Glide run ");
            byte[] imgBy = readPic(position+".jpg");
            Glide.with(MainActivity.this)
                    .load(imgBy)
                    .into(view[position]);
            FirstStart.setVisibility(View.INVISIBLE);
        }else if(ImgPaths[position]!=null){
            Toast.makeText(this, "Failed to load image(May be deleted)", Toast.LENGTH_SHORT).show();
        }
    }
    public byte[] readPic(String fileName){
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            if (inputStream == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            inputStream.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "".getBytes();
    }
    public int copyStream(FileInputStream input, FileOutputStream output) throws Exception {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }
    public void copyFile(Uri imagePath, String dstFile){
        try {
            FileInputStream inputStream = (FileInputStream)context.getContentResolver().openInputStream(imagePath);
            if (inputStream == null) return;
            FileOutputStream outputStream = context.openFileOutput(dstFile,Context.MODE_PRIVATE);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(sharedPreferences.getBoolean("wakeup",false)){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.i("MainActivity","wake up!");
        }
        if(sharedPreferences.getBoolean("zoom",true)){
            for(int i=0;i<3;i++){
                view[i].enable();
                view[i].enableRotate();
            }
        }else{
            for(int i=0;i<3;i++){
                view[i].disable();
                view[i].disableRotate();
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (serviceIntent != null) {
            startService(serviceIntent);
        }
    }
}