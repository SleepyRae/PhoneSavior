package com.example.inspiron.phonesavior.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.inspiron.phonesavior.R;

/**
 *	使用帮助界面：指导用户如何使用本app
 *
 */
public class HelpActivity extends AppCompatActivity {

    private TextView tv_common_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_help);
        tv_common_title = (TextView) findViewById(R.id.tv_common_title);
        tv_common_title.setText(R.string.app_help_tv_title);
        /*要粘resource里的文件*/
    }
}
