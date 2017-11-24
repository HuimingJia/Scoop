package com.coop.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.util.NetworkChecker;

public class LoginActivity extends Activity implements View.OnClickListener {

	//private ImageView Logo;
	private Button Login_button_login;
	private Button Login_button_register;
	private EditText Login_edittext_account;
	private EditText login_edittext_password;
	private LinearLayout Login_layout_loginboard;
	private CustomToast CustomToast=new CustomToast();
	
	private ImageView icon_c;
	private ImageView icon_o1;
	private ImageView icon_o2;
	private ImageView icon_p;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Bmob.initialize(this, "dd8b3775403310d9f67e2a24808da6a4");
		initiate();
		initialanimation();
		addlistener();
	}

	public void initiate() {
		Login_button_login = (Button) findViewById(R.id.login_button_login);
		Login_button_register = (Button) findViewById(R.id.login_button_register);
		Login_edittext_account = (EditText) findViewById(R.id.login_edittext_account);

		login_edittext_password = (EditText) findViewById(R.id.login_edittext_password);		
		Login_layout_loginboard = (LinearLayout) findViewById(R.id.login_layout_loginboard);
		
		icon_c = (ImageView) findViewById(R.id.icon_c);
		icon_o1 = (ImageView) findViewById(R.id.icon_o1);
		icon_o2 = (ImageView) findViewById(R.id.icon_o2);
		icon_p = (ImageView) findViewById(R.id.icon_p);
	
	}
	/*
	 * initiate the animations of the login
	 */
	
	public void initialanimation() {
		TranslateAnimation translateAnimation_c = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
		translateAnimation_c.setDuration(1200);
		icon_c.startAnimation(translateAnimation_c);
		
		TranslateAnimation translateAnimation_o1 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, -0.6f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
		translateAnimation_o1.setDuration(1000);
		icon_o1.startAnimation(translateAnimation_o1);
		
		TranslateAnimation translateAnimation_o2 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.6f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
		translateAnimation_o2.setDuration(1000);
		icon_o2.startAnimation(translateAnimation_o2);
		
		TranslateAnimation translateAnimation_p = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
		translateAnimation_p.setDuration(1200);
		icon_p.startAnimation(translateAnimation_p);
	}
	
	public void addlistener() {
		Login_button_login.setOnClickListener(this);
		Login_button_register.setOnClickListener(this);

	}

	@SuppressLint("DefaultLocale")

	/*
	 * set the listenerEvent of login button and register button
	 */
	@Override
	public void onClick(View view) {
		switch(view.getId())
		{
			case R.id.login_button_login:
				login();
				break;
			case R.id.login_button_register:
				Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
				startActivity(intent);
				break;
				}
		}
	/*
	 * login function
	 */

	public void login()
	{
		NetworkChecker networkerchecker=new NetworkChecker(LoginActivity.this);
		
		final String mAccount = Login_edittext_account.getText().toString().toLowerCase()+"";
		final String mPassword = login_edittext_password.getText().toString()+"";

		/**
		 * verify account and password input by user
		 * if account or password equal to null, show the warn that "..."
		 * if there is not available network, show the warn that "..."
		 */
		
		if(mAccount.equals("") || mPassword.equals("")) {
			CustomToast.showCustomToast("Account can not be empty！",this);
		} else if(!networkerchecker.isNetworkAvailable()) {
			CustomToast.showCustomToast("No available Internet Connect right now",this);
		} else {
			/*
			 * if query can get the user by username, start to verify the state of "emailverified",only users get their email address verified can login
			 * if the user's email have been verified, start to verify password,if the password is correct,start to login
			 */

//			BmobQuery<BmobUser> query = new BmobQuery<BmobUser>();
//			query.addWhereEqualTo("username",mAccount);
//			query.findObjects()


			BmobUser user = new BmobUser();
			user.setUsername(mAccount);
			user.setPassword(mPassword);
			user.login(new SaveListener<BmobUser>() {
				@Override
				public void done(BmobUser bmobUser, BmobException e) {
					if (e == null) {
						CustomToast.showCustomToast("登录成功",LoginActivity.this);
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this,ScoopActivity.class);
						startActivity(intent);
					} else {
						CustomToast.showCustomToast(e.toString(), LoginActivity.this);
					}
				}
			});



//
//
//			query_user.findObjects(this,new FindListener<BmobUser>() {
//
//				@Override
//			    public void onSuccess(List<BmobUser> objects)
//				{
//					if(objects.size()==0)
//					{
//						CustomToast.showCustomToast("用户不存在！",LoginActivity.this);
//					}
//					else
//					{
//						if(	objects != null && objects.size() > 0 && objects.get(0).getEmailVerified() == false)
//						{
//							CustomToast.showCustomToast("请先进行邮箱验证Orz",LoginActivity.this);
//
//						}
//						else
//						{
//							CustomToast.showCustomToast("正待登录请稍后...",LoginActivity.this);
//							final BmobUser user = new BmobUser();
//							user.setUsername(mAccount);
//							user.setPassword(mPassword);
//							user.login(LoginActivity.this, new SaveListener() {
//								@Override
//								public void onSuccess() {
//									/*
//									 * success to login in, create an intent and start to change activity
//									 */
//									CustomToast.showCustomToast("登录成功",LoginActivity.this);
//									Intent intent = new Intent();
//									intent.setClass(LoginActivity.this,ScoopActivity.class);
//									startActivity(intent);
//								}
//
//								@Override
//								public void onFailure(int code, String msg) {
//									CustomToast.showCustomToast("登录失败,密码错误",LoginActivity.this);
//								}
//							});
//						}
//
//					}
//
//			    }
//
//				@Override
//				public void onError(int arg0, String arg1)
//				{
//					CustomToast.showCustomToast("未知错误！",LoginActivity.this);
//				}
//
//			});
		}		
	}
}
