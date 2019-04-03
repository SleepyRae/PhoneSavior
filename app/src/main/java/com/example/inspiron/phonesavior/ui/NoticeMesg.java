package com.example.inspiron.phonesavior.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.widget.Toast;
import com.example.inspiron.phonesavior.R;

import static android.content.Context.NOTIFICATION_SERVICE;


public class NoticeMesg {

    public NoticeMesg(Context context, String label) {
        this.context = context;
        this.label = label;
    }

    private Context context;
    private String label;

    public void Toast(String type){
        String s;
        switch (type){
            case "0":
                s="友情提示";
                Toast.makeText(context.getApplicationContext(), label + "使用已超时!", Toast.LENGTH_SHORT).show();
                break;
            case "1":
                s="睡眠";
                Toast.makeText(context.getApplicationContext(), label + "使用已超时，将于10秒钟后自动"+s+"!", Toast.LENGTH_SHORT).show();
                break;
            case "2":
                s="重启";
                Toast.makeText(context.getApplicationContext(), label + "使用已超时，将于10秒钟后自动"+s+"!", Toast.LENGTH_SHORT).show();
                break;
            case "3":
                s="关机";
                Toast.makeText(context.getApplicationContext(), label + "使用已超时，将于10秒钟后自动"+s+"!", Toast.LENGTH_SHORT).show();
            case "4":
                Toast.makeText(context.getApplicationContext(), label + "设定已取消!", Toast.LENGTH_SHORT).show();
                break;
            default:break;
        }


    }

    public void Time_Notice(String limit,String time, String type){

        if (type.equals("4"))return;
        switch (type){
            case "0":
                type="友情提示";
                break;
            case "1":
                type="睡眠";
                break;
            case "2":
                type="重启";
                break;
            case "3":
                type="关机";
                break;
            default:
                return;
        }

        String notice_title= "应用使用超时提醒";
        String notice_content= "您的"  + label + "超时："+ DateUtils.formatElapsedTime(Integer.parseInt(time) - Integer.parseInt(limit))+
              "\n惩罚类型："+type;
        Notice_launcher(notice_title, notice_content);

    }

    protected void Notice_launcher(String notice_title, String notice_content){

        Notification notifation= new Notification.Builder(context)
                .setContentTitle(notice_title)
                .setContentText(notice_content)
                .setSmallIcon(R.drawable.ic_phone)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_phone))
                .build();
        NotificationManager manger= (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manger.notify(0, notifation);
    }
}
