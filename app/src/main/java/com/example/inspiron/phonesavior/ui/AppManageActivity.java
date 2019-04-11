package com.example.inspiron.phonesavior.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Statistics.AppInformation;
import com.example.inspiron.phonesavior.Statistics.StatisticsInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private int style;
    SimpleAdapter adapter;
    ListView listView;
    private ArrayList<AppInformation> ShowList;
    private ArrayList<AppSet> SetList;
    private AppSet appSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manage);
        this.style = StatisticsInfo.DAY;

        Log.d("AppManageActivity", "onCreate");

    }
    //每次重新进入界面的时候加载listView
    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        Log.d("AppManageActivity", "onResume");

        List<Map<String,Object>> datalist = null;

        StatisticsInfo statisticsInfo = new StatisticsInfo(this,this.style);
        ShowList = statisticsInfo.getShowList();
        datalist = getDataList(ShowList);

        listView = (ListView)findViewById(R.id.AppManageList);
        adapter = new SimpleAdapter(this,datalist,R.layout.inner_list2,
                new String[]{"label","setTime","icon"},
                new int[]{R.id.label,R.id.setTime,R.id.icon});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

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

    }

    private List<Map<String,Object>> getDataList(ArrayList<AppInformation> ShowList) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        try {
            getSetList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String,Object> map;

        for(AppInformation appInformation : ShowList) {
            map = new HashMap<String,Object>();
            map.put("label",appInformation.getLabel());
            String info;
            if(FoundAppSet(appInformation.getLabel()))
            {
                info = "设定时间上限: " + DateUtils.formatElapsedTime(Integer.parseInt(appSet.getTime())) + "\n惩罚方式：";
                switch (appSet.getType()) {
                    case AppSet.TIP:
                        info += "通知提醒";
                        break;
                    case AppSet.SLEEP:
                        info += "自动锁屏";
                        break;
                    case AppSet.REBOOT:
                        info += "手机重启";
                        break;
                    case AppSet.SHUTDOWN:
                        info += "自动关机";
                        break;
                }
            }
            else info = "未设定";
            map.put("setTime",info);
            map.put("icon",appInformation.getIcon());
            dataList.add(map);
        }

        return dataList;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String label = ShowList.get(i).getLabel();

        Intent a= new Intent(this,IO.class);
        Bundle bundle=new Bundle();
        bundle.putString("name", label);
        a.putExtras(bundle);
        startActivity(a);

    }

    private void getSetList() throws IOException {
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
}

