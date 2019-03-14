package com.bistu747.selectpic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import permison.FloatWindowManager;

public class MySettings extends Activity {
    private SharedPreferences sharedPreferences;
    private RadioGroup RG_SuspensionW;
    private Button SaveAll,ImgPreview;
    private ImageView ImgPre;
    private EditText ET_Transparency;
    private String SuspensionW;
    RadioButton RB_Empty;
    RadioButton RB_OpenApp;
    RadioButton RB_OpenPic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        RG_SuspensionW.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.RB_Empty:
                        SuspensionW = "";
                        break;
                    case R.id.RB_OpenApp:
                        SuspensionW = "OpenApp";
                        break;
                    case R.id.RB_OpenPic:
                        Toast.makeText(MySettings.this, "功能正在建设中！", Toast.LENGTH_SHORT).show();
                        RB_OpenApp.setChecked(true);
                        //SuspensionW = "OpenPic";
                        break;
                    default:
                        break;
                }
            }
        });
        ImgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float Transparency = Float.parseFloat(ET_Transparency.getText().toString());
                if(Transparency>100) Transparency = 100;
                if(Transparency<0) Transparency = 0;
                ImgPre.setAlpha(Transparency/100);
            }
        });
        SaveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Transparency",Integer.parseInt(ET_Transparency.getText().toString()));
                editor.putString("SuspensionW",SuspensionW);
                if(SuspensionW.equals("")){
                    editor.putString("BackRun","false");
                }else{
                    editor.putString("BackRun","true");
                    FloatWindowManager.getInstance().applyOrShowFloatWindow(MySettings.this);
                    Intent serviceIntent = new Intent(MySettings.this,MainService.class);
                    startService(serviceIntent);
                }
                editor.apply();
            }
        });
    }
    private void init(){
        sharedPreferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        RG_SuspensionW = (RadioGroup)findViewById(R.id.RG_SpW);
        SaveAll = (Button)findViewById(R.id.SaveAll);
        ImgPreview = (Button)findViewById(R.id.PreviewTrans);
        ImgPre = (ImageView)findViewById(R.id.ImgPreview);
        ET_Transparency = (EditText)findViewById(R.id.Transparency);
        SuspensionW = sharedPreferences.getString("SuspensionW","");
        RB_Empty = findViewById(R.id.RB_Empty);
        RB_OpenApp = findViewById(R.id.RB_OpenApp);
        RB_OpenPic = findViewById(R.id.RB_OpenPic);
        int Transparency = sharedPreferences.getInt("Transparency",50);
        ET_Transparency.setText(String.valueOf(Transparency));
        Log.e(" ","Transparency"+Transparency);
        ImgPre.setAlpha(((float)Transparency)/100);
        switch (SuspensionW){
            case "":
                RB_Empty.setChecked(true);
                break;
            case "OpenApp":
                RB_OpenApp.setChecked(true);
                break;
            case "OpenPic":
                RB_OpenPic.setChecked(true);
                break;
            default:
                break;
        }
    }
}
