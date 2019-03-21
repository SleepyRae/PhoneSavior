package com.example.inspiron.phonesavior.ui;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;

import com.example.inspiron.phonesavior.db.DBManager;
import com.example.inspiron.phonesavior.db.DayStatisticDBManager;
import com.example.inspiron.phonesavior.db.WeekStatisticDBManager;
import com.example.inspiron.phonesavior.engine.AppInfoProvider;
import com.example.inspiron.phonesavior.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @file AppContext.java
 * @package com.zp.quickaccess.ui
 * @comment 用于保存一些全局变量
 *
 * @author zp
 * @date 2015-12-29 下午10:28:09
 */
public class AppContext extends Application {

    private static final String TAG = "AppContext";
    public static AppContext mAppContext; // AppConetxt实例,为没有Context的服务或者广播接收者使用
    public static SharedPreferences mSharedPreferences;// SharedPreferences实例

    // 这两个变量很少使用，没必要全局静态，直接在使用的时候获取即可
    public static int appVersionCode; // app版本号
    public static String appVersionName; // app版本名字

    // 以下的全局变量也可以被替换
    public static DBManager mDBManager;
    public static DayStatisticDBManager mDayStatisticDBManager;
    public static WeekStatisticDBManager mWeekStatisticDBManager;

    public static AppInfoProvider mAppInfoProvider;
    public static android.app.ActivityManager mActivityManager;

    public static List<String> homeList;

    public static AppContext getAppContext() {
        return AppContext.mAppContext;
    }

    public static SharedPreferences getSharedPreferences() {
        return AppContext.mSharedPreferences;
    }

    /**
     * 初始化数据库实例和SharedPreferences实例
     *
     * @Title init
     */
    private void init() {
        AppContext.mAppContext = this;
        AppContext.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDBManager = new DBManager(mAppContext);
        mWeekStatisticDBManager = new WeekStatisticDBManager(mAppContext);
        mDayStatisticDBManager = new DayStatisticDBManager(mAppContext);
        mAppInfoProvider = new AppInfoProvider(mAppContext);
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        homeList = getHomes();
    }

    /**
     * 获取当前软件版本号
     *
     * @Title initAppInfo
     */
    private void initAppInfo() {
        final PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            pi = new PackageInfo();
            pi.versionName = "1.0";
            pi.versionCode = 20150101;
        }
        AppContext.appVersionCode = pi.versionCode;
        AppContext.appVersionName = pi.versionName;
    }

    /**
     * 获得属于桌面的应用的包名称
     *
     * 在桌面和本应用界面打开悬浮窗，其他应用界面不打开，因此在getHomes()方法返回的是所有应用的包名构成的字符串链表
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        // 获取桌面包名称
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            names.add(info.activityInfo.packageName);
        }
        // 将本包名也加进去
        names.add(this.getPackageName());
        return names;
    }

    /**
     * 判断当前界面是否是桌面
     */
    public static boolean isHome() {
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return homeList.contains(rti.get(0).topActivity.getPackageName());
    }

    public static boolean isHome(String pkgName) {
        return homeList.contains(pkgName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        initAppInfo();
        LogUtil.i(TAG, "App Context onCreate");
    }

}
