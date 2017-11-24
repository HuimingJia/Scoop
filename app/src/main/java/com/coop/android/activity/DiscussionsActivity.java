package com.coop.android.activity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.bean.Discussion;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.fragment.AnsDiscussionFragment;
import com.coop.android.activity.fragment.HomeFragment;
import com.coop.android.activity.fragment.ProjectFileFragment;
import com.coop.android.activity.fragment.ProjectMemberFragment;
import com.coop.android.activity.fragment.ProjectTaskFragment;
import com.coop.android.activity.fragment.UnAnsDiscussionFragment;
import com.coop.android.activity.toast.CustomToast;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiscussionsActivity extends FragmentActivity implements
		OnClickListener, OnPageChangeListener, DialogInterface.OnClickListener {
	private CustomToast CustomToast = new CustomToast();

	private Discussion mDiscussion;
	private TextView tab_answered;
	private TextView tab_unanswered;

	private ViewPager viewpager;
	private List<Fragment> mFragments;
	private FragmentPagerAdapter mAdapter;

	Fragment answeredfragment;
	Fragment unansweredfragment;

	private Button mBackBtn;
	private Button mAddDisBtn;

	private Project mProject;
	private Task mTask;

	private ShortMessageDialog mAddDialog;

	/*
	 * the needed operation when create the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discussionslist);
		mProject = (Project) getIntent().getSerializableExtra("Project");
		mTask = (Task) getIntent().getSerializableExtra("Task");
		initiateViews();
		initiateEvents();
		resetImg();
		setSelect(1);
	}

	private void initiateViews() {
		viewpager = (ViewPager) findViewById(R.id.id_viewpager);

		mAddDialog = new ShortMessageDialog(DiscussionsActivity.this);
		mAddDialog.setTitle("Add Discussion");
		mAddDialog.setHint("Type Title");

		mBackBtn = (Button) findViewById(R.id.discussionslist_button_back);
		mBackBtn.setBackgroundResource(R.drawable.button_back);

		mAddDisBtn = (Button) findViewById(R.id.discussionslist_image_addiscussion);
		mAddDisBtn.setBackgroundResource(R.drawable.button_add);

		tab_answered = (TextView) findViewById(R.id.discussionslist_button_answeredlistbutton);
		tab_unanswered = (TextView) findViewById(R.id.discussionslist_button_unansweredlistbutton);

		mFragments = new ArrayList<Fragment>();
		answeredfragment = new AnsDiscussionFragment();
		unansweredfragment = new UnAnsDiscussionFragment();

		mFragments.add(unansweredfragment);
		mFragments.add(answeredfragment);
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragments.get(arg0);
			}
		};

		viewpager.setAdapter(mAdapter);
	}

	private void initiateEvents() {
		mBackBtn.setOnClickListener(this);
		mAddDisBtn.setOnClickListener(this);
		tab_answered.setOnClickListener(this);
		tab_unanswered.setOnClickListener(this);
		mAddDialog.setButton1("Sure", this);
		mAddDialog.setButton2("Cancel", this);

		viewpager.setOnPageChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.discussionslist_image_addiscussion:
			mAddDialog.show();
			break;
		case R.id.discussionslist_button_back:
			Intent intent_back = new Intent(DiscussionsActivity.this, TaskActivity.class);
			intent_back.putExtra("Project", mProject);
			intent_back.putExtra("Task", mTask);
			startActivity(intent_back);
			finish();
			break;
		case R.id.discussionslist_button_unansweredlistbutton:
			resetImg();
			setSelect(1);
			break;
		case R.id.discussionslist_button_answeredlistbutton:
			resetImg();
			setSelect(2);
			break;
		}
	}

	private void setSelect(int i) {
		if (i == 1) {
			tab_answered.setBackgroundResource(R.drawable.transpant);
		} else if (i == 2) {
			tab_unanswered.setBackgroundResource(R.drawable.transpant);
		}

		viewpager.setCurrentItem(i - 1);
	}

	private void resetImg() {
		tab_answered.setBackgroundResource(R.drawable.problemslist_image_cuttingline2_0);
		tab_unanswered.setBackgroundResource(R.drawable.problemslist_image_cuttingline2_0);
	}

	private void setTab(int i) {
		resetImg();
		switch (i) {
		case 0:
			tab_answered.setBackgroundResource(R.drawable.transpant);
			break;
		case 1:
			tab_unanswered.setBackgroundResource(R.drawable.transpant);
			break;
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		int currentItem = viewpager.getCurrentItem();
		setTab(currentItem);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1) {
			dialog.cancel();
		} else {
			if (dialog == mAddDialog) {
				String title = mAddDialog.getText();
				if (title == null) {
					mAddDialog.requestFocus();
					CustomToast.showCustomToast("请输入讨论主题",
							DiscussionsActivity.this);
				} else {
					mAddDialog.dismiss();
					addDiscussion(title);
					mAddDialog.setTextNull();
				}
			}
		}

	}

	private void addDiscussion(String title) {
		mDiscussion = new Discussion();
		mDiscussion.setState(0);
		mDiscussion.setTile(title);
		User user = (User) BmobUser.getCurrentUser(User.class);
		mDiscussion.setUser(user);
		mDiscussion.setName(user.getName());
		mDiscussion.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e == null) {
					BmobRelation mDiscussions = new BmobRelation();
					mDiscussions.add(mDiscussion);
					mTask.setDiscussions(mDiscussions);
					mTask.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								((UnAnsDiscussionFragment) unansweredfragment).UpdateList(mDiscussion);
								((UnAnsDiscussionFragment) unansweredfragment).AdapterNotify();
								CustomToast.showCustomToast("Topic Creation Finished", DiscussionsActivity.this);
							} else {
								CustomToast.showCustomToast("Topic Creation Failed ", DiscussionsActivity.this);
							}
						}
					});
				} else {
					CustomToast.showCustomToast("Topic Creation Failed : " + e.toString(), DiscussionsActivity.this);
				}
			}
		});
	}
}
