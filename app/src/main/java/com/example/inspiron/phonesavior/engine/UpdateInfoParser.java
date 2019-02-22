package com.example.inspiron.phonesavior.engine;

import android.util.Xml;

import com.example.inspiron.phonesavior.domain.UpdateInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * 解析服务器端获取的更新信息的xml文件
 * 
 */
public class UpdateInfoParser {

	/**
	 * 
	 * @param is
	 *            待解析的xml的inputstream，使用pull解析器解析
	 *            
	 * @return updateinfo
	 */
	public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		UpdateInfo info = new UpdateInfo();
		parser.setInput(is, "utf-8");
		int type = parser.getEventType();

		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if("version".equals(parser.getName())){
					String version = parser.nextText();
					info.setVersion(version);
				}else if("description".equals(parser.getName())){
					String description = parser.nextText();
					info.setDescription(description);
				}else if("apkurl".equals(parser.getName())){
					String apkurl = parser.nextText();
					info.setApkurl(apkurl);
				}
				
				break;

			}

			type = parser.next();
		}
		return info;
	}
}
