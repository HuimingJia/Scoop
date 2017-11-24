package com.coop.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import cn.bmob.v3.BmobUser;

import com.coop.android.activity.bean.CoopBmobInstallation;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.fragment.HomeFragment;
import com.coop.android.activity.fragment.NoticeFragment;
import com.coop.android.activity.fragment.ProfileFragment;
import com.coop.android.activity.fragment.SettingFragment;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.CircleImageView;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class ScoopActivity extends FragmentActivity implements View.OnClickListener,ResideMenu.OnMenuListener
{
	private CustomToast CustomToast=new CustomToast();
	private Button Menu;
	
	private ResideMenu mResideMenu;
	private ResideMenuItem itemProfile;
	private ResideMenuItem itemHome;
	private ResideMenuItem itemNotice;
	private ResideMenuItem itemSetting;

	private ProfileFragment mProfileFragment;
	private NoticeFragment mNoticeFragment;
	private SettingFragment mSettingFragment;
	
	private User mUser;
	private long mExitTime = 0;

	private CircleImageView imageView;
	private CoopBmobInstallation mbmobInstallation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUser = BmobUser.getCurrentUser(User.class);
		setContentView(R.layout.activity_srcoop);
//		initiateInstallaiton();
		initiateViews();
		initiateEvents();
		if (savedInstanceState == null) {
			changeFragment(new HomeFragment());
		}
	}
	
//	private void initiateInstallaiton() {
//		mbmobInstallation = new CoopBmobInstallation();
//		BmobQuery<CoopBmobInstallation> query = new BmobQuery<CoopBmobInstallation>();
//		query.addWhereEqualTo("installationId", mbmobInstallation.getInstallationId());
//		query.findObjects(new FindListener<CoopBmobInstallation>() {
//			@Override
//			public void done(List<CoopBmobInstallation> list, BmobException e) {
//				if (e == null) {
//					if (!list.isEmpty()) {
//						list.get(0).delete(new DeleteListener() {
//							@Override
//							public void onSuccess() {
//
//							}
//
//							@Override
//							public void onFailure(int i, String s) {
//
//							}
//						})
//					}
//				} else {
//
//				}
//			}
//		});
//
//		query.findObjects(this, new FindListener<CoopBmobInstallation>() {
//
//			@Override
//			public void onSuccess(List<CoopBmobInstallation> installations) {
//				if(installations.size()!=0)
//				{
//					installations.get(0).delete(ScoopActivity.this, new DeleteListener() {
//					@Override
//					public void onFailure(int arg0, String arg1) {
//						// TODO Auto-generated method stub
//					}
//
//					@Override
//					public void onSuccess() {
//						// TODO Auto-generated method stub
//					}
//				});
//				}
//			}
//
//			@Override
//			public void onError(int arg0, String arg1) {}
//		});
//
//		mbmobInstallation = new CoopBmobInstallation();
//		mbmobInstallation.setEmail(mUser.getEmail().toString());
//		mbmobInstallation.setObjectID(mUser.getObjectId().toString());
//		mbmobInstallation.save(new SaveListener<String>() {
//			@Override
//			public void done(String s, BmobException e) {
//
//			}
//		});
//		BmobPush.startWork(this);
//
//
//	}
	private void initiateViews() {			
		mResideMenu = new ResideMenu(this);
		mResideMenu.setBackground(R.drawable.menu_background);
		mResideMenu.attachToActivity(this);
		mResideMenu.setScaleValue(0.63f);
		itemProfile = new ResideMenuItem(this);
		itemProfile.setTitle("Resources");

		itemHome = new ResideMenuItem(this, R.drawable.icon_home, "Home");
		itemNotice = new ResideMenuItem(this, R.drawable.icon_notice, "Notification");
		itemSetting = new ResideMenuItem(this, R.drawable.icon_settings, "Setting");

		mResideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
		mResideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
		mResideMenu.addMenuItem(itemNotice, ResideMenu.DIRECTION_LEFT);
		mResideMenu.addMenuItem(itemSetting, ResideMenu.DIRECTION_LEFT);

		mProfileFragment = new ProfileFragment();
		mNoticeFragment = new NoticeFragment();
		mSettingFragment = new SettingFragment();

		mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
		Menu=(Button)findViewById(R.id.title_bar_left_menu);
		
		imageView = (CircleImageView) itemProfile.findViewById(R.id.iv_icon);
		LayoutParams params = imageView.getLayoutParams();
		params.width *= 1.8;
		params.height *= 1.8;
		imageView.setLayoutParams(params);
		if (mUser.getPhoto() != null) {
//			BmobFile photo = mUser.getPhoto().;
//			imageView.setImageURI();
//			imageView.setImageResource(photo.);
		} else {
//			imageView.setImage
			imageView.setImageResource(R.drawable.profile_photo);
		}
	}
	
	private void initiateEvents()
	{
		mResideMenu.setMenuListener(this);
		itemProfile.setOnClickListener(this);
		itemHome.setOnClickListener(this);
		itemNotice.setOnClickListener(this);
		itemSetting.setOnClickListener(this);
		Menu.setOnClickListener(this);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return mResideMenu.dispatchTouchEvent(ev);
	}

	@Override
	public void onClick(View v) {	
		if (v == itemProfile) {
			changeFragment(mProfileFragment);
			mResideMenu.closeMenu();
		} else if (v == itemHome) {
			changeFragment(new HomeFragment());
			mResideMenu.closeMenu();
		} else if (v == itemNotice) {
			changeFragment(mNoticeFragment);
			mResideMenu.closeMenu();
		} else if (v == itemSetting) {
			changeFragment(mSettingFragment);
			mResideMenu.closeMenu();
		} else if (v == Menu) {
			mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
		}
	}

	private void changeFragment(Fragment targetFragment) {
		mResideMenu.clearIgnoredViewList();
		getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, targetFragment, "fragment")
				.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				CustomToast.showCustomToast("再按一次退出",ScoopActivity.this);
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void openMenu() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void closeMenu() {
		// TODO Auto-generated method stub		
	}
}
