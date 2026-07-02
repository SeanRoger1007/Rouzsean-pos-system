package com.refresh.pos.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A static class, global access for how to handle with date format.
 */
public class DateTimeStrategy {
	
	private static Locale locale;
	private static String customFormat = "yyyy-MM-dd HH:mm:ss";
	private static final TimeZone PH_TIMEZONE = TimeZone.getTimeZone("Asia/Manila");
	
	private DateTimeStrategy() {}
	
	public static void setLocale(String lang, String reg) {
		locale = new Locale(lang, reg);
	}

	public static void setCustomFormat(String format) {
		if (format != null && !format.isEmpty()) {
			customFormat = format;
		}
	}
	
	public static String format(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat(customFormat, locale);
		sdf.setTimeZone(PH_TIMEZONE);
		return sdf.format(Calendar.getInstance(PH_TIMEZONE).getTime());
	}
	
	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
		sdf.setTimeZone(PH_TIMEZONE);
		return sdf.format(Calendar.getInstance(PH_TIMEZONE).getTime());
	}
	
	public static String getSQLDateFormat(Calendar instance) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", locale);
		sdf.setTimeZone(PH_TIMEZONE);
		return sdf.format(instance.getTime());
	}

	public static String formatToAmPm(String dateTimeStr) {
		try {
			SimpleDateFormat sdf24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
			sdf24.setTimeZone(PH_TIMEZONE);
			java.util.Date date = sdf24.parse(dateTimeStr);
			
			SimpleDateFormat sdf12 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", locale);
			sdf12.setTimeZone(PH_TIMEZONE);
			return sdf12.format(date);
		} catch (Exception e) {
			return dateTimeStr;
		}
	}
}
