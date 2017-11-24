package com.coop.android.activity;

import java.util.ArrayList;
import java.util.List;

import com.coop.android.activity.bean.Project;
import com.coop.android.activity.fragment.ProjectFileFragment;
import com.coop.android.activity.fragment.ProjectMemberFragment;
import com.coop.android.activity.fragment.ProjectTaskFragment;

import android.content.Intent;
import android.graphics.Color;
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

public class ProjectActivity extends FragmentActivity implements
		OnClickListener, OnPageChangeListener {
	public static final String TASK_TAG = "com.srcoop.android.activity.fragment.ProjectTaskListFragment";

	private TextView mProjectName;

	private LinearLayout tab_task;
	private LinearLayout tab_file;
	private LinearLayout tab_member;

	private ImageButton button_task;
	private ImageButton button_file;
	private ImageButton button_member;

	private TextView text_task;
	private TextView text_file;
	private TextView text_member;

	private ViewPager viewpager;
	private List<Fragment> mFragments;
	private FragmentPagerAdapter mAdapter;

	private Button mBackBtn;
	private Button mAddTaskBtn;
	private Button mAddFileBtn;
	private Button mAddMembBtn;

	/*
	 * the needed operation when create the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project);
		initiateViews();
		initiateEvents();
		resetImg();
		setSelect(1);
	}

	private void initiateViews() {
		viewpager = (ViewPager) findViewById(R.id.id_viewpager);
		mAddTaskBtn = (Button) findViewById(R.id.btn_add_task);
		mAddFileBtn = (Button) findViewById(R.id.btn_add_file);
		mAddMembBtn = (Button) findViewById(R.id.btn_add_member);

		mBackBtn = (Button) findViewById(R.id.title_bar_left_back);
		mProjectName = (TextView) findViewById(R.id.project_activity_title);

		mAddTaskBtn.setBackgroundResource(R.drawable.button_add);
		mAddFileBtn.setBackgroundResource(R.drawable.button_add);
		mAddMembBtn.setBackgroundResource(R.drawable.button_add);
		mBackBtn.setBackgroundResource(R.drawable.button_back);

		button_task = (ImageButton) findViewById(R.id.Button_task);
		button_file = (ImageButton) findViewById(R.id.Button_file);
		button_member = (ImageButton) findViewById(R.id.Button_member);

		text_task = (TextView) findViewById(R.id.text_task);
		text_file = (TextView) findViewById(R.id.text_file);
		text_member = (TextView) findViewById(R.id.text_member);

		tab_task = (LinearLayout) findViewById(R.id.Tab_task);
		tab_file = (LinearLayout) findViewById(R.id.Tab_file);
		tab_member = (LinearLayout) findViewById(R.id.Tab_member);

		mFragments = new ArrayList<Fragment>();

		Fragment taskfragment = new ProjectTaskFragment();
		Fragment filefragment = new ProjectFileFragment();
		Fragment memberfragment = new ProjectMemberFragment();

		mFragments.add(taskfragment);
		mFragments.add(filefragment);
		mFragments.add(memberfragment);

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
		mProjectName.setOnClickListener(this);

		tab_task.setOnClickListener(this);
		tab_file.setOnClickListener(this);
		tab_member.setOnClickListener(this);

		viewpager.setOnPageChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_bar_left_back:
			Intent intent_back = new Intent(ProjectActivity.this,
					ScoopActivity.class);
			startActivity(intent_back);
			this.finish();
			break;
		case R.id.project_activity_title:
			Intent intent_Project = new Intent(ProjectActivity.this,
					ProjectDetailActivity.class);
			intent_Project.putExtra("Project", (Project) this.getIntent()
					.getSerializableExtra("Project"));
			startActivity(intent_Project);
			break;
		case R.id.Tab_task:
			resetImg();
			setSelect(1);
			break;
		case R.id.Tab_file:
			resetImg();
			setSelect(2);
			break;
		case R.id.Tab_member:
			resetImg();
			setSelect(3);
			break;

		}
	}

	private void setSelect(int i) {
		if (i == 1) {
			button_task.setBackgroundResource(R.drawable.scan_book);
			text_task.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(1);
		} else if (i == 2) {
			button_file.setBackgroundResource(R.drawable.scan_street);
			text_file.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(2);
		} else if (i == 3) {
			button_member.setBackgroundResource(R.drawable.scan_word);
			text_member.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(3);
		}
		viewpager.setCurrentItem(i - 1);
	}

	private void resetImg() {
		button_task.setBackgroundResource(R.drawable.scan_book_hl);
		button_file.setBackgroundResource(R.drawable.scan_street_hl);
		button_member.setBackgroundResource(R.drawable.scan_word_hl);

		text_task.setTextColor(Color.rgb(121, 90, 38));
		text_file.setTextColor(Color.rgb(121, 90, 38));
		text_member.setTextColor(Color.rgb(121, 90, 38));

	}

	private void setTab(int i) {
		resetImg();
		switch (i) {
		case 0:
			button_task.setBackgroundResource(R.drawable.scan_book);
			text_task.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(1);
			break;
		case 1:
			button_file.setBackgroundResource(R.drawable.scan_street);
			text_file.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(2);
			break;
		case 2:
			button_member.setBackgroundResource(R.drawable.scan_word);
			text_member.setTextColor(Color.rgb(207, 166, 85));
			setAddclickable(3);
			break;
		}
	}

	public void setAddclickable(int i) {
		if (i == 1) {
			mAddFileBtn.setVisibility(View.GONE);
			mAddMembBtn.setVisibility(View.GONE);
			mAddTaskBtn.setVisibility(View.VISIBLE);
			
			mAddTaskBtn.setClickable(true);
			mAddFileBtn.setClickable(false);
			mAddMembBtn.setClickable(false);
		} else if (i == 2) {
			
			mAddTaskBtn.setVisibility(View.GONE);
			mAddMembBtn.setVisibility(View.GONE);
			mAddFileBtn.setVisibility(View.VISIBLE);
			
			mAddTaskBtn.setClickable(false);
			mAddFileBtn.setClickable(true);
			mAddMembBtn.setClickable(false);
		} else if (i == 3) {
			
			mAddTaskBtn.setVisibility(View.GONE);
			mAddFileBtn.setVisibility(View.GONE);
			mAddMembBtn.setVisibility(View.VISIBLE);
			
			mAddTaskBtn.setClickable(false);
			mAddFileBtn.setClickable(false);
			mAddMembBtn.setClickable(true);
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
}
