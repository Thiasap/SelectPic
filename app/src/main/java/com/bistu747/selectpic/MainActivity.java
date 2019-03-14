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

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.List;

import permison.FloatWindowManager;
import permison.PermissonUtil;
import permison.listener.PermissionListener;

public class MainActivity extends Activity {

    final private int MAX_PIC = 3;
    final private int MAX_SCALE = 4;
    private TextView FirstStart;
    private SharedPreferences sharedPreferences;
    private ViewPager mPager;
    private Uri[] ImgPaths = new Uri[MAX_PIC];
    private int lastPosition;
    PhotoView[] view = new PhotoView[3];
    private Intent serviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    void init(){
        sharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        mPager = (ViewPager) findViewById(R.id.pager);
        FirstStart = (TextView) findViewById(R.id.FirstStart);
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
        }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        load();
    }
    void load() {
        for (int i = 0; i < 3; i++) {
            String str = sharedPreferences.getString("Path" + i, null);
            if (str!=null) ImgPaths[i] = Uri.parse((String) str);
            else ImgPaths[i]=null;
            view[i]= new PhotoView(MainActivity.this);
            view[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Toast.makeText(MainActivity.this,"Long Click!",Toast.LENGTH_SHORT).show();
                    //openAlbum();
                    showListDialog();
                    return true;
                }
            });
        }
        lastPosition = sharedPreferences.getInt("lastPosition", 0);
        if(ImgPaths[lastPosition]!=null){
            FirstStart.setVisibility(View.INVISIBLE);
            mlog(ImgPaths[lastPosition].toString());
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
                view[position].enable();
                view[position].setScaleType(ImageView.ScaleType.FIT_CENTER);
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
                    mlog(ImgPaths[position].toString());
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
                this.getString(R.string.SharePic)};
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
                        startActivity(intent);
                        break;
                    case 1:
                        Matisse.from(MainActivity.this)
                                .choose(MimeType.ofImage(), false)//图片类型
                                .countable(false)//true:选中后显示数字;false:选中后显示对号
                                .maxSelectable(1)//可选的最大数
                                .capture(false)//选择照片时，是否显示拍照
                                //.captureStrategy(new CaptureStrategy(true, "com.example.xx.fileprovider"))//参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                                .thumbnailScale(0.87f)//缩略图的清晰程度(与内存占用有关)
                                .imageEngine(new GlideLoadEngine())//图片加载引擎
                                .forResult(1);//
                        break;
                    case 2:
                        //share
                        if (ImgPaths[lastPosition] == null){
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
            displayImage(path,lastPosition);
            mlog("path "+path);
        }
    }
    //自定义log
    private void mlog(String log){
        String TAG = "ViewDebug";
        Log.d(TAG,log);
    }
    //展示图片
    private void displayImage(Uri imagePath, int position) {
        mlog("Display image "+imagePath+" position is "+position);
        if(imagePath != null){
            if (imagePath!=ImgPaths[position]){
                SharedPreferences.Editor editor = sharedPreferences.edit();//存下地址
                editor.putString("Path"+position, imagePath.toString());
                editor.apply();
                ImgPaths[position] = imagePath;
            }
            mlog("Glide run ");
            Glide.with(MainActivity.this)
                    .load(imagePath)
                    .into(view[position]);
            FirstStart.setVisibility(View.INVISIBLE);
        }else if(ImgPaths[position]!=null){
            Toast.makeText(this, "Failed to load image(May be deleted)", Toast.LENGTH_SHORT).show();
        }
    }


    /*权限相关
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
    */

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
    protected void onStop(){
        super.onStop();
        startService(serviceIntent);
    }
}