package com.example.inspiron.phonesavior.utils;

public class CommonUtils {
	/**
	 * 输入秒数，将其转化为 *小时*分*秒的时长表示形式
	 * 
	 * 其中如果时间大于1小时，那么只以 *小时*分的形式展示
	 * 如果时间低于1小时但是大于1分钟，那么以 *分*秒的形式展示
	 * 如果使用时间低于1分钟 以*秒的形式展示
	 * 
	 * @param time
	 * @return
	 */
	public static String getFormatTime(int time) {
		int hour = 0;
		int minute = 0;
		StringBuilder result = new StringBuilder();
		
		if (time > 3600) {
			hour = time / 3600;
			time = time % 3600;
			result.append(hour + "小时");
			
			if (time > 60) {
				minute = time / 60;
				time = time % 60;
				result.append(minute + "分");
			}
		}else{
			if (time > 60) {
				minute = time / 60;
				time = time % 60;
				result.append(minute + "分");
				
				if(time > 0){
					result.append(time + "秒");
				}
			}else{
				result.append(time + "秒");
			}
		}
		return result.toString();
	}
	
	/**
	 * 将秒数转换为分钟数，不加单位
	 * 
	 * @param time
	 * @return
	 */
	public static int getFormatMinute(int time) {
		int minute = 0;
		if (time > 60) {
			minute = minute / 60 ;
		}
		return minute + 1; // 不足一分钟按一分钟计算
	}
	
	/**
	 * 
	 * @comment 绝对值计算
	 * @param @param value
	 * @param @return   
	 * @return float  
	 * @throws
	 * @date 2015-12-28 下午12:57:59
	 */
	public static float abs(float f){
		if(f > 0){
			return f;
		}else{
			return -f;
		}
	}

}
