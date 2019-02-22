package com.example.inspiron.phonesavior.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.example.inspiron.phonesavior.db.DayStatisticDBManager;
import com.example.inspiron.phonesavior.db.WeekStatisticDBManager;
import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.engine.AppInfoProvider;
import com.example.inspiron.phonesavior.ui.AppContext;
import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.view.FloatViewManager;

/**
 * 监视当前开启的应用的任务栈并检查其最顶层的activity
 *
 * 用于应用使用信息的统计，实了现监视屏幕开启且不是桌面的情形下的栈顶应用
 * 然后根据获取的应用信息查询数据库得到应用使用的统计数据 并将每次的应用使用信息更新到数据库
 *
 * 之前的监视方法：
 * 对于activity栈顶变化可以划分为4中情形：
 * 桌面-应用：计时开始
 * 应用-桌面：计时结束，更新数据库
 * 应用-应用：又分为：本应用-本应用 或者 本应用-其他应用
 * 桌面-桌面：此情形不计时
 *
 * 1秒钟的循环，对于进入之后迅速退出的程序而言可能检测不到，不过不必那么精确
 *
 * 由于在进行判断的时候，将本应用也作为桌面加入，因此从本应用到桌面 == 桌面--桌面 从桌面--本应用 == 桌面--桌面 从其他应用--本应用 ==
 * 应用到桌面 会造成一定的统计误差
 *
 * 如果不可以去制造极端测试情况的话，还是比较准确的，次数，时间，开启应用数目 非极端的正常使用都是正确的。
 * 除非一秒钟做多次切换无法准确的监测
 *
 * 需要注意一点的是，虽然自己监听了软件安装卸载的广播事件，而且以此为根据更新数据库
 * 但是总是会有意外情况，所以如果将数据插入数据库之前可以先判断一下该应用是不是在数据库中
 * 如果不在，那么将其添加进去。也就是修改updateUseTime()方法，添加一个判断即可
 */

public class WatchdogService extends Service {
    private static final String TAG = "WatchdogService";
    private ActivityManager mActivityManager;

    private List<ActivityManager.RunningTaskInfo> infos;
    private ActivityManager.RunningTaskInfo topTaskInfo;

    private String prePackageName;
    private String nextPackageName;

    private int sleepLength = 1000; // 线程休眠时长
    private int timeCount = 0; // 时间计数器
    private String week;
    private String curWeek;

    @Override
    public void onCreate() {
        LogUtil.i(TAG, "WatchdogService onCreate");

        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final WeekStatisticDBManager wsMansger = new WeekStatisticDBManager(this);
        final DayStatisticDBManager dsMansger = new DayStatisticDBManager(this);

        /*
         *  目前存在一种无法统计的情形：在统计的过程中，统计数据还没有写入到外存中，但是服务被系统终止，
         *  然后被再次开启，在终止之前所统计的数据就丢失了。不过此种情况较少发生，低配机器上内存不足时可能会发生
         *
         *  解决办法？？被系统结束时走的是哪条声明周期路线？被系统终止后数据是否会存储在线程中或Bundle？？
         */
        if(timeCount != 0){
            updateUseTime(prePackageName);
            updateSharedpreference(prePackageName);
            LogUtil.i(TAG, "onCreate : timeCount = " + timeCount);
        }

        new Thread() {
            public void run() {
                prePackageName = getPackageName();
                nextPackageName = getPackageName();
                PowerManager mPowerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = true;
                LogUtil.i(TAG, "preAppInfo : package name : " + prePackageName);
                while (true) {
                    try {
                        sleep(sleepLength);
                        isScreenOn = mPowerManager.isScreenOn();
                        // 只有当屏幕亮起的时候才进行统计
                        if(isScreenOn){
                            LogUtil.i(TAG, "屏幕亮起，开始统计");

                            // 获取当前是一周的第几天

                            // 此处如果使用静态或者全局mCalendar对象将会使更新延迟
                            //而每次获取新的对象可以保证及时更新数据
                            Calendar mCalendar = Calendar.getInstance();
                            mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                            curWeek = String.valueOf(mCalendar.get(Calendar.DAY_OF_WEEK));
                            week = AppContext.getSharedPreferences().getString("week", "");
                            LogUtil.w(TAG, "curWeek = " + curWeek + "  week = " + week);
                            // 如果day_of_week变化了，说明是新的一天。更新sp中的数据
                            if (!week.equals(curWeek)) {
                                SharedPreferences.Editor editor = AppContext.getSharedPreferences().edit();
                                // 新的一天，更新每日数据：初始化为0
                                editor.putInt("day_count", 0);
                                editor.putInt("day_time", 0);
                                editor.putInt("day_num", 0);
                                // 必须更新week的值
                                editor.putString("week", curWeek);

                                // 更新周数据的一个标志位，保证下面的if语句体只执行一次
                                // 而不是一整天都在清零
                                editor.putBoolean("week_flag", true);

                                editor.commit();
                                // 清空每日数据库
                                dsMansger.deleteAll();
                                LogUtil.w(TAG, "日数据清空：curWeek =  " + curWeek);
                            }
                            // 清空周数据。也可以设置为一周的其他天
                            // 需要保证这个逻辑仅仅执行一次，而不是一整天都在执行
                            if ( "1".equals(curWeek)) { // 星期日为1,星期一为2，类推
                                if(AppContext.getSharedPreferences().getBoolean("week_flag", true)){
                                    SharedPreferences.Editor editor = AppContext.getSharedPreferences().edit();
                                    // 更新周信息数据 ：初始化为0
                                    editor.putInt("week_count", 0);
                                    editor.putInt("week_time", 0);
                                    editor.putInt("week_num", 0);

                                    editor.putBoolean("week_flag", false);

                                    editor.commit();
                                    // 清空每周数据库
                                    wsMansger.deleteAll();
                                    LogUtil.w(TAG, "周数据清空： Calendar.SUNDAY = " + String.valueOf(mCalendar.get(Calendar.SUNDAY)));
                                }
                            }

                            // 获取当前正在运行的任务
                            infos = mActivityManager.getRunningTasks(1);
                            // 获取正在运行的任务，然后获取该任务的人物栈的最顶端的Activity，进而获取该任务对应的包名
                            // 根据包名可以查询应用信息数据库得出该包名对应的应用的所有信息
                            topTaskInfo = infos.get(0);
                            // 获取当前正在运行的应用的任务栈，获取当前用户可见的Activity
                            nextPackageName = topTaskInfo.topActivity.getPackageName();

                            if (AppContext.isHome(nextPackageName)) {
                                // 守护FloatViewService
                                deamonOfFloatViewService();

                                if (AppContext.isHome(prePackageName)) {
                                    // 1 1 ：nextPackageName是桌面  prePackageName是桌面，不统计
                                    // com.sec.android.app.launcher--com.sec.android.app.launcher或者
                                    // com.sec.android.app.launcher和com.zp.quickaccess 被视为是桌面，因此在这种情形下也会执行如下的log
                                    LogUtil.i(TAG, "1 1 : " + nextPackageName + "--" + prePackageName);
                                } else {
                                    writeToStorage();
                                    // 1 0 : nextPackageName是桌面  prePackageName不是桌面，从非桌面到桌面，计时结束
                                    LogUtil.i(TAG, "1 0 : " + nextPackageName + "--" + prePackageName);
                                }
                            } else {
                                if (AppContext.isHome(prePackageName)) {
                                    // 0 1 ：nextPackageName不是桌面  prePackageName是桌面，从桌面到非桌面，计时开始
                                    timeCount = timeCount + 1;
                                    prePackageName = nextPackageName;
                                    LogUtil.i(TAG, "0 1 : " + nextPackageName + "--" + prePackageName);
                                } else {
                                    // 0 0：nextPackageName不是桌面  prePackageName不是桌面，从非桌面到非桌面，计时增加
                                    if (prePackageName.equals(nextPackageName)) {
                                        // 0 0 1 ：如果nextPackageName和prePackageName相同，那么计时增加
                                        timeCount = timeCount + 1;
                                        LogUtil.i(TAG, "0 0 1");
                                    } else {
                                        // 0 0 0： 如果nextPackageName和prePackageName不同，那么说明从prePackageName跳转到了nextPackageName
                                        // 		  也就是prePackageName的计时结束，nextPackageName的计时开始
                                        writeToStorage();
                                        LogUtil.i(TAG, "0 0 0");
                                    }
                                }
                            }
                            // end if(isScreenOn)
                        } else { // 屏幕熄灭
                            /*
                             * 	屏幕熄灭的时候如果统计并没有结束，那么将当前统计结果写入，然后将统计变量初始化
                             *
                             *  避免统计时间丢失：比如当前正在运行音乐软件，然后直接锁屏，如果不在else中将之前统计数据写入
                             *  那么可能会造成之前数据的丢失
                             *
                             *  写入统计结果的同时，将服务自身关闭，减少不必要的运行。当用户下次解锁屏幕的时候再次开启此服务
                             */
                            if(timeCount != 0){
                                writeToStorage();
                                LogUtil.i(TAG, "timeCount != 0");
                            }else{
                                LogUtil.i(TAG, "timeCount == 0");
                            }
                            stopSelf();
                            LogUtil.i(TAG, "屏幕熄灭，结束统计，服务停止运行");
                            break; // 结束服务之后跳出循环
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // end while
            } // end run

            /**
             * 将统计数据写入到SharedPreference和数据库
             */
            private void writeToStorage() {
                updateUseTime(prePackageName);
                updateSharedpreference(prePackageName);
                timeCount = 0;
                prePackageName = nextPackageName;
            };
        }.start();
    }

    /**
     *
     * @comment FloatViewService的守护方法，当用户设置的显示悬浮窗（isOpenFloatview为true）
     * 			而悬浮窗没有显示（isSmallWindowShowing为false）,那么说明服务被系统杀死，需要重新开启
     * @param
     * @return void
     * @throws
     * @date 2015-12-29 下午10:46:01
     */
    public void deamonOfFloatViewService(){
        boolean isOpenFloatview = AppContext.getSharedPreferences().getBoolean("isOpenFloatview", false);
        boolean isSmallWindowShowing = FloatViewManager.getInstance(this).isSmallWindowShowing();
        if(isOpenFloatview){
            if(!isSmallWindowShowing){
                Intent mFloatViewService = new Intent(getApplicationContext(), FloatViewService.class);
                mFloatViewService.putExtra(FloatViewService.OPERATION,FloatViewService.HIDE_FLOATWINDOW);
                startService(mFloatViewService);
                LogUtil.i(TAG, "deamonOfFloatViewService : WatchdogService start FloatViewService");
            }
        }else{
            // // 关于关闭服务的操作还是FloatViewService自己来做
//			Intent mFloatViewService = new Intent(getApplicationContext(), FloatViewService.class);
//			stopService(mFloatViewService);
        }
    }

    /**
     *
     * @comment mAppPkgNames 的获取是需要从数据库中获取的,而且mAppPkgNames最好设置为局部变量
     * 			否则容易出现空指针异常，或者当应用完全退出再完全开启的时候就会出现重复统计的情况
     *
     * @param @param pkgName   根据包名区分应用
     * @return void
     * @throws
     * @date 2015-12-28 下午4:12:10
     */
    private void updateSharedpreference(String pkgName) {
        List<String> mDayAppPkgNames = new ArrayList<String>();
        List<String> mWeekAppPkgNames = new ArrayList<String>();
        DayStatisticDBManager dsMansger = new DayStatisticDBManager(this);
        WeekStatisticDBManager wsMansger = new WeekStatisticDBManager(this);
        mDayAppPkgNames = dsMansger.findAllPkgNames();
        mWeekAppPkgNames = wsMansger.findAllPkgNames();

        // 使用的应用的个数加1
        SharedPreferences.Editor editor = AppContext.getSharedPreferences().edit();
        if (!mDayAppPkgNames.contains(pkgName)) {
            dsMansger.addByName(pkgName);
            // 使用app的个数加1
            editor.putInt("day_num", AppContext.getSharedPreferences().getInt("day_num", 0) + 1);

        }
        if(!mWeekAppPkgNames.contains(pkgName)){
            wsMansger.addByName(pkgName);
            editor.putInt("week_num", AppContext.getSharedPreferences().getInt("week_num", 0) + 1);
        }

        // 使用次数加1
        editor.putInt("day_count", AppContext.getSharedPreferences().getInt("day_count", 0) + 1);
        editor.putInt("week_count", AppContext.getSharedPreferences().getInt("week_count", 0) + 1);
        // 使用时间长度增加
        editor.putInt("day_time", AppContext.getSharedPreferences().getInt("day_time", 0) + timeCount);
        editor.putInt("week_time", AppContext.getSharedPreferences().getInt("week_time", 0) + timeCount);

        editor.commit();
    }

    /**
     *
     * @comment 根据包名将当前统计信息写入到数据库
     * 			在写入之前先判断数据库中是否有该应用，如果没有那么从系统中获取(该过程可能导致WatchdogService阻塞)
     * 			然后将获取的信息赋值给tempAppInfo。
     * 			如果有该应用(tempAppInfo的name 不为empty)那么直接使用tempAppInfo为对象插入到数据库
     *
     * @param @param pkgName
     * @return void
     * @throws
     * @date 2015-12-31 上午10:57:06
     */
    public void updateUseTime(String pkgName){
        AppUseStatics tempAppInfo = AppContext.mDBManager.queryByPkgName(pkgName);
        if("empty".equals(tempAppInfo.getName())){
            LogUtil.i(TAG, "未添加到数据库的新应用 ： " + pkgName);
            try {
                AppInfoProvider provider = new AppInfoProvider(getApplicationContext());
                PackageManager pm = getPackageManager();
                PackageInfo info = pm.getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
                AppUseStatics appStatics = provider.getAppFromPkgName(info, pm);
                appStatics.setUseFreq(tempAppInfo.getUseFreq() + 1);
                appStatics.setUseTime(tempAppInfo.getUseTime() + timeCount);
                appStatics.setWeight(tempAppInfo.getWeight() + timeCount + 1);
                AppContext.mDBManager.add(appStatics);
                LogUtil.i(TAG, "未添加到数据库的新应用 ： " + pkgName + " 信息添加成功");
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.e(TAG, "updateUseTime " + pkgName+" NameNotFoundException : " + e.toString());
                e.printStackTrace();
            }
        }else{
            tempAppInfo.setUseFreq(tempAppInfo.getUseFreq() + 1); // 计算使用次数
            tempAppInfo.setUseTime(tempAppInfo.getUseTime() + timeCount); // 计算使用时间
            tempAppInfo.setWeight(tempAppInfo.getWeight() + timeCount + 1); // 计算权重值
            if(AppContext.mDBManager.updateAppInfo(tempAppInfo) > 0){
                LogUtil.i(TAG, "更新 "+ pkgName +" 应用信息到数据库成功");
            }else{
                LogUtil.i(TAG, "更新 "+ pkgName +" 应用信息到数据库失败");
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//	/**
//	 * Service设置成START_STICKY，kill 后会被重启（等待5秒左右）
//	 * 重传Intent
//	 *
//	 * 由于后来通过解锁广播开启服务，所以就不必通过START_STICKY来保证服务不被系统关闭了
//	 *
//	 */
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		return START_STICKY;
//	}



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timeCount != 0){
            updateUseTime(prePackageName);
            updateSharedpreference(prePackageName);
            LogUtil.i(TAG, "WatchdogService onDestroy ： timeCount != 0");
        }
        LogUtil.i(TAG, "WatchdogService onDestroy");
    }

}

