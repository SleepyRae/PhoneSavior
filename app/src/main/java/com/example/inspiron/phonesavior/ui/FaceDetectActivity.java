package com.example.inspiron.phonesavior.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Service.AppLimitService;
import com.example.inspiron.phonesavior.Service.CameraService;

import java.util.ArrayList;

public class FaceDetectActivity extends AppCompatActivity {
    private boolean ServiceIsOn = false;
    private Switch aSwitch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);
        aSwitch = (Switch) findViewById(R.id.s_v);

        aSwitch.setChecked(false);

        aSwitch.setSwitchTextAppearance(FaceDetectActivity.this,R.style.s_false);

        ServiceIsOn = isRunning("com.example.inspiron.phonesavior.Service.CameraService");
        if(ServiceIsOn){
            aSwitch.setChecked(true);
        }else{
            aSwitch.setChecked(false);
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean f) {
                //控制开关字体颜色
                Intent face = new Intent(FaceDetectActivity.this, CameraService.class);
                if (f) {
                    aSwitch.setSwitchTextAppearance(FaceDetectActivity.this, R.style.s_true);
                    /*Intent intent = new Intent(FaceDetectActivity.this, CameraActivity.class);
                    startActivity(intent);*/
                    startService(face);
                    ServiceIsOn = true;
                    Toast.makeText(getApplicationContext(), "开启人脸检测啦", Toast.LENGTH_SHORT).show();
                } else {
                    aSwitch.setSwitchTextAppearance(FaceDetectActivity.this, R.style.s_false);
                    stopService(face);
                    ServiceIsOn = false;
                  //  CameraActivity.finishActivity();
                    Toast.makeText(getApplicationContext(), "关掉人脸检测啦", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //判断service是否运行
    public boolean isRunning(String name){
        android.app.ActivityManager myManager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //获取所有正在运行的service
        ArrayList<android.app.ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(name)) {
                return true;
            }
        }
        return false;
    }

}
