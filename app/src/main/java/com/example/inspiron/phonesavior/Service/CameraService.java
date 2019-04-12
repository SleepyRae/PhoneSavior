package com.example.inspiron.phonesavior.Service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Statistics.AppInformation;
import com.example.inspiron.phonesavior.Statistics.StatisticsInfo;
import com.example.inspiron.phonesavior.ui.AdminReceiver;
import com.example.inspiron.phonesavior.ui.AppSet;
import com.example.inspiron.phonesavior.ui.CameraActivity;
import com.example.inspiron.phonesavior.ui.NoticeMesg;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {

    private  long oriTime = 0;
    private  int i=1;
    private long setTime = 1; //设置1分钟


    public CameraService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("CameraService","onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(isRunning("com.example.inspiron.phonesavior.Service.CameraService")){//如果AppLimitService正在运行
                    Log.d("CameraService", "第" + i +"次");

                    punish();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();//开启线程
        return super.onStartCommand(intent, flags, startId);
    }

    private void punish() {
        Log.d("CameraService", "punish");

        if(i==1){
          //  StatisticsInfo statisticsInfo = new StatisticsInfo(this,StatisticsInfo.DAY);
            Calendar oricalendar = Calendar.getInstance();
            oriTime = (  oricalendar.getTimeInMillis());
            Log.d("CameraService", "oriTime: " + oriTime);
        }

        i++;

        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis() ;  //分钟
        Log.d("CameraService", "time: " + time);
        time = (time - oriTime) / 60000 ;
        Log.d("CameraService", "time - oriTime: " + time);
        if(time > setTime-1) {   //设置1分钟
            i = 1;
            time = 0;
            oriTime = 0;
            Looper.prepare();
            Toast.makeText(this.getApplicationContext(), "检测到您持续用眼，请注意用眼", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    //判断service是否运行
    public boolean isRunning(String name){
        ActivityManager myManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //获取所有正在运行的service
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
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
