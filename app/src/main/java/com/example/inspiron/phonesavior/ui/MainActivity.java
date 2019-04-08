package com.example.inspiron.phonesavior.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import android.widget.Toast;
import com.example.inspiron.phonesavior.Chart.DemoBase;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Statistics.AppInformation;
import com.example.inspiron.phonesavior.Statistics.StatisticsInfo;
import com.example.inspiron.phonesavior.adapter.MainUIAdapter;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends DemoBase implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private GridView gv;
    private MainUIAdapter adapter;
    private Intent intent;
    private ArrayList<AppInformation> ShowList;
//    long YLength;
//    int cellHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置没有标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        ActivityManager.getInstance().addActivity(this);

        intent = new Intent();

        gv = (GridView) findViewById(R.id.gv_main);
        adapter = new MainUIAdapter(this);
        gv.setAdapter(adapter);

        gv.setOnItemClickListener(this);

        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    /**
     * gridview条目被点击的响应事件
     *
     * parent：gv view：被点击的条目对应的view position：被点击条目的位置 id：被点击条目的id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0: // 使用帮助界面
                intent.setClass(this, HelpActivity.class);
                startActivity(intent);
                break;
            case 1: // 应用管理界面
                Intent intent1 = new Intent(MainActivity.this, AppManageActivity.class);
                startActivity(intent1);
                break;
            case 2: // 人脸检测界面
                Intent intent2 = new Intent(MainActivity.this, FaceDetectActivity.class);
                startActivity(intent2);
                break;
            case 3: // 应用信息查看界面
                Intent intent3 = new Intent(MainActivity.this, AppStaticsActivity.class);
                startActivity(intent3);
                break;
        }
    }

    @Override
    protected void onResume() {

        Log.d("MainActivity", "onResume");
        super.onResume();

        try {
            getSetList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        punish();
    }

    private void punish() {
        Log.d("MainActivity", "punish");

        StatisticsInfo statisticsInfo = new StatisticsInfo(this,StatisticsInfo.DAY);
        ShowList = statisticsInfo.getShowList();

        if(SetList.size() > 0) {
            for (final AppInformation appInformation : ShowList) {
                if (FoundAppSet(appInformation.getLabel())) {
                    final long time = appInformation.getUsedTimebyDay() / 1000;
                    if (time > Integer.parseInt(appSet.getTime())) {
                        if (appSet.getType() == AppSet.TIP) {
                            NoticeMesg noticeMesg = new NoticeMesg(MainActivity.this, appInformation.getLabel());
                            noticeMesg.Toast(String.valueOf(appSet.getType()));
                            noticeMesg.Time_Notice(appSet.getTime(), String.valueOf(time), String.valueOf(appSet.getType()));

                        } else if (appSet.getType() == AppSet.SLEEP) {
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后睡眠", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(MainActivity.this, appInformation.getLabel());
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
                                    componentName = new ComponentName(MainActivity.this, AdminReceiver.class);

                                    if (devicePolicyManager.isAdminActive(componentName))
                                        devicePolicyManager.lockNow();
                                    else {
                                        Registration();
                                    }
                                }
                            }, 5000);
                        } else if (appSet.getType() == AppSet.REBOOT) {
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后重启", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(MainActivity.this, appInformation.getLabel());
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
                            Toast.makeText(this.getApplicationContext(), "应用" + appInformation.getLabel() + "超时，手机将于5秒钟后关机", Toast.LENGTH_SHORT).show();
                            NoticeMesg noticeMesg = new NoticeMesg(MainActivity.this, appInformation.getLabel());
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

        Log.d("MainActivity", "delete_label");

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

        Log.d("MainActivity", "getSetList");

        SetList = new ArrayList<AppSet>();
        File path = new File(getExternalCacheDir(),"TEST.txt");
        if(path.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String line = in.readLine();
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

        Log.d("MainActivity", "FoundAppSet");

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
    private void init() {

        Log.d("MainActivity", "init");

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(MainActivity.this, AdminReceiver.class);
        Registration();

    }

    private void Registration() {

        Log.d("MainActivity", "Registration");

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "程式的描述");
        startActivityForResult(intent, 0);
    }
}
