package com.example.inspiron.phonesavior.engine;

import android.content.Context;

import com.example.inspiron.phonesavior.domain.UpdateInfo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 访问服务器获取更新信息
 * 
 */
public class UpdateInfoService {

	private Context context;

	public UpdateInfoService(Context context) {
		this.context = context;
	}

	/**
	 * 
	 * @param urlid
	 *            服务器路径string对应的id
	 * @return 更新的信息
	 */
	public UpdateInfo getUpdataInfo(int urlid) throws Exception {
		String path = context.getResources().getString(urlid);
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000); // 连接超时5秒
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		return UpdateInfoParser.getUpdataInfo(is);
	}
}
