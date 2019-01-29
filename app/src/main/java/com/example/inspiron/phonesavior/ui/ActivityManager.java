package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * activity管理
 */
public class ActivityManager extends Application {
    private List<Activity> activityList = new LinkedList<Activity>();
    private static ActivityManager instance = null;

    private ActivityManager() {

    }

    /**
     * 从单例模式中获取唯一的ActivityManager实例
     */
    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 添加activity到容器中
     */
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    /**
     * 退出系统，销毁所有activity
     */
    public void exit() {
        for (Activity activity : activityList) {
            if (activity != null){
                activity.finish();
            }
        }
        activityList.clear();
        /*如果执行了System.exit(0)那么如果系统是开启了FloatViewService，那么会出现空指针异常
        执行System.exit(0)会导致添加在服务上的窗体也被关闭，而直接finish不会影响到该窗体
        然而奇怪的是WatchdogService开启后，使用System.exit(0)退出却不会出现空指针异常*/
        //System.exit(0);
    }
}
