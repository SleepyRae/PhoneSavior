package com.example.inspiron.phonesavior.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.inspiron.phonesavior.ui.AppContext;
import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.view.FloatViewManager;

/**
 * 悬浮窗的管理：对悬浮窗是否显示的控制
 *
 * 之前有尝试以动画的形式显示自定义窗体，由于窗体大小以及显示位置难以确定，因此没有采用 而是使用上面的二级窗体的形式显示
 *
 * 由于这个服务是不需要一直运行在后台的，因此采用绑定开启的方式，可以被用户显式的关闭
 *
 * 当点击开启浮窗的时候，显示悬浮窗同时开启这个服务；当用户点击关闭浮窗的时候，移除浮窗同时关闭该服务
 * 此处对于该服务的开启和关闭没有加以区分而是仅仅以一个StartCommand来控制是显示浮窗还是关闭浮窗
 * 没有对服务开启关闭进行控制，会造成不必要的服务的运行。同时由于StartCommand中
 *  intent.getIntExtra(OPERATION, SHOW_FLOATWINDOW);
 * 在应用关闭不当的时候总是导致空指针异常
 *
 * 解决办法还是使用绑定服务的方式，将悬浮窗的开启关闭和服务的声明周期联系在一起即可，去掉StartCommand中的逻辑
 *
 */

public class FloatViewService extends Service {
    private static final String TAG = "FloatViewService";
    public static final String OPERATION = "operation";

    public static final int SHOW_FLOATWINDOW = 100;
    public static final int HIDE_FLOATWINDOW = 101;
    private static final int HANDLE_HIDE_FLOATWINDOW = 102;
    private static final int HANDLE_SHOW_FLOATWINDOW = 103;
    private static final int HANDLE_CHECK_ACTIVITY = 200;

    // 初始时悬浮窗是没有添加的，也就是隐藏的
    //public boolean isAdded = false; // 用于判断悬浮窗实际是否被添加显示
    public boolean isHided = true;    // 用于判断用户是否设置悬浮窗显示
    public boolean triger = true;     // 用于触发HANDLE_CHECK_ACTIVITY消息，实现循环

    private Context context;
    private FloatViewManager mFloatViewManager;
    private static int operation;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mFloatViewManager = FloatViewManager.getInstance(context);

        LogUtil.i(TAG, "FloatViewService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        /*
         *  系统启动本服务的时候，存在intent为空的情形，导致应用崩溃
         *  所以此处进行了非空判断，虽然可以保证应用不崩溃，但是在本服务被优化关闭后不能被系统开启
         *  解决方法是将WatchdogService服务作为其deamon进程
         */
        if (intent == null) {
            LogUtil.i(TAG, "intent == null");
        } else {
            LogUtil.i(TAG, "intent != null");

            operation = intent.getIntExtra(OPERATION, SHOW_FLOATWINDOW);
            switch (operation) {
                case SHOW_FLOATWINDOW:
                    mHandler.sendEmptyMessage(HANDLE_SHOW_FLOATWINDOW);
                    break;
                case HIDE_FLOATWINDOW:
                    mHandler.sendEmptyMessage(HANDLE_HIDE_FLOATWINDOW);
                    break;
            }
            LogUtil.i(TAG, "operation : " + operation);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 此处的处理显示悬浮窗的消息处理略微杂乱了一些，主题逻辑为：
     *
     * 1.如果当前用户点击设置显示悬浮窗，那么就首先判断isAdded是否被添加，如果没有被添加，那么就将其添加
     * 同时设置FloatViewManager.isSmallWindowAdded为true,isHided为false
     *
     * 2.如果用户当前点击隐藏悬浮窗按钮，那么就先FloatViewManager.isSmallWindowAdded是否被添加，如果添加了，那么将其移除并设置为空
     * 同时设置FloatViewManager.isSmallWindowAdded为false,isHided为true
     *
     * 3.上述的两个按钮必定会有一个触发HANDLE_CHECK_ACTIVITY事件，于是就可以每100ms检查一次当前是否是桌面
     * 如果是桌面，而且悬浮窗体设置为不被隐藏(isHided为false)那么就按1的逻辑，判断悬浮窗是否被添加显示，若未添加，则将其添加显示
     * 如果不是桌面 ，悬浮窗设置的为不隐藏(isHided为true)那么就需要判断悬浮窗是否正在显示(FloatViewManager.isSmallWindowAdded为true)，
     * 如果正在显示，那么将其移除
     *
     * 需要注意的是：悬浮窗体的添加和删除都是通过FloatViewManager来实现的，其中添加和删除操作必须成对出现
     * 尤其是FloatViewManager会在多个类中使用，所以其函数的封装一定要保证这种成对的特点
     * 否则就会出现已经添加窗体的异常或者重复移除窗体的的异常
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CHECK_ACTIVITY:
                    // 只有当用户设置显示的时候才判断是否是桌面，以决定是否显示
                    boolean isOpenFloatview = AppContext.getSharedPreferences().getBoolean("isOpenFloatview", false);
                    if (isOpenFloatview) {

                        if (AppContext.isHome()) {
                            mFloatViewManager.addSmallFloatWindow();
                        } else {
                            if (FloatViewManager.isSmallWindowAdded)
                                mFloatViewManager.removeSmallWindow();
                        }
                    }else{
                        /*
                         *  如果用户设置的是不显示，那么就将自身关闭
                         *
                         *  及时关闭自己可以减少系统资源消耗，缺点是某些情况下可能响应不是很及时
                         *  毕竟下次开启需要重新开启或者需要Watchdog服务将其开启
                         */
                        stopSelf();
                    }
                    // 100毫秒执行一次检查，判断是否是切换到了桌面或者其他应用，以判定是否显示悬浮窗
                    mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY, 100);
                    break;

                case HANDLE_SHOW_FLOATWINDOW:
                    mFloatViewManager.addSmallFloatWindow();
                    LogUtil.i(TAG, "mFloatViewManager.addSmallFloatWindow()");
                    isHided = false;
                    // 为了使HANDLE_CHECK_ACTIVITY消息能够被触发(该消息没有对应按钮点击事件，所以需要自行触发)
                    // 此处用一个布尔变量保证其被触发一次即可，后面会周期性的执行
                    if (triger) {
                        mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
                        triger = false;
                    }
                    LogUtil.i(TAG, "HANDLE_SHOW_FLOATWINDOW");
                    break;

                case HANDLE_HIDE_FLOATWINDOW:
                    if(FloatViewManager.isSmallWindowAdded){
                        mFloatViewManager.removeSmallWindow();
                        isHided = true;
                        LogUtil.i(TAG, "mFloatViewManager.addSmallFloatWindow()");
                    }
                    if (triger) {
                        mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
                        triger = false;
                    }
                    LogUtil.i(TAG, "HANDLE_HIDE_FLOATWINDOW");
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "FloatViewService onDestroy");
    }

}