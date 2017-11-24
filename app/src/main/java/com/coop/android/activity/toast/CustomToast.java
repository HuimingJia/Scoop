package com.coop.android.activity.toast;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.coop.android.activity.R;
import com.coop.android.activity.view.HandyTextView;

public class CustomToast {
	/**
	 * 自定义toast
	 * @param text 要显示的文本
	 * @param activity 显示该文本的activity
	 */
	public void showCustomToast(String text,Activity activity) {
		View toastRoot = LayoutInflater.from(activity).inflate(R.layout.toast_custom, null);
		((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.CENTER, 0, 600);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastRoot);
		toast.show();
	}
}