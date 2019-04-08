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
import com.example.inspiron.phonesavior.Service.AppLimitService;
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

/*    private ArrayList<AppInformation> ShowList;
//    long YLength;
//    int cellHeight;*/

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
            case 2: // 软件设置界面
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
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
    }
}
