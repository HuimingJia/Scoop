package com.coop.android.activity.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

public class DateUtil {
	

	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static String getDateFormarted() {
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	public static long getDate(String dateString) throws ParseException {
		return sdf.parse(dateString).getTime();
	}
	/**
	 * 将时间（毫秒）转换为对应的字符串（天-小时-分-秒
	 * 
	 * @param time
	 */
	
	public StringBuilder getFinishTime(long time)
	{
		StringBuilder stringBuilder = new StringBuilder();
		
		long diffSeconds = time / 1000 % 60;
		long diffMinutes = time / (60 * 1000) % 60;
		long diffHours = time 	/ (60 * 60 * 1000) % 24;
		long diffDays = time 	/ (24 * 60 * 60 * 1000);
		if (diffDays != 0)
			stringBuilder.append(diffDays + "天");
		if (diffHours != 0)
			stringBuilder.append(diffHours + "小时");
		if (diffMinutes != 0)
			stringBuilder.append(diffMinutes + "分");
		if (diffSeconds != 0)
			stringBuilder.append(diffSeconds + "秒");
		
		return stringBuilder;
	}
}
