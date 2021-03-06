package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import android.widget.DatePicker;

public class Utils {

	public static String TAG = "failure_reporter";
	
	public static void log(String string) {
		Log.i(TAG, string);
	}
	
	public static void loge(String string) {
		Log.e(TAG, string);
	}
	
	public static String dateToString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String datetime = dateFormat.format(date);
		return datetime;
	}

	public static Date stringToDate(String dt) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try {
			date = format.parse(dt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static String getDateInString(DatePicker dp) {
		StringBuilder res = new StringBuilder();
		res.append(String.valueOf(dp.getYear())).append("-");
		res.append(String.valueOf(dp.getMonth()+1)).append("-");
		res.append(String.valueOf(dp.getDayOfMonth()));
		return dateToString(stringToDate(res.toString()));
	}
}
