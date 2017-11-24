package com.coop.android.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Reference;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.util.DownUtil;
import com.coop.android.activity.util.FileUtil;

public class ReferenceDetailActivity extends FragmentActivity implements View.OnClickListener, OnClickListener  {

	private Button mEditBtn; // 编辑按钮
	private Button mBackBtn;
	private Button mDownLoadBtn;
	
	private EditText mReferenceTitle;
	private EditText mReferenceDescription;
	
	private TextView mReferenceOwner;
	private TextView mReferenceUpTime;
	// 文件路径选择需要用到跳转--用Bundle传值方法
	@SuppressWarnings("unused")
	private Bundle fileBundle;
	private String filePath;
	private String downloadDir;
	
	private BaseDialog mEditDialog;
	private BmobFile mBmobFile;
	private Reference mReference;
	private Task mTask;
	private Project mProject;
	private CustomToast CustomToast=new CustomToast();

	private LoadingDialog mFlippingLoadingDialog;
	@SuppressWarnings("unused")
	private LoadingDialog mDownloadFlippingLoadingDialog;

	// true表示当前状态是编辑状态 右上角图标此时应为Project_edit_button_confirm.png
	// false表示当前状态为默认状态即不可编辑， 右上角图标应此时为Project_display_button_edit
	private int EditBtnStatue=0;
	//0,button is uneditable
	//1,button is editable
	private DownUtil downUtil;
	
	private void initiateViews()
	{
		mEditDialog =new BaseDialog(ReferenceDetailActivity.this);
		mEditDialog.setTitle("提示");
		mEditDialog.setMessage("确认修改？");
		
		mEditBtn 	 = (Button) findViewById(R.id.referencedetial_button_edit);	
		mDownLoadBtn = (Button) findViewById(R.id.referencedetial_button_download);
		mBackBtn	 = (Button) findViewById(R.id.referencedetial_button_back);

		mReferenceTitle 	  = (EditText) findViewById(R.id.referencedetial_editview_name);
		mReferenceDescription = (EditText) findViewById(R.id.referencedetial_editview_description);
		mReferenceOwner 	  = (TextView) findViewById(R.id.referencedetial_textview_owner);
		mReferenceUpTime	  = (TextView) findViewById(R.id.referencedetial_textview_time);
		
		 mReferenceTitle.setText(mReference.getFileName());;
		 mReferenceDescription.setText(mReference.getDescription());;
		
		 mReferenceOwner.setText(mReference.getName());
		 mReferenceUpTime.setText(mReference.getCreatedAt());
		
		mFlippingLoadingDialog = new LoadingDialog(this, "保存修改中....");
		mDownloadFlippingLoadingDialog = new LoadingDialog(this,"文件下载中....");	
	}

	
	private void initiateEvents()
	{
		mBackBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
		mDownLoadBtn.setOnClickListener(this);
		
		mEditDialog.setButton1("确定",this);
		mEditDialog.setButton2("取消",this);
		
	}
	
	
	@SuppressLint("HandlerLeak") @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mProject = (Project)getIntent().getSerializableExtra("Project");
		mTask = (Task)getIntent().getSerializableExtra("Task");
		mReference= (Reference)getIntent().getSerializableExtra("Reference");
		this.setContentView(R.layout.activity_referencedetial);

		initiateViews();
		initiateEvents();
	}
	
	private class DownloadThread extends AsyncTask<Void, Integer, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{super.onPreExecute();
		mDownloadFlippingLoadingDialog.show();}

		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			try
			{
				downUtil.download();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			CustomToast.showCustomToast("下载完成,保存路径为"+downloadDir,ReferenceDetailActivity.this);
		}
	}

	/*
	 * 判断文件路径路径是否存在
	 */

	
	private void SaveModification()
	{
		mEditBtn.setBackgroundResource(R.drawable.project_display_button_edit);
		mReference.setDescription(mReferenceDescription.getText().toString());		
		mReference.setFileName(mReferenceTitle.getText().toString());
		mReference.update(this.mReference.getObjectId(), new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("修改保存成功",ReferenceDetailActivity.this);
				} else {
					CustomToast.showCustomToast("修改保存失败，请检查网络",ReferenceDetailActivity.this);
				}
			}
		});
		EditBtnStatue=0;	
		mEditDialog.cancel();
		mReferenceTitle.setText(mReference.getFileName());;
		mReferenceDescription.setText(mReference.getDescription());;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mEditBtn)
		{
			// TODO Auto-generated method stub
			if (EditBtnStatue==0) {
				
				mEditBtn.setBackgroundResource(R.drawable.project_edit_button_confirm);
				mReferenceDescription.setEnabled(true);
				mReferenceTitle.setEnabled(true);
				EditBtnStatue=1;
			
			} 
			else {
				mEditDialog.show();
			}
	
		}
		else if(v==mBackBtn) {
			Intent intent_back = new Intent(ReferenceDetailActivity.this,ReferencesActivity.class);
			intent_back.putExtra("Project", mProject);
			intent_back.putExtra("Task", mTask);
			startActivity(intent_back);
		}
		else if(v==mDownLoadBtn) {
			downloadDir = "/mnt/sdcard/SRcoop/task/" + mTask.getTaskName()+ "/references"; // 目录--下载的文件文件存放于此	
			FileUtil file=new FileUtil(downloadDir);
			if (file.isExist())// 如果存在文件夹
			{// 初始化DownUtil对象（最后一个参数指定线程数）
				downUtil = new DownUtil(mReference.getFileUrl(),downloadDir + "/"+ mReference.getFileName(), 6);
				new DownloadThread().execute();
			}
		}		
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
	
		// TODO Auto-generated method stub
		if(which==1)
		{
			dialog.cancel();
		}
		else
		{
			if(dialog==mEditDialog)
			{
				SaveModification();
			}
		}
	}
}
