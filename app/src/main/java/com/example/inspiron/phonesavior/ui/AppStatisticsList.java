package com.example.inspiron.phonesavior.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.example.inspiron.phonesavior.Chart.DemoBase;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Statistics.AppInformation;
import com.example.inspiron.phonesavior.Statistics.StatisticsInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppStatisticsList extends DemoBase {
    private int style;
    private long totalTime;
    private int totalTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_statistics_list);

        this.style = StatisticsInfo.DAY;

        Button buttonday = (Button) findViewById(R.id.daybuttonlist3);
        buttonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(style != StatisticsInfo.DAY) {
                    style = StatisticsInfo.DAY;
                    onResume();
                }
            }
        });
        Button buttonweek = (Button) findViewById(R.id.weekbuttonlist3);
        buttonweek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(style != StatisticsInfo.WEEK) {
                    style = StatisticsInfo.WEEK;
                    onResume();
                }
            }
        });
        Button buttonmonth = (Button) findViewById(R.id.monthbuttonlist3);
        buttonmonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(style != StatisticsInfo.MONTH) {
                    style = StatisticsInfo.MONTH;
                    onResume();
                }
            }
        });
        Button buttonyear = (Button) findViewById(R.id.yearbuttonlist3);
        buttonyear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(style != StatisticsInfo.YEAR) {
                    style = StatisticsInfo.YEAR;
                    onResume();
                }
            }
        });

        Button buttonbar = (Button) findViewById(R.id.BarButton3);
        buttonbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppStatisticsList.this,BarChartActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonpie = (Button) findViewById(R.id.PieButton3);
        buttonpie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppStatisticsList.this,PiePolylineChartActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void SetButtonColor() {
        Button buttonday = (Button) findViewById(R.id.daybuttonlist3);
        Button buttonmonth = (Button) findViewById(R.id.monthbuttonlist3);
        Button buttonyear = (Button) findViewById(R.id.yearbuttonlist3);
        Button buttonweek = (Button) findViewById(R.id.weekbuttonlist3);
        Button buttonpie = (Button)findViewById(R.id.PieButton3);
        Button buttonbar = (Button)findViewById(R.id.BarButton3);
        Button buttonlist = (Button)findViewById(R.id.ListButton3);

        buttonday.setTextColor(Color.GRAY);
        buttonmonth.setTextColor(Color.GRAY);
        buttonweek.setTextColor(Color.GRAY);
        buttonyear.setTextColor(Color.GRAY);
        buttonbar.setTextColor(Color.GRAY);
        buttonpie.setTextColor(Color.GRAY);
        buttonlist.setTextColor(Color.GRAY);

        switch (style) {
            case StatisticsInfo.DAY:
                buttonday.setTextColor(Color.parseColor("#4682B4"));
                break;
            case StatisticsInfo.MONTH:
                buttonmonth.setTextColor(Color.parseColor("#4682B4"));
                break;
            case StatisticsInfo.WEEK:
                buttonweek.setTextColor(Color.parseColor("#4682B4"));
                break;
            case StatisticsInfo.YEAR:
                buttonyear.setTextColor(Color.parseColor("#4682B4"));
                break;
        }

        String classname = this.getClass().getName();
        if(classname.contains("BarChartActivity")) {
            buttonbar.setTextColor(Color.parseColor("#228B22"));
        }
        else if(classname.contains("AppStatisticsList")) {
            buttonlist.setTextColor(Color.parseColor("#228B22"));
        }
        else if(classname.contains("PiePolylineChartActivity")) {
            buttonpie.setTextColor(Color.parseColor("#228B22"));
        }
    }

    //每次重新进入界面的时候加载listView
    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        SetButtonColor();

        List<Map<String,Object>> datalist = null;

        StatisticsInfo statisticsInfo = new StatisticsInfo(this,this.style);
        totalTime = statisticsInfo.getTotalTime();
        totalTimes = statisticsInfo.getTotalTimes();
        datalist = getDataList(statisticsInfo.getShowList());

        ListView listView = (ListView)findViewById(R.id.AppStatisticsList);
        SimpleAdapter adapter = new SimpleAdapter(this,datalist,R.layout.inner_list,
                new String[]{"label","info","times","icon"},
                new int[]{R.id.label,R.id.info,R.id.times,R.id.icon});
        listView.setAdapter(adapter);

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                if(view instanceof ImageView && o instanceof Drawable){

                    ImageView iv=(ImageView)view;
                    iv.setImageDrawable((Drawable)o);
                    return true;
                }
                else return false;
            }
        });

//        TextView textView = (TextView)findViewById(R.id.text1);
//        textView.setText("运行总时间: " + DateUtils.formatElapsedTime(totalTime / 1000));
    }

    private List<Map<String,Object>> getDataList(ArrayList<AppInformation> ShowList) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("label","全部应用");
        map.put("info","运行时间: " + DateUtils.formatElapsedTime(totalTime / 1000));
        map.put("times","本次开机操作次数: " + totalTimes);
        map.put("icon",R.drawable.use);
        dataList.add(map);

        for(AppInformation appInformation : ShowList) {
            map = new HashMap<String,Object>();
            map.put("label",appInformation.getLabel());
            map.put("info","运行时间: " + DateUtils.formatElapsedTime(appInformation.getUsedTimebyDay() / 1000));
            map.put("times","本次开机操作次数: " + appInformation.getTimes());
            map.put("icon",appInformation.getIcon());
            dataList.add(map);
        }

        return dataList;
    }
}
