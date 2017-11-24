package com.coop.android.activity.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.coop.android.activity.R;

public class ListDialog extends BaseDialog{
	private ListView mListView;

	public ListDialog(Context context) {
		super(context);
		setDialogContentView(R.layout.dialog_list);
		mListView = (ListView) findViewById(R.id.dialog_simplelist_list);
	}

	public void setButton(CharSequence text1,
			DialogInterface.OnClickListener listener1, CharSequence text2,
			DialogInterface.OnClickListener listener2) {
		super.setButton1(text1, listener1);
		super.setButton2(text2, listener2);
	}
	
	public ListView getListView()
	{
		return mListView;
	}
}
