package com.example.inspiron.phonesavior.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.utils.LogUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取系统中应用的使用信息
 * 
 * 获取原理是使用packageManager扫描所有安装在系统中的应用然后获取信息
 * 是一个比较耗时的工作，因此如果要调用这个provider获取应用信息的时候需要在子线程中执行
 */
public class AppInfoProvider {
	private static final String TAG = "AppInfoProvider";
	private Context context; // 用于获取pm
	private PackageManager pm;

	public AppInfoProvider(Context c) {
		this.context = c;
		pm = this.context.getPackageManager();
	}

	/**
	 * 获取已经安装的所有应用的信息，并返回信息列表
	 * 
	 * 根据SharedPreference的标志位，如果是第一次查询，那么直接在系统中遍历
	 * 如果不是第一次，那么直接从数据库进行查询即可
	 * 
	 * @return List<AppInfo> 应用信息列表,且按权重排序完成
	 */
	public List<AppUseStatics> getAllApps() {
		List<PackageInfo> pkgInfo = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
		List<AppUseStatics> result = new ArrayList<AppUseStatics>();

		for (PackageInfo info : pkgInfo) {
			result.add(getAppFromPkgName(info, pm));
			LogUtil.i(TAG, "getAllApps: " + info.packageName);
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * 
	 * @comment 根据PackageInfo获取该包对应的应用的信息，并将应用信息以AppUseStatics bean的形式返回
	 * 			默认统计数据都为零
	 * 
	 * @param @param info
	 * @param @return   
	 * @return AppUseStatics  
	 * @throws
	 * @date 2015-12-30 上午10:35:33
	 */
	public static AppUseStatics getAppFromPkgName(PackageInfo info, PackageManager pm) {
		AppUseStatics myApp = new AppUseStatics();
		// 获取包名
		String pkgName = info.packageName;
		// 根据包名获取应用信息
		ApplicationInfo appInfo = info.applicationInfo;
		Drawable icon = appInfo.loadIcon(pm);
		String appName = appInfo.loadLabel(pm).toString();
		
		myApp.setIcon(icon);
		myApp.setPkgName(pkgName);
		myApp.setName(appName);
		
		if (filterApp(appInfo)) { // 如果是第三方应用
			myApp.setSysApp(0);
			myApp.setWeight(1);
		} else { // 如果是系统应用
			myApp.setSysApp(1);
			myApp.setWeight(0);
		}
		myApp.setUseFreq(0);
		myApp.setUseTime(0);
		
		LogUtil.i(TAG, "getAllApps: " + myApp.getPkgName());
		return myApp;
	}

	/*
	 * 判断是否是第三方应用。如果是第三方应用，那么返回true否则返回false
	 */
	public static boolean filterApp(ApplicationInfo info) {
		// 如果系统应用更新之后也被视为是三方应用
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;
	}
}
