package com.coop.android.activity.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.v3.BmobUser;

import com.coop.android.activity.LoginActivity;
import com.coop.android.activity.R;
import com.coop.android.activity.dialog.BaseDialog;

public class SettingFragment extends Fragment implements View.OnClickListener,DialogInterface.OnClickListener {
	private Button mLogoutBtn;
	private BaseDialog mBaseDialog;

	@SuppressLint("ResourceAsColor")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@SuppressLint("ResourceAsColor")
	private void initiateViews(View settingFragment)
	{
		((TextView) getActivity().findViewById(R.id.activity_title)).setText("Setting");
		Button button = ((Button) getActivity().findViewById(R.id.title_bar_right_menu));
		button.setBackgroundColor(R.color.transparent);
		button.setClickable(false);
		button.setOnClickListener(null);
		
		mLogoutBtn = (Button) settingFragment.findViewById(R.id.btn_logout);
		
		mBaseDialog=new BaseDialog(getActivity());
		mBaseDialog.setTitle("Message");
		mBaseDialog.setMessage("Are You Sure Log Out?");
	
	}
	
	private void initiateEvents()
	{
		mLogoutBtn.setOnClickListener(this);
		mBaseDialog.setButton1("确认",this);
		mBaseDialog.setButton2("取消",this);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{	
		View  settingFragment= inflater.inflate(R.layout.fragment_menuitem_setting,container, false);
		initiateViews(settingFragment);
		initiateEvents();
		return settingFragment;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mLogoutBtn)
		{
			mBaseDialog.show();
		}		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which==1)
			dialog.cancel();
		else {
			if(dialog==mBaseDialog) {
				// TODO Auto-generated method stub
				mBaseDialog.dismiss();
				BmobUser.logOut();
				Intent intent = new Intent(getActivity(),LoginActivity.class);
				getActivity().startActivity(intent);			
			}
		}
	}
}
