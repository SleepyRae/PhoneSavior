package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inspiron.phonesavior.R;
import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.service.WatchdogService;
import com.example.inspiron.phonesavior.utils.CommonUtils;
import com.example.inspiron.phonesavior.utils.LogUtil;

import java.util.Collections;
import java.util.List;

/**
 * 查看本机安装的应用的使用情况：开启次数，累计开启时间
 * 
 * 使用信息展示界面：包括使用频率最高的3款应用，当天使用次数，使用时长，使用个数
 * 
 * 那么问题来了：如何统计每天的数据以及如何组织这些数据
 */
public class ViewAppStaticsActivity extends Activity {
	private static final String TAG = "ViewAppStaticsActivity";

	private ImageView iv_usemost_icon_1;
	private ImageView iv_usemost_icon_2;
	private ImageView iv_usemost_icon_3;

	private TextView tv_usemost_freq_1;
	private TextView tv_usemost_freq_2;
	private TextView tv_usemost_freq_3;

	private TextView tv_usemost_time_1;
	private TextView tv_usemost_time_2;
	private TextView tv_usemost_time_3;

	private TextView tv_viewstatistics_day_count;
	private TextView tv_viewstatistics_day_time;
	private TextView tv_viewstatistics_day_num;
	private TextView tv_viewstatistics_week_count;
	private TextView tv_viewstatistics_week_time;
	private TextView tv_viewstatistics_week_num;

	private int day_count;
	private int day_time;
	private int day_num;
	private int week_count;
	private int week_time;
	private int week_num;

	private List<AppUseStatics> topThreeAppInfo;
	private List<AppUseStatics> infos;

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_viewstatics);

		initViews();

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				pd.dismiss();
				// 数据库初始化完毕之后开启服务
				Intent mWatchdogService = new Intent(ViewAppStaticsActivity.this, WatchdogService.class);
				startService(mWatchdogService);
				updateUI();
			}
		};
		
		boolean isFirst = AppContext.getSharedPreferences().getBoolean("isFirst", true);
		// 开启子线程加载应用信息
		/**
		 * 如果是第一次开启页面，那么说明数据库为空，WatchdogService也没有开启
		 * 
		 * 所以可以在本Activity或者APPManageActivity中添加数据到数据库并开启WatchdogService进行数据统计
		 * 
		 * 默认情况下并不开启服务进行统计
		 */
		if (isFirst) {
			pd = ProgressDialog.show(ViewAppStaticsActivity.this, "", "正在加载数据请稍候……", true, false);
			new Thread() {
				public void run() {
					infos = AppContext.mAppInfoProvider.getAllApps();
					// 按优先级对其递增排序
					Collections.sort(infos);
					AppContext.mDBManager.addAll(infos);
					Editor editor = AppContext.mSharedPreferences.edit();
					editor.putBoolean("isFirst", false);
					editor.commit();
					Message msg = new Message();
					handler.sendMessage(msg);
				};
			}.start();
		} else {
			// 否则直接更新UI
			updateUI();
		}

	}

	private void initViews() {
		tv_viewstatistics_day_count = (TextView) findViewById(R.id.tv_viewstatistics_day_count);
		tv_viewstatistics_day_time = (TextView) findViewById(R.id.tv_viewstatistics_day_time);
		tv_viewstatistics_day_num = (TextView) findViewById(R.id.tv_viewstatistics_day_num);
		tv_viewstatistics_week_count = (TextView) findViewById(R.id.tv_viewstatistics_week_count);
		tv_viewstatistics_week_time = (TextView) findViewById(R.id.tv_viewstatistics_week_time);
		tv_viewstatistics_week_num = (TextView) findViewById(R.id.tv_viewstatistics_week_num);

		iv_usemost_icon_1 = (ImageView) findViewById(R.id.iv_usemost_icon_1);
		iv_usemost_icon_2 = (ImageView) findViewById(R.id.iv_usemost_icon_2);
		iv_usemost_icon_3 = (ImageView) findViewById(R.id.iv_usemost_icon_3);

		tv_usemost_freq_1 = (TextView) findViewById(R.id.tv_usemost_freq_1);
		tv_usemost_freq_2 = (TextView) findViewById(R.id.tv_usemost_freq_2);
		tv_usemost_freq_3 = (TextView) findViewById(R.id.tv_usemost_freq_3);

		tv_usemost_time_1 = (TextView) findViewById(R.id.tv_usemost_time_1);
		tv_usemost_time_2 = (TextView) findViewById(R.id.tv_usemost_time_2);
		tv_usemost_time_3 = (TextView) findViewById(R.id.tv_usemost_time_3);
	}

	private void updateUI() {
		topThreeAppInfo = AppContext.mDBManager.findTopApp(3);

		day_count = AppContext.getSharedPreferences().getInt("day_count", 0);
		day_time = AppContext.getSharedPreferences().getInt("day_time", 0);
		day_num = AppContext.getSharedPreferences().getInt("day_num", 0);

		week_count = AppContext.getSharedPreferences().getInt("week_count", 0);
		week_time = AppContext.getSharedPreferences().getInt("week_time", 0);
		week_num = AppContext.getSharedPreferences().getInt("week_num", 0);

		// 要减去本应用自身，这是和统计服务的实现相关
		tv_viewstatistics_day_count.setText(day_count + "次");
		tv_viewstatistics_day_time.setText(CommonUtils.getFormatTime(day_time));
		tv_viewstatistics_day_num.setText(day_num + "个");

		tv_viewstatistics_week_count.setText(week_count + "次");
		tv_viewstatistics_week_time.setText(CommonUtils
				.getFormatTime(week_time));
		tv_viewstatistics_week_num.setText(week_num + "个");

		if (topThreeAppInfo.size() == 3) {
			LogUtil.i(TAG, topThreeAppInfo.get(0).getName() + " -- " + topThreeAppInfo.get(0).getUseFreq() + "次");
			LogUtil.i(TAG, topThreeAppInfo.get(1).getName() + " -- " + topThreeAppInfo.get(1).getUseFreq() + "次");
			LogUtil.i(TAG, topThreeAppInfo.get(2).getName() + " -- " + topThreeAppInfo.get(1).getUseFreq() + "次");

			iv_usemost_icon_1.setImageDrawable(topThreeAppInfo.get(0).getIcon());
			tv_usemost_freq_1.setText(topThreeAppInfo.get(0).getUseFreq() + "次");
			tv_usemost_time_1.setText(CommonUtils.getFormatTime(topThreeAppInfo.get(0).getUseTime()));

			iv_usemost_icon_2.setImageDrawable(topThreeAppInfo.get(1).getIcon());
			tv_usemost_freq_2.setText(topThreeAppInfo.get(1).getUseFreq() + "次");
			tv_usemost_time_2.setText(CommonUtils.getFormatTime(topThreeAppInfo.get(1).getUseTime()));

			iv_usemost_icon_3.setImageDrawable(topThreeAppInfo.get(2).getIcon());
			tv_usemost_freq_3.setText(topThreeAppInfo.get(2).getUseFreq() + "次");
			tv_usemost_time_3.setText(CommonUtils.getFormatTime(topThreeAppInfo.get(2).getUseTime()));
		}
	}
}
