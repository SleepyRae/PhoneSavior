package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.animation.TVAnimation;
import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.service.FloatViewService;
import com.example.inspiron.phonesavior.service.WatchdogService;

import java.util.Collections;
import java.util.List;

/**
 * 应用设置界面：主要包括开启或者关闭悬浮窗；手动设置自己的常用应用；检查更新；关于；清除历史设置(数据)
 *
 * 注意悬浮窗是否开启的状态标识需要写入到sp中，如果只是在程序中使用临时变量保存，并不能记录用户的实际设置状态
 *
 */
public class SettingActivity extends Activity implements OnClickListener {
    private static final String TAG = "SettingActivity";
    private final static int INIT_DATA_FINISHED = 200;
    private final static int CLEAR_CACHE_FINISHED = 400;
    private final static int CLEAR_CACHE_CANCEL = 401;

    private LinearLayout ll_set_floatview_status;
    private LinearLayout ll_set_about_author;
    private LinearLayout ll_set_product_info;
    private LinearLayout ll_set_clear_cache;
    private LinearLayout ll_set_check_update;
    private LinearLayout ll_set_exit;
    private TextView tv_set_floatview_status;
    private TextView tv_common_title;
    // 是否开启悬浮窗的标志位，初始为false，需要从SharedPreference中读取用户的实际设置值
    private SharedPreferences sp;
    private ProgressDialog pd;

    private boolean isOpenFloatview = false;
    private Intent mFloatViewService;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_CACHE_FINISHED:
                    pd.dismiss();
                    Toast.makeText(SettingActivity.this, "缓存已经清除", 0).show();
                    break;
                case CLEAR_CACHE_CANCEL:
                    Toast.makeText(SettingActivity.this, "取消清除", 0).show();
                    break;
                // 数据初始化完成
                case INIT_DATA_FINISHED:
                    pd.dismiss();
                    Intent watchdogService = new Intent(SettingActivity.this, WatchdogService.class);
                    startService(watchdogService);
                    break;
            }

        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);

        initViews();

        boolean isFirst = AppContext.getSharedPreferences().getBoolean("isFirst", true);
        // 开启子线程加载应用信息
        /**
         * 如果是第一次开启页面，那么说明数据库为空，WatchdogService也没有开启
         * 所以可以在本Activity或者APPManageActivity中添加数据到数据库并开启WatchdogService进行数据统计
         * 默认情况下并不开启服务进行统计
         */
        if (isFirst) {
            pd = ProgressDialog.show(SettingActivity.this, "", "正在加载数据请稍候……", true, false);
            new Thread() {
                public void run() {
                    List<AppUseStatics> infos = AppContext.mAppInfoProvider.getAllApps();
                    // 按优先级对其递增排序
                    Collections.sort(infos);
                    AppContext.mDBManager.addAll(infos);
                    Editor editor = AppContext.mSharedPreferences.edit();
                    editor.putBoolean("isFirst", false);
                    editor.commit();
                    Message msg = new Message();
                    msg.what = INIT_DATA_FINISHED;
                    handler.sendMessage(msg);
                };
            }.start();
        }

    }

    private void initViews() {
        ll_set_floatview_status = (LinearLayout) findViewById(R.id.ll_set_floatview_status);
        ll_set_about_author = (LinearLayout) findViewById(R.id.ll_set_about_author);
        ll_set_product_info = (LinearLayout) findViewById(R.id.ll_set_product_info);
        ll_set_clear_cache = (LinearLayout) findViewById(R.id.ll_set_clear_cache);
        ll_set_check_update = (LinearLayout) findViewById(R.id.ll_set_check_update);
        ll_set_exit = (LinearLayout) findViewById(R.id.ll_set_exit);

        tv_set_floatview_status = (TextView) findViewById(R.id.tv_set_floatview_status);
        // 标题栏文本
        tv_common_title = (TextView) findViewById(R.id.tv_common_title);
        tv_common_title.setText(R.string.app_set_tv_title);

        sp = AppContext.getSharedPreferences();
        pd = new ProgressDialog(this);

        ll_set_about_author.setOnClickListener(this);
        ll_set_product_info.setOnClickListener(this);
        ll_set_clear_cache.setOnClickListener(this);
        ll_set_check_update.setOnClickListener(this);
        ll_set_exit.setOnClickListener(this);
        ll_set_floatview_status.setOnClickListener(this);

        // 当Activity被finish掉之后，service怎么办？最近总是在服务中抛出空指针异常
        mFloatViewService = new Intent(getApplicationContext(), FloatViewService.class);
        // 将Activity添加到任务栈中
        ActivityManager.getInstance().addActivity(SettingActivity.this);
    }

    /**
     * 由于悬浮窗的状态需要根据实际情形进行设置，所以在窗体可见的时候根据用户设置对其状态进行更新
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 从sp中获取用户设置状态
        isOpenFloatview = sp.getBoolean("isOpenFloatview", false);
        if(isOpenFloatview){
            tv_set_floatview_status.setText("关闭浮窗");
        }else{
            tv_set_floatview_status.setText("开启浮窗");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 悬浮窗的开启和关闭
            case R.id.ll_set_floatview_status:
                /*
                 * 悬浮窗实现参考
                 * http://blog.csdn.net/stevenhu_223/article/details/8504058
                 */
                if (isOpenFloatview) {
                    tv_set_floatview_status.setText("开启浮窗");

                    isOpenFloatview = false;
                    Editor editor = sp.edit();
                    editor.putBoolean("isOpenFloatview", isOpenFloatview);
                    editor.commit();

                    mFloatViewService.putExtra(FloatViewService.OPERATION,FloatViewService.HIDE_FLOATWINDOW);
                    startService(mFloatViewService);
                    Toast.makeText(this, "悬浮窗已经关闭", 0).show();
                } else {
                    tv_set_floatview_status.setText("关闭浮窗");

                    isOpenFloatview = true;
                    Editor editor = sp.edit();
                    editor.putBoolean("isOpenFloatview", isOpenFloatview);
                    editor.commit();

                    mFloatViewService.putExtra(FloatViewService.OPERATION, FloatViewService.SHOW_FLOATWINDOW);
                    startService(mFloatViewService);
                    Toast.makeText(this, "悬浮窗已经打开", 0).show();
                }
                break;

            // 关于作者
            case R.id.ll_set_about_author:
/*                Intent intentAboutAuthor = new Intent(this,	AboutAuthorActivity.class);
                startActivity(intentAboutAuthor);
                LogUtil.i(TAG, "about_author : super sugar");*/
                break;

            // 查看产品信息
            case R.id.ll_set_product_info:
/*                Intent intentAboutApp = new Intent(this, AboutAppActivity.class);
                startActivity(intentAboutApp);
                LogUtil.i(TAG, "product_info : a great products");*/
                break;

            // 清除临时缓存数据
            /**
             * 缓存数据主要是指应用使用情况的统计数据，包括所有应用统计数据，周数据，排名前三的应用信息
             *
             * 在清除数据之前需要弹出一个对话框和用户进行确认
             */
            case R.id.ll_set_clear_cache:
                final Message msg = new Message();
                AlertDialog.Builder builder1 = new Builder(this);
                builder1.setIcon(R.drawable.ic_launcher);
                builder1.setTitle("确定清除数据吗");
                builder1.setMessage("此操作将会清除所有应用使用情况的统计信息");
                builder1.setPositiveButton("确定",
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*
                                 *  清除数据的逻辑:清除的数据包括：数据库的统计信息和sp中的六个值
                                 *  3个数据库，所有应用数据库只清除次数，权重，时间，周数据和每日数据直接deleteAll
                                 *  六个sp数据直接全部设置为初始的0即可
                                 *
                                 *  由于删除数据比较耗时，所以使用子线程删除
                                 */
                                pd.setMessage("正在清除数据...");
                                pd.setCancelable(false);
                                pd.show();

                                new Thread(){
                                    public void run() {
                                        // 6个Sp的数据置为0
                                        Editor editor = AppContext.getSharedPreferences().edit();

                                        editor.putInt("day_count", 0);
                                        editor.putInt("day_time", 0);
                                        editor.putInt("day_num", 0);

                                        editor.putInt("week_count", 0);
                                        editor.putInt("week_time", 0);
                                        editor.putInt("week_num", 0);

                                        editor.commit();

                                        // 清除所有应用的统计数据，仅仅是将其三个统计值置为0，并不是将数据库置空
                                        List<AppUseStatics> infos = AppContext.mDBManager.findAll();
                                        for(int i=0;i<infos.size();i++){
                                            infos.get(i).setUseFreq(0);
                                            infos.get(i).setUseTime(0);
                                            infos.get(i).setWeight(0);
                                        }
                                        // TODO: 2016/9/15 修改数据库中的值是如何实现？？直接删除所有再添加所有的方法太低效
                                        // update  表  set  字段=值 where
                                        AppContext.mDBManager.deleteAll();
                                        AppContext.mDBManager.addAll(infos);

                                        // 清除每日数据统计和每周数据统计
                                        AppContext.mWeekStatisticDBManager.deleteAll();
                                        AppContext.mDayStatisticDBManager.deleteAll();


                                        // 数据清除完成，发消息更新UI
                                        msg.what = CLEAR_CACHE_FINISHED;
                                        handler.sendMessage(msg);
                                    };
                                }.start();
                            }
                        });
                builder1.setNegativeButton("取消",
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                msg.what = CLEAR_CACHE_CANCEL;
                                handler.sendMessage(msg);
                            }
                        });
                builder1.create().show();
                break;

            // 检查更新点击事件，访问服务器检查新版本
            case R.id.ll_set_check_update:
                Toast.makeText(this, "作者决定不更新了，没有新版本啦", 0).show();
                break;

            // 退出应用
            /**
             * 最好添加一个类似电视机关闭的属性动画
             */
            case R.id.ll_set_exit:
                AlertDialog.Builder builder = new Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("确定退出吗");
                builder.setPositiveButton("确定",
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TVAnimation tva = new TVAnimation();

                                ActivityManager.getInstance().exit();
                            }
                        });
                builder.setNegativeButton("取消",
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        });
                builder.create().show();
                break;
        }

    }
}
