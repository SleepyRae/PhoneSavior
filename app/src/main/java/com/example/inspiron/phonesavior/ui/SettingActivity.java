package com.example.inspiron.phonesavior.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.inspiron.phonesavior.R;

public class SettingActivity extends AppCompatActivity implements OnClickListener{
    private LinearLayout ll_set_facedetect;
    private LinearLayout ll_set_about_author;
    private LinearLayout ll_set_product_info;
    private LinearLayout ll_set_exit;
    private TextView tv_set_facedetect;
    private TextView tv_common_title;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();//初始化视图
    }


    private void initView() {
        this.ll_set_facedetect = (LinearLayout)this.findViewById(R.id.ll_set_facedetect);
        this.ll_set_about_author=(LinearLayout)this.findViewById(R.id.ll_set_about_author);
        this.ll_set_product_info = (LinearLayout)this.findViewById(R.id.ll_set_product_info);
        this.ll_set_exit = (LinearLayout)this.findViewById(R.id.ll_set_exit);
        this.tv_set_facedetect = (TextView)this.findViewById(R.id.tv_set_facedetect);
        this.tv_common_title = (TextView)this.findViewById(R.id.tv_common_title);
        this.ll_set_about_author.setOnClickListener(this);
        this.ll_set_product_info.setOnClickListener(this);
        this.ll_set_exit.setOnClickListener(this);
        this.ll_set_facedetect.setOnClickListener(this);
        ActivityManager.getInstance().addActivity(this);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.ll_set_facedetect:
                Intent intentFaceDetect = new Intent(this, FaceDetectActivity.class);
                this.startActivity(intentFaceDetect);
                break;
            case R.id.ll_set_about_author:
                Intent intentAboutAuthor = new Intent(this, AboutAuthorActivity.class);
                this.startActivity(intentAboutAuthor);
                break;
            case R.id.ll_set_product_info:
                Intent intentAboutApp = new Intent(this, AboutAppActivity.class);
                this.startActivity(intentAboutApp);
                break;
            case R.id.ll_set_exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("确定退出吗").setIcon(R.drawable.warning)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which) {new TVAnimation();
                                ActivityManager.getInstance().exit();
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                    }
                });
                builder.create().show();
        }

    }
}
