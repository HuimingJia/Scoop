package com.coop.android.activity;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.toast.CustomToast;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ReactionToAddActivity extends Activity implements View.OnClickListener,DialogInterface.OnClickListener{
	private Button mEditBtn; // 编辑按钮
	private Button mBackBtn;
	private Button mDelBtn;
	private Project mProject;
	
	private EditText mTitleText;
	private EditText mDepictText;
	private EditText mTimeText;
	
	private BaseDialog mConfirmDialog;
	private BaseDialog mConfDelDialog;
	private CustomToast CustomToast=new CustomToast();

	private String title;
	private String decipt;
	private boolean iEditOrSave = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mProject = (Project) this.getIntent().getSerializableExtra("Project");
		this.setContentView(R.layout.activity_projectdetail);
		initiateViews();
		initiateEvents();		
	}
	
	private void initiateViews() {
		mEditBtn = (Button) findViewById(R.id.project_display_edit_w);
		mBackBtn = (Button) findViewById(R.id.project_display_back_w);
		mDelBtn = (Button) findViewById(R.id.btn_delete);
		
		mTitleText = (EditText) findViewById(R.id.project_title);
		mDepictText = (EditText) findViewById(R.id.project_depict);
		mTimeText = (EditText) findViewById(R.id.project_time);
		
		
		if (mProject.getProjectName() == null) {
			mTitleText.setText("UNSET");
		} else {
			mTitleText.setText(mProject.getProjectName());
		}

		if(mProject.getProjectContent()==null) {
			mDepictText.setText("UNSET");
		} else {
			mDepictText.setText(mProject.getProjectContent());
		}
		
		title = mTitleText.getText().toString();
		decipt = mDepictText.getText().toString();
		
		mTimeText.setText(mProject.getCreatedAt() + "");
		
		mConfirmDialog = new BaseDialog(this);
		mConfirmDialog.setTitle("Confirm Update");
		mConfirmDialog.setMessage("Are You Sure Update Information?");

		mConfDelDialog = new BaseDialog(this);
		if (BmobUser.getCurrentUser(User.class).getObjectId().equals(mProject.getLeader().getObjectId())) {
			mDelBtn.setText("Delete Project");
			mConfDelDialog.setTitle("Confirm Delete?");
			mConfDelDialog.setMessage("Are You Sure Delete Project?");
		} else {
			mDelBtn.setText("Drop Project");
			mConfDelDialog.setTitle("Confirm Drop?");
			mConfDelDialog.setMessage("Are You Sure Drop Project?");
		}		
	}
	
	private void initiateEvents() {
		mEditBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mDelBtn.setOnClickListener(this);
		
		mConfirmDialog.setButton1("Sure",this);
		mConfirmDialog.setButton2("Cancel",this);
		mConfDelDialog.setButton1("Sure",this);
		mConfDelDialog.setButton2("Cancel",this);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mBackBtn) {
			Intent intent = new Intent(this,ProjectActivity.class);
			intent.putExtra("Project", mProject);
			startActivity(intent);	
			this.finish();
		} else if (v == mDelBtn) {
			mConfDelDialog.show();
		} else if (v == mEditBtn) {
			if (!iEditOrSave) {
				if(BmobUser.getCurrentUser(User.class).getObjectId().equals(mProject.getLeader().getObjectId())) {
					mEditBtn.setBackgroundResource(R.drawable.button_confirm);
					mTitleText.setEnabled(true);				
					mTitleText.setTextColor(Color.rgb(123,123,123));
					mDepictText.setEnabled( true);
					mDepictText.setTextColor(Color.rgb(123,123,123));
					iEditOrSave = !iEditOrSave;
				} else {
					CustomToast.showCustomToast("You Do Not Have Authority To Operate",this);
				}
			} else {
				if ((!mTitleText.getText().toString().equals(title)) || (!mDepictText.getText().toString().equals(decipt)))
					mConfirmDialog.show();
				else {
					if(mProject.getProjectName()==null) {
						mTitleText.setText("UNSET");
					} else {
						mTitleText.setText(mProject.getProjectName());
					}
					
					if(mProject.getProjectContent()==null) {
						mDepictText.setText("UNSET");
					} else {
						mDepictText.setText(mProject.getProjectContent());
					}
					mEditBtn.setBackgroundResource(R.drawable.button_edit);
					mTitleText.setEnabled(false);
					mTitleText.setTextColor(Color.rgb(41,55,70));
					mDepictText.setEnabled(false);
					mDepictText.setTextColor(Color.rgb(41,55,70));
				}
				iEditOrSave = !iEditOrSave;
			}		
		}		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1) {
			dialog.cancel();
		} else {
			if (dialog == mConfirmDialog) {
				 SaveModification();
				 dialog.cancel();
					mEditBtn.setBackgroundResource(R.drawable.button_edit);
					mTitleText.setEnabled(false);
					mTitleText.setTextColor(Color.rgb(41,55,70));
					mDepictText.setEnabled(false);
					mDepictText.setTextColor(Color.rgb(41,55,70));
			} else {
				if(BmobUser.getCurrentUser(User.class).getObjectId().equals(mProject.getLeader().getObjectId())) {
					DismissProject();
				} else {
					BackoutProject();
				}
			}
		}
	}
	
	private void DismissProject() {
		mProject.delete(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Delete Project Finished",ReactionToAddActivity.this);
				}
			}
		});

		BmobRelation membersProject = new BmobRelation();
		membersProject.remove(mProject);
		BmobUser.getCurrentUser(User.class).setProjects_L(membersProject);
		BmobUser.getCurrentUser(User.class).update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Delete Project Finished", ReactionToAddActivity.this);
					Intent intent = new Intent(ReactionToAddActivity.this,ScoopActivity.class);
					startActivity(intent);
					ReactionToAddActivity.this.finish();
				}
			}
		});
	}
	
	private void BackoutProject() {
		mConfDelDialog.dismiss();
		BmobQuery<User_Project> query = new BmobQuery<User_Project>();
		query.addWhereEqualTo("mProject",mProject.getObjectId().toString());
		query.addWhereEqualTo("mEmail", BmobUser.getCurrentUser(User.class).getEmail());
		query.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e == null) {
					list.get(0).delete(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								CustomToast.showCustomToast("Drop Out Finished",ReactionToAddActivity.this);
								Intent intent = new Intent(ReactionToAddActivity.this,ScoopActivity.class);
								startActivity(intent);
								ReactionToAddActivity.this.finish();
							} else {
								CustomToast.showCustomToast("Drop Out Failed", ReactionToAddActivity.this);
							}
						}
					});
				} else {
					CustomToast.showCustomToast("Can Not Get RelationShip！",ReactionToAddActivity.this);
				}
			}
		});
	}
	
	
	private void SaveModification() {
		title = mTitleText.getText().toString();
		decipt = mDepictText.getText().toString();

		if (title == null) {
			title="UNSET";
		}
		if (decipt == null) {
			decipt="UNSET";
		}

		/*
		 * use regex match to verify the correctness of format of input telephone number
		 * if the format of in put email address is not meet the standard of normal telephone numer,only save the name
		 * otherwise save both
		 */
		mProject.setProjectName(title);
		mProject.setProjectContent(decipt);
		mProject.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Update Finished",ReactionToAddActivity.this);
				} else {
					CustomToast.showCustomToast("Update Failed",ReactionToAddActivity.this);
				}
			}
		});
		/*
		 * finish the modification and reset the statue of the button
		 */

		mEditBtn.setBackgroundResource(R.drawable.button_edit);

		mTitleText.setEnabled(false);
		mDepictText.setEnabled(false);

		mTitleText.setTextColor(Color.rgb(41,55,70));
		mDepictText.setTextColor(Color.rgb(41,55,70));
	
	}

}
