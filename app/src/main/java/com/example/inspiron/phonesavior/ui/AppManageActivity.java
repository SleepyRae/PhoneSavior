package com.example.inspiron.phonesavior.ui;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.service.WatchdogService;
import com.example.inspiron.phonesavior.utils.CommonUtils;
import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.utils.StringUtils;
import com.example.inspiron.phonesavior.view.ListViewCompat;
import com.example.inspiron.phonesavior.view.SlideView;
import com.example.inspiron.phonesavior.view.SlideView.OnSlideListener;
import com.example.inspiron.phonesavior.R;

/**
 * 管理应用：主要功能是列出自己的所有应用并提供侧滑卸载，打开 三个点击事件
 * 
 * 侧滑实现参考讲解示例：Baidu Android高级工程师任玉刚的博客
 * http://blog.csdn.net/singwhatiwanna/article/details/17515543#comments 
 * 
 * 侧滑ListView存在的问题是加载特别卡顿。在后台获取所有数据之后，填充到显示的过程很长
 * 
 * 目前ListViewCpmpact存在一个
 * setTag和getTag为什么没有效果的问题
 * 
 */
public class AppManageActivity extends Activity implements OnItemClickListener,
		OnClickListener, OnSlideListener {

	private static final String TAG = "AppManageActivity";

	private static int clicked_item_position = 0;
	private static String clicked_item_pkgname = "";

	private final static int GET_ALLAPP_FINISH = 1;
	private ListViewCompat lvc_appinfo;
	private LinearLayout ll_appinfo;

	private SlideView mLastSlideViewWithStatusOn; // 侧滑view

	private List<AppUseStatics> infos; // 数据实体
	private AppManagerAdapter adapter; // 显示数据实体的适配器

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_ALLAPP_FINISH:
				adapter = new AppManagerAdapter();
				lvc_appinfo.setAdapter(adapter);
				ll_appinfo.setVisibility(View.INVISIBLE);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 取消标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_appmanage);

		lvc_appinfo = (ListViewCompat) findViewById(R.id.lvc_appinfo);
		lvc_appinfo.setOnItemClickListener(this);

		// 进度条所在的LinearLayout
		ll_appinfo = (LinearLayout) findViewById(R.id.ll_appinfo);
		ll_appinfo.setVisibility(View.VISIBLE);

		Intent watchdogService = new Intent(AppManageActivity.this, WatchdogService.class);
		startService(watchdogService);

		// 开启子线程加载应用信息
		new Thread() {
			public void run() {
				boolean isFirst = AppContext.getSharedPreferences().getBoolean("isFirst", true);
				// 若是第一次使用，那么遍历系统安装的应用信息
				/*
				 * 这里存在一个交互问题：在遍历并排序所有应用信息的过程是很漫长的，然而获取之后填充到ListView的时候却是一瞬间
				 * 给人感觉很不好，可不可以增量获取，分批次更新ListView？？
				 */
				if (isFirst) {
					infos = AppContext.mAppInfoProvider.getAllApps();
					// 按优先级对其递增排序
					Collections.sort(infos);
					AppContext.mDBManager.addAll(infos);
					Editor editor = AppContext.mSharedPreferences.edit();
					editor.putBoolean("isFirst", false);

					editor.commit();
				} else {
					// 否则就直接从数据库中查询信息
					infos = AppContext.mDBManager.findAll();
					Collections.sort(infos);
				}

				Message msg = new Message();
				msg.what = GET_ALLAPP_FINISH;
				handler.sendMessage(msg);
			};
		}.start();

	}

	@Override
	protected void onResume() {
		super.onResume();
		// 恢复可见的时候立刻更新数据
		if(infos != null){
			infos = AppContext.mDBManager.findAll();
			Collections.sort(infos);
			adapter.notifyDataSetChanged();
			LogUtil.i(TAG, "onResume ： 更新数据");
		}
	}
	public class AppManagerAdapter extends BaseAdapter {

		public AppManagerAdapter() {
			super();
		}

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public Object getItem(int position) {
			return infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			SlideView slideView = (SlideView) convertView;
			AppUseStatics info = infos.get(position);
			if (slideView == null) {
				View itemView = View.inflate(AppManageActivity.this, R.layout.activity_appmanage_appinfo_item, null);

				slideView = new SlideView(AppManageActivity.this);
				slideView.setContentView(itemView);

				holder = new ViewHolder(slideView);
				slideView.setOnSlideListener(AppManageActivity.this);
				slideView.setTag(holder);
			} else {
				holder = (ViewHolder) slideView.getTag();
			}

			// 设置滑动view，且默认收缩不显示
			AppUseStatics mAppUseStatics = infos.get(position);
			mAppUseStatics.setSlideView(slideView);
			mAppUseStatics.getSlideView().shrink();

			// 不知道会不会存在没有图标的情形，对于此类情况最好还是做一个是否为空的判断
			holder.icon.setImageDrawable(info.getIcon());
			holder.tv_appname.setText(info.getName());
			if (info.isSysApp() == 1) {
				holder.tv_issys.setText("系统应用");
			} else {
				holder.tv_issys.setText("第三方应用");
			}
			holder.tv_app_freq.setText(info.getUseFreq() + "次");
			holder.tv_app_time.setText(CommonUtils.getFormatTime(info.getUseTime()));

			//holder.deleteHolder.setOnClickListener(AppManageActivity.this);

			holder.tv_start = (TextView) holder.deleteHolder.findViewById(R.id.tv_merge_start);
			holder.tv_delete = (TextView) holder.deleteHolder.findViewById(R.id.tv_merge_delete);
			holder.tv_start.setOnClickListener(AppManageActivity.this);
			holder.tv_delete.setOnClickListener(AppManageActivity.this);

			return slideView;
		}

		/**
		 * 实现ListView中条目不会超过一行，过长的部分截掉加上省略号
		 * 
		 * 后来发现TextView自身提供了这样的功能 android:singleLine="true"
		 * 显然自己此处的处理是不够精细的，对于中英文混合的情况，显示效果很不好
		 * 
		 * @param name
		 * @return
		 */
		private String getFormatAppName(String name) {
			if (StringUtils.isEnglish(name)) {
				if (name.length() > 18) {
					name = name.substring(0, 15) + "...";
				}
			} else {// 混合中英文的应用名，长度大于18
				if (name.length() > 18) {
					name = name.substring(0, 14);
					// 截取后如果是全英文，那么直接加省略号
					if (StringUtils.isEnglish(name)) {
						name += "...";
					} else { // 如果仍然是中英文混合，那么截取前10个混合字符加上省略号
						name = name.substring(0, 10) + "...";
					}
				}
			}
			return name;
		}

	}

	/**
	 * 
	 * @file AppManageActivity.java
	 * @package com.zp.quickaccess.ui 
	 * @comment 可复用的view
	 * @author zp
	 */
	private static class ViewHolder {
		public ImageView icon;
		public TextView tv_appname;
		public TextView tv_app_freq;
		public TextView tv_app_time;
		public TextView tv_issys;
		public ViewGroup deleteHolder;
		public TextView tv_start;
		public TextView tv_delete;

		ViewHolder(View view) {
			icon = (ImageView) view.findViewById(R.id.iv_appicon);
			tv_appname = (TextView) view.findViewById(R.id.tv_appname);
			tv_app_freq = (TextView) view.findViewById(R.id.tv_app_freq);
			tv_app_time = (TextView) view.findViewById(R.id.tv_app_time);
			tv_issys = (TextView) view.findViewById(R.id.tv_issys);
			deleteHolder = (ViewGroup) view.findViewById(R.id.holder);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		AppUseStatics info = (AppUseStatics) lvc_appinfo.getItemAtPosition(position);
		/*
		 * 此处设置的tag在onClick()中不一定总是可以获取
		 * 因为设置tag是在onItemClick()中而不是onTouch()中
		 * 因此可能出现用户直接滑动列表但是没有触发onItemClick()的点击事件
		 */
		TextView tv_merge_start = (TextView)findViewById(R.id.holder).findViewById(R.id.tv_merge_start);
		TextView tv_merge_delete = (TextView)findViewById(R.id.holder).findViewById(R.id.tv_merge_delete);
		tv_merge_delete.setTag(info.getPkgName());
		tv_merge_start.setTag(info.getPkgName());
		LogUtil.i(TAG, "tv_merge_start.getTag() : " + tv_merge_start.getTag());
		LogUtil.i(TAG, "tv_merge_delete.getTag() : " + tv_merge_delete.getTag());
		 
		 
		clicked_item_position = position;
		clicked_item_pkgname = info.getPkgName();
		LogUtil.i(TAG, "onItemClick package name = " + clicked_item_pkgname);
	}

	@Override
	public void onSlide(View view, int status) {
		// 滑动一次，此方法会被执行两次
		if (mLastSlideViewWithStatusOn != null && mLastSlideViewWithStatusOn != view) {
			mLastSlideViewWithStatusOn.shrink();
		}
		if (status == SLIDE_STATUS_ON) {
			mLastSlideViewWithStatusOn = (SlideView) view;
		}
	}

	/**
	 * 开启应用和卸载应用的点击响应事件
	 */
	@Override
	public void onClick(View v) {
		LogUtil.i(TAG, "被点击view的tag : " + v.getTag());
		AppUseStatics info = infos.get(clicked_item_position);
		String pkgName = info.getPkgName();

		if (v.getId() == R.id.tv_merge_delete) {
			if (info.isSysApp() == 1) {
				Toast.makeText(AppManageActivity.this, "系统应用不可以被卸载", 0).show();
				adapter.notifyDataSetChanged();
				LogUtil.i(TAG, "系统应用不可以被卸载" + pkgName);
			} else {
				// 卸载应用的逻辑;卸载成功之后记得去更新ListView以及更新数据库
				String uriStr = "package:" + pkgName;
				Uri deleteUri = Uri.parse(uriStr);
				Intent deleteIntent = new Intent();
				deleteIntent.setData(deleteUri);
				deleteIntent.setAction(Intent.ACTION_DELETE);
				startActivityForResult(deleteIntent, 0);
				LogUtil.i(TAG, "delete " + pkgName);
			}
		} else if (v.getId() == R.id.tv_merge_start) {
			LogUtil.i(TAG, "start " + pkgName);
			// 开启应用的逻辑：根据包名获取具有启动属性的activity；然后开启
			try {
				PackageInfo pkgInfo = getPackageManager().getPackageInfo(pkgName,
						PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_ACTIVITIES);
				ActivityInfo[] activityInfos = pkgInfo.activities;
				// 对于有些系统应用是没有启动属性的，所以添加判断避免崩溃
				if (activityInfos.length > 0) {
					adapter.notifyDataSetChanged();
					ActivityInfo startActivity = activityInfos[0];
					Intent intent = new Intent();
					intent.setClassName(pkgName, startActivity.name);
					startActivity(intent);
				} else {
					Toast.makeText(this, "应用程序无法启动", 0).show();
				}

			} catch (Exception e) {
				Toast.makeText(this, "应用程序无法找到或不允许被启动", 0).show();
				e.printStackTrace();
			}

		} else if (v.getId() == R.id.holder) {
			LogUtil.i(TAG, "holder " + v.toString());
		}
	}

	/**
	 * 对卸载事件的返回结果的判断：是否成功卸载
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		/*
		 * 此处卸载之后刷新列表，被卸载应用是否还存在列表中取决于不同的系统
		 * 模拟器以及华为手机上卸载之后列表中跟着移除
		 * 三星卸载成功后列表中仍然保留该条目
		 */
		adapter.notifyDataSetChanged(); // 刷新列表。比较保险的方式是先判断adapter是否为空
		/*
		// 返回始终是RESULT_CANCELED
		// 根据卸载提示框中用户的实际点击进行判断是否真的执行卸载操作了
		// 如果不进行判断，那么当用户点击卸载然后取消，由于没进行判断，应用也会被从列表中移除
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(AppManageActivity.this, "取消卸载", 0).show();
			adapter.notifyDataSetChanged(); // 刷新列表，复位
			LogUtil.i(TAG, clicked_item_pkgname + "取消卸载");
		} else if(resultCode == RESULT_OK){
			// 卸载之后就将数据从数组中删除，同时将其从数据库中删除
			AppUseStatics removeObject = AppContext.mDBManager
					.queryByPkgName(clicked_item_pkgname);
			AppContext.mDBManager.deleteByAppName(removeObject.getName());
			// 从数据库中获取更新后的应用信息
			infos = AppContext.mDBManager.findAll();
			// 从列表和数据库中移除之后通知ListView更新
			adapter.notifyDataSetChanged();
			Toast.makeText(AppManageActivity.this, "卸载成功", 0).show();
			LogUtil.i(TAG, clicked_item_pkgname + "被卸载");
		}else{
			Toast.makeText(AppManageActivity.this, "无法卸载", 0).show();
			LogUtil.i(TAG, clicked_item_pkgname + "无法卸载");
		}
		*/
	}
	
	private void uninstallApplication(){
		
	}
}
