package com.example.inspiron.phonesavior.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.Service.AppLimitService;

import java.io.*;

import static java.lang.System.exit;

public class IO extends AppCompatActivity implements View.OnClickListener {

    private String name;
    private int type;
    private String time;
    private String buddle;
    private RadioButton rb;
    private TextView tv;
    private EditText Time;
    private Button OK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);

        Bundle bundle=getIntent().getExtras();
        name= bundle.getString("name");
        TextView pro=(TextView)findViewById(R.id.program);
        String stem=pro.getText().toString();
        stem=stem+name;
        pro.setText(stem);
        //  System.out.println(name);
        Time=(EditText) findViewById(R.id.Time);
        tv = (TextView) findViewById(R.id.tvSex);
        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                //获取变更后的选中项的ID
                int radioButtonId = arg0.getCheckedRadioButtonId();
                //根据ID获取RadioButton的实例
                rb = (RadioButton) IO.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
                String s=rb.getText().toString();
                tv.setText("您选择的类型：" + s);
                String s1="友情提示"+"";
                String s2="睡眠"+"";
                String s3="重启"+"";
                String s4="关机"+"";
                String s5="取消设定"+"";
                if(s.equals(s1))type=0;
                else if(s.equals(s2))type=1;
                else if(s.equals(s3))type=2;
                else if(s.equals(s4))type=3;
                else if(s.equals(s5))type=4;
                else exit(0);
            }
        });
        OK=(Button) findViewById(R.id.OK);
        OK.setOnClickListener(this);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.OK:
                time=Time.getText().toString();
                int i;
                if(time.equals(""))
                    i = 0;
                else i=Integer.parseInt(time);
                    i=i*60;
                time=String.valueOf(i);
                try {
                    writeFileData("TEST.txt",name,type,time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent punish = new Intent(IO.this, AppLimitService.class);
                startService(punish);
                finish();
                break;
            default:
                break;
        }
    }
    public void writeFileData(String fileName,String label,int type, String time) throws IOException {
        if (Integer.parseInt(time) != 0) {
            File path = new File(getExternalCacheDir(), fileName);
            NoticeMesg noticeMesg = new NoticeMesg(this, "");
            boolean found = false;
            String content = "";
            if (path.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(path));
                String line = in.readLine();
                while (line != null) {
                    if (line.contains(label)) {
                        found = true;
                        if (type != 4) {
                            content += label + "#" + type + "#" + time + "\n";
                            Toast.makeText(this.getApplicationContext(), "设定成功!", Toast.LENGTH_SHORT).show();
                        } else {
                            noticeMesg.Toast("4");
                        }
                    } else {
                        content += line + "\n";
                    }
                    line = in.readLine();
                }
            }
            BufferedWriter bw;
            if (found) {
                bw = new BufferedWriter(new FileWriter(path.getAbsoluteFile()));
                bw.write(content);
                bw.flush();
                bw.close();
            } else {
                if (type != 4) {
                    String str = label + "#" + type + "#" + time + "\n";
                    bw = new BufferedWriter(new FileWriter(path.getAbsoluteFile(), true));
                    bw.write(str);
                    Toast.makeText(this.getApplicationContext(), "设定成功!", Toast.LENGTH_SHORT).show();
                    bw.flush();
                    bw.close();
                } else {
                    noticeMesg.Toast("4");
                }
            }

        }
    }
}

