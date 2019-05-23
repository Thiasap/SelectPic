package com.bistu747.selectpic;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.provider.Settings;

public class MainService extends Service {
    private static final String TAG = "MainService";

    //要引用的布局文件.
    ConstraintLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;

    ImageButton imageButton1;
    private boolean initViewPlace = false;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    private float lastParamsX,lastParamsY;
    SharedPreferences sharedPreferences;
    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG,"MainService Created");
        //OnCreate中来生成悬浮窗.
        if(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(MainService.this)){
            Toast.makeText(MainService.this,"无法创建悬浮窗，请检查权限",Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        sharedPreferences= getSharedPreferences("Config", Context.MODE_PRIVATE);
        createToucher();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        //Android8.0行为变更，对8.0进行适配https://developer.android.google.cn/about/versions/oreo/android-8.0-changes#o-apps

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.START | Gravity.TOP;

        params.x = (int)sharedPreferences.getFloat("lastParamsX",0);
        params.y = (int)sharedPreferences.getFloat("lastParamsY",0);
        params.alpha = (float)sharedPreferences.getInt("Transparency",0)/100;
        //设置悬浮窗口长宽数据.
        params.width = 140;
        params.height = 140;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.toucherlayout,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(MainService.this,MainActivity.class);
                    startActivity(intent);
            }
        });
        imageButton1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vb  = (Vibrator)MainService.this.getSystemService(Service.VIBRATOR_SERVICE);
                if(vb!=null)
                    vb.vibrate(60);
                stopSelf();
                System.exit(0);
                return false;
            }
        });
        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!initViewPlace) {
                            initViewPlace = true;
                            //获取初始位置
                            mTouchStartX += (event.getRawX() - params.x);
                            mTouchStartY += (event.getRawY() - params.y);
                        } else {
                            //根据上次手指离开的位置与此次点击的位置进行初始位置微调
                            mTouchStartX += (event.getRawX() - x);
                            mTouchStartY += (event.getRawY() - y);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取相对屏幕的坐标，以屏幕左上角为原点
                        x = event.getRawX();
                        y = event.getRawY();
                        updateViewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
    }
    private void updateViewPosition() {
        params.x = (int) (x - mTouchStartX);
        params.y = (int) (y - mTouchStartY);
        windowManager.updateViewLayout(toucherLayout, params);
    }
    @Override
    public void onDestroy() {
        if (imageButton1 != null) {
            windowManager.removeView(toucherLayout);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("lastParamsX",params.x);
            editor.putFloat("lastParamsY",params.y);
            editor.apply();
        }
        super.onDestroy();
    }
}
