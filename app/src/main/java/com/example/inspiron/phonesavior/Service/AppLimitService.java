package com.example.inspiron.phonesavior.Service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.example.inspiron.phonesavior.Statistics.AppInformation;
import com.example.inspiron.phonesavior.Statistics.StatisticsInfo;
import com.example.inspiron.phonesavior.ui.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

public class AppLimitService extends Service {

    private ArrayList<AppInformation> ShowList;
    private  long oriTime = 0;
    private  int i=1;

    public AppLimitService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AppLimitService","onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AppLimitService", "onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {

                //int i = 0;
                while(isRunning("com.example.inspiron.phonesavior.Service.AppLimitService")){//如果AppLimitService正在运行
                    Log.d("AppLimitService", "第" + i +"次");
                    try {
                        getSetList();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    punish();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                            }
        }).start();//开启线程


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AppLimitService", "onDestroy");
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

    private void punish() {
        Log.d("AppLimitService", "punish");

        StatisticsInfo statisticsInfo = new StatisticsInfo(this,StatisticsInfo.DAY);
        ShowList = statisticsInfo.getShowList();

        if(SetList.size() > 0) {
            for (final AppInformation appInformation : ShowList) {

                //解决当前应用时间戳过时问题
                Calendar calendar = Calendar.getInstance();
                appInformation.setTimeStampMoveToBackGround( calendar.getTimeInMillis());
                appInformation.calculateRunningTime();

                if (FoundAppSet(appInformation.getLabel())) {
                    if(i==1){
                       oriTime = appInformation.getUsedTimebyDay() / 1000;
                    }
                    i++;
                    long time = appInformation.getUsedTimebyDay() / 1000;
                    time = time - oriTime;

                    Log.d("AppLimitService", "time : " + time + "  oriTime: " + oriTime);

                    if (time > Integer.parseInt(appSet.getTime())) {
                        if (appSet.getType() == AppSet.TIP) {
                            i = 0;
                            NoticeMesg noticeMesg = new NoticeMesg(AppLimitService.this, appInformation.getLabel());
                            noticeMesg.Toast(String.valueOf(appSet.getType()));
                            noticeMesg.Time_Notice(appSet.getTime(), String.valueOf(time), String.valueOf(appSet.getType()));

                        } else if (appSet.getType() == AppSet.SLEEP) {
                            i = 0;
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后睡眠", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(AppLimitService.this, appInformation.getLabel());
                            noticeMesg.Time_Notice(appSet.getTime(), String.valueOf(time), String.valueOf(appSet.getType()));
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    //执行惩罚之后删除之前的设定
                                    try {
                                        delete_label(appInformation.getLabel());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                                    componentName = new ComponentName(AppLimitService.this, AdminReceiver.class);

                                    if (devicePolicyManager.isAdminActive(componentName))
                                        devicePolicyManager.lockNow();
                                    else {
                                        Registration();
                                    }
                                }
                            }, 5000);
                        } else if (appSet.getType() == AppSet.REBOOT) {
                            i = 0;
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后重启", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(AppLimitService.this, appInformation.getLabel());
                            noticeMesg.Time_Notice(appSet.getTime(), String.valueOf(time), String.valueOf(appSet.getType()));

                            //执行惩罚之后删除之前的设定
                            try {
                                delete_label(appInformation.getLabel());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            new Handler().postDelayed(new Runnable() {
                                public void run() {

                                    PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
                                    pManager.reboot(null);//重启</span>
                                    try {
                                        delete_label(appInformation.getLabel());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 5000);

                        } else if (appSet.getType() == AppSet.SHUTDOWN) {
                            i = 0;
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后关机", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(AppLimitService.this, appInformation.getLabel());
                            noticeMesg.Time_Notice(appSet.getTime(), String.valueOf(time), String.valueOf(appSet.getType()));

                            //执行惩罚之后删除之前的设定
                            try {
                                delete_label(appInformation.getLabel());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    try {
                                        //获得ServiceManager类
                                        Class<?> ServiceManager = Class
                                                .forName("android.os.ServiceManager");

                                        //获得ServiceManager的getService方法
                                        Method getService = ServiceManager.getMethod("getService", java.lang.String.class);

                                        //调用getService获取RemoteService
                                        Object oRemoteService = getService.invoke(null,Context.POWER_SERVICE);

                                        //获得IPowerManager.Stub类
                                        Class<?> cStub = Class
                                                .forName("android.os.IPowerManager$Stub");
                                        //获得asInterface方法
                                        Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
                                        //调用asInterface方法获取IPowerManager对象
                                        Object oIPowerManager = asInterface.invoke(null, oRemoteService);
                                        //获得shutdown()方法
                                        Method shutdown = oIPowerManager.getClass().getMethod("shutdown",boolean.class,boolean.class);
                                        shutdown.invoke(oIPowerManager,false,true);

                                    } catch (Exception e) {

                                    }
                                }
                            }, 5000);

                        }
                    }
                }
            }
        }
    }

    private void delete_label(String label) throws IOException{

        Log.d("AppLimitService", "delete_label");

        File path = new File(getExternalCacheDir(),"TEST.txt");
        String content = "";
        BufferedReader in = new BufferedReader(new FileReader(path));
        String line = in.readLine();
        while (line != null) {
            if (line.contains(label)) {

            } else {
                content += line + "\n";
            }
            line = in.readLine();
        }
        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(path.getAbsoluteFile()));
        bw.write(content);
        bw.flush();
        bw.close();
    }

    private ArrayList<AppSet> SetList;
    private AppSet appSet;

    private void getSetList() throws IOException {

        Log.d("AppLimitService", "getSetList");

        SetList = new ArrayList<AppSet>();
        File path = new File(getExternalCacheDir(),"TEST.txt");
        if(path.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String line = in.readLine();
            Log.d("AppLimitService", line);
            while (line != null) {
                int n1 = line.indexOf("#");
                int n2 = line.indexOf("#",n1 + 1);
                String label = line.substring(0,n1);
                String type = line.substring(n1 + 1,n2);
                String time = line.substring(n2 + 1);

                AppSet appSet = new AppSet(label,Integer.parseInt(type),time);
                SetList.add(appSet);
                line = in.readLine();
            }
        }
    }

    boolean FoundAppSet(String label) {

        Log.d("AppLimitService", "FoundAppSet");

        if(SetList.size() > 0) {
            for(AppSet appSet1 : SetList) {
                if(appSet1.getLabel().equals(label)) {
                    appSet = appSet1;
                    return true;
                }
            }
        }
        return false;
    }

    private ComponentName componentName;

    //赋予app相应的sleep的权限
    DevicePolicyManager devicePolicyManager;

    private void Registration() {

        Log.d("AppLimitService", "Registration");

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "程式的描述");
        startActivity(intent);
    }

}
