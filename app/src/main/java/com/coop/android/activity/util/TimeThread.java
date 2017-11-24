package com.coop.android.activity.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * 自定义线程类且在指定的TextView上动态更新当前时间
 * 
 * @author SS
 * 
 * @param <Token>
 */
public class TimeThread<Token> extends HandlerThread {

	private static final String TAG = "com.srcoop.android.activity.util.TimeThread";
	private static final int TIME_RUN = 0;

	private Handler mHandler;
	private Handler mResponseHandler;

	Map<Token, Integer> requestMap = Collections
			.synchronizedMap(new HashMap<Token, Integer>());

	private Listener<Token> mListener;

	public interface Listener<Token> {
		void onTimeRunned(int id, String date);
	}

	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}

	public TimeThread(Handler handler) {
		super(TAG);
		mResponseHandler = handler;
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == TIME_RUN) {
					@SuppressWarnings("unchecked")
					Token token = (Token) msg.obj;
					handleTime(token);
				}
			}
		};
	}

	private void handleTime(final Token token) {
		while (true) {
			try {
				if (requestMap == null || token == null)
					return;
				if (requestMap.get(token) == null)
					return;
				final int id = requestMap.get(token);
				mResponseHandler.post(new Runnable() {

					@Override
					public void run() {
						String date = DateUtil.getDateFormarted();
						mListener.onTimeRunned(id, date);
					}
				});
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void onTimeRunner(Token token, int id) {
		requestMap.put(token, id);
		mHandler.obtainMessage(TIME_RUN, token).sendToTarget();
	}

	public void clearTime() {
		mResponseHandler.removeMessages(TIME_RUN);
		requestMap.clear();
	}
}
