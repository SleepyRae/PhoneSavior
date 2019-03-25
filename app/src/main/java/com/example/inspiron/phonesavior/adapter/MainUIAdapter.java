package com.example.inspiron.phonesavior.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inspiron.phonesavior.R;

public class MainUIAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private static String names[] = { "使用帮助", "应用设置", "人脸检测", "统计信息" };
    private static int icons[] = { R.drawable.phone, R.drawable.phone, R.drawable.phone, R.drawable.phone };

    public MainUIAdapter(Context c) {
        this.context = c;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.activity_main_item, null);
        ImageView iv_main_item_icon = (ImageView) view.findViewById(R.id.iv_main_item_icon);
        TextView tv_main_item_name = (TextView) view.findViewById(R.id.tv_main_item_name);

        iv_main_item_icon.setImageResource(icons[position]);
        tv_main_item_name.setText(names[position]);

        return view;
    }

}
