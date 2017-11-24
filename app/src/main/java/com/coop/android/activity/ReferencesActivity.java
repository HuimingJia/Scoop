package com.coop.android.activity;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Reference;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ConfirmDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.util.DownUtil;
import com.coop.android.activity.util.FileUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public class ReferencesActivity extends FragmentActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,DialogInterface.OnClickListener,View.OnClickListener,OnRefreshListener<ListView>
{	
	private int operation_position;
	private Project mProject;
	private Task mTask;
	private BmobFile bmobFile;
	private Reference mReference;
	
	
	
	private PullToRefreshListView mPullRefreshListView;	
	private LinkedList<Reference> mReferenceList = new LinkedList<Reference>();
	private ReferenceAdapter listAdapter;
	
	private CustomToast CustomToast=new CustomToast();

	private Button mAddRefBtn;
	private Button mBackBtn;
	
	private BaseDialog mDelDialog;
	private ConfirmDialog mAddRefDialog; // 确认文件路径的dialog
	private LoadingDialog mWaitingAddRefDialog;

	// 文件路径选择需要用到跳转--用Bundle传值方法
	/* private Intent fileIntent; */
	private Bundle fileBundle;
	private String downloadDir;
	private String filePath;
	private String fileName;
	
	private FileUtil fileUtil;

	// 文件下载
	private DownUtil downUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);		
		mProject = (Project)getIntent().getSerializableExtra("Project");
		mTask = (Task)getIntent().getSerializableExtra("Task");
		setContentView(R.layout.activity_referenceslist);
		queryReferences();
		initiateViews();
		initiateEvents();	
	}

	private void initiateViews()
	{
		mWaitingAddRefDialog = new LoadingDialog(ReferencesActivity.this, "添加参考资料中，请稍后...");
	
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.referenceslist_listview);
		listAdapter = new ReferenceAdapter(ReferencesActivity.this, mReferenceList);
		mPullRefreshListView.setAdapter(listAdapter);

		mDelDialog  = new BaseDialog(ReferencesActivity.this);	
		mDelDialog.setTitle("提示");
		mDelDialog.setMessage("确认删除参考资料？");
		
		mAddRefDialog = new ConfirmDialog(ReferencesActivity.this);
		mAddRefDialog.setTitle("文件路径");
		mAddRefDialog.setText("空");

		mAddRefBtn = (Button) findViewById(R.id.referenceslist_button_add);
		mBackBtn= (Button) findViewById(R.id.referenceslist_image_back);
	}
	
	private void initiateEvents()
	{
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setOnItemLongClickListener(this);
		
		mDelDialog.setButton1("确定",this);
		mDelDialog.setButton2("取消",this);
		
		mAddRefDialog.setButton1("确认", this);
		mAddRefDialog.setButton2("取消", this);
		mAddRefBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
	}

	private void queryReferences()
	{
		BmobQuery<Reference> query = new BmobQuery<Reference>();
		query.addWhereRelatedTo("mReferences", new BmobPointer(mTask));
		query.findObjects(ReferencesActivity.this,new FindListener<Reference>() 
		{

			@Override
			public void onSuccess(List<Reference> ref)
			{
				for (Reference r : ref)
					mReferenceList.addFirst(r);
				listAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(int arg0, String arg1) {

			}
		});		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode)
		{ // resultCode为回传的标记，我在SDFileExploreActivity中回传的是RESULT_OK
		case RESULT_OK:
			fileBundle = data.getExtras(); // data为B中回传的Intent
			filePath = fileBundle.getString("path"); // path即为回传的值
			fileName = fileBundle.getString("name"); 
			mAddRefDialog.setText(fileName);
			mAddRefDialog.show();
			break;
		default:
			break;
		}
	}
	
	private class GetReferencesThread extends AsyncTask<Void, Void, String[]>
	{
		@Override
		protected String[] doInBackground(Void... params)
		{
			try 
			{
				queryReferences();
				Thread.sleep(2000);
				} 
			catch (InterruptedException e) 
			{}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) 
		{
			listAdapter.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
		
	/* addRefThread <--0.添加参考资料线程 */
	private class addRefThread extends AsyncTask<Void, Integer, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			mWaitingAddRefDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) 
		{
			try {
				File file=new File(filePath);
				if(!fileName.contains("init"))
				{
					bmobFile = new BmobFile(file);
					fileUtil = new FileUtil(filePath);
					bmobFile.uploadblock(ReferencesActivity.this,new UploadFileListener() 
					{
						@Override
						public void onSuccess() {
				
							mReference = new Reference();
							mReference.setName(BmobUser.getCurrentUser(ReferencesActivity.this, User.class).getName());
							mReference.setgetEmail("123");
							mReference.setgetEmail(BmobUser.getCurrentUser(ReferencesActivity.this, User.class).getEmail());
							mReference.setSize(fileUtil.getFileSize());
							mReference.setType(fileUtil.getFileType());
							mReference.setFileUrl( bmobFile.getFileUrl(ReferencesActivity.this));
							mReference.setFileName(bmobFile.getFilename());
							mReference.save(ReferencesActivity.this,new SaveListener()
							{
								@Override
								public void onSuccess(){
									addRefToTask();
								}
	
								@Override
								public void onFailure(int arg0,String msg) 
								{
									CustomToast.showCustomToast("添加参考资料失败:"+ msg,ReferencesActivity.this);
								}
							});
						}
	
						@Override
						public void onProgress(Integer value) 
						{	}
	
						@Override
						public void onFailure(int arg0, String msg) {
							  if(arg0==146)
							  {
								  CustomToast.showCustomToast("请上传有后缀的文件",ReferencesActivity.this);
							  }
							  else
							  {
								  CustomToast.showCustomToast("上传文件失败:" + msg,ReferencesActivity.this);
								  }
						 }
					
					});
				}
				else
				{
					  CustomToast.showCustomToast("该文件不可以被上载",ReferencesActivity.this);
				}
				Thread.sleep(1100);
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitingAddRefDialog.dismiss();
		}
	}

	/* addRefToTask 函数 <--- 2.添加参考资料到任务的关联关系 addRef函数会调用 */
	private void addRefToTask() 
	{
		BmobRelation mRefs = new BmobRelation();
		mRefs.add(mReference);
		mTask.setReferences(mRefs);
		mTask.update(ReferencesActivity.this, new UpdateListener() 
		{
			@Override
			public void onSuccess()
			{
				mReferenceList.addFirst(mReference);
				listAdapter.notifyDataSetChanged();
				CustomToast.showCustomToast("添加参考资料成功",ReferencesActivity.this);
				filePath = null;
			}

			@Override
			public void onFailure(int arg0, String msg) 
			{CustomToast.showCustomToast("添加参考资料失败:" + msg,ReferencesActivity.this);}
		});
	}


	/*
	 * RefAdapter类
	 */
	private class ReferenceAdapter extends ArrayAdapter<Reference> 
	{
		public ReferenceAdapter(Context context, List<Reference> objects) 
		{super(context, 0, objects);}

		@Override
		public View getView(final int position, View convertView,ViewGroup parent) 
		{
			if (convertView == null) 
			{convertView = getLayoutInflater().inflate(R.layout.item_reference, parent, false);}
			Button mDownLoadBtn = (Button) convertView.findViewById(R.id.references_download);
			mDownLoadBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View arg0) {										
									
					Intent intent_back = new Intent(ReferencesActivity.this,ReferenceDetailActivity.class);
					intent_back.putExtra("Project", mProject);
					intent_back.putExtra("Task", mTask);
					intent_back.putExtra("Reference", getItem(position));
					startActivity(intent_back);
				}
			});

			TextView mRefName = (TextView) convertView.findViewById(R.id.fileName);
			TextView uploadTime = (TextView) convertView.findViewById(R.id.uploadTime);
			LinearLayout mLayout = (LinearLayout) findViewById(R.id.reference_item_layout);
			
			mRefName.setText(getItem(position).getFileName());
			uploadTime.setText(getItem(position).getCreatedAt());
			return convertView;
		}
	}

	/*
	 * 判断文件路径路径是否存在
	 */
	boolean isFolderExists(String strFolder) {
		File file = new File(strFolder);
		if (!file.exists()) {
			if (file.mkdirs()) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id)
	{// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) 
	{
		// TODO Auto-generated method stub
		deleteReference(position);
		return true;
	}
	
	private void deleteReference(int position){
		operation_position=position-1;
		mReference = mReferenceList.get(operation_position);
		if(BmobUser.getCurrentUser(ReferencesActivity.this,User.class).getEmail().equals(mReference.getEmail()))
		{
			mDelDialog.show();
		}else
		{
			CustomToast.showCustomToast("不是任务的创建者，不可以删除任务",ReferencesActivity.this);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which==1)
		{
			dialog.cancel();
		}
		else if(which==0)
		{
			if(dialog==mAddRefDialog)
			{
				if (filePath == null)
				{
					CustomToast.showCustomToast("添加资料失败,文件名为空",ReferencesActivity.this);
					mAddRefDialog.dismiss();
				} 
				else 
				{
					mAddRefDialog.dismiss();
					mAddRefDialog.setTextNull();
					new addRefThread().execute();
				}				
			}
			else if(dialog==mDelDialog)
			{
				mDelDialog.dismiss();
				BmobRelation taskReference  = new BmobRelation();
				taskReference.remove(mReference);
				mTask.setReferences(taskReference);
				mTask.update(ReferencesActivity.this,new UpdateListener() {					
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						mReference.delete(ReferencesActivity.this,new DeleteListener() 
						{							
							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								CustomToast.showCustomToast("删除参考资料成功",ReferencesActivity.this);
								mReferenceList.remove(operation_position);
								listAdapter.notifyDataSetChanged();
							}
							
							@Override
							public void onFailure(int arg0, String arg1)
							{
								// TODO Auto-generated method stub		
								CustomToast.showCustomToast("删除参考资料失败",ReferencesActivity.this);
							}
						});
					}					
					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						CustomToast.showCustomToast("删除参考资料失败",ReferencesActivity.this);
					}
				});			
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mAddRefBtn)
		{
			Intent intent = new Intent(ReferencesActivity.this,FileExplorerActivity.class);
			startActivityForResult(intent, 1);	
		}
		else if(v==mBackBtn)
		{
			Intent intent_back = new Intent(ReferencesActivity.this,TaskActivity.class);
			intent_back.putExtra("Project", mProject);
			intent_back.putExtra("Task", mTask);
			startActivity(intent_back);
		}
		
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		String label = DateUtils.formatDateTime(ReferencesActivity.this,System.currentTimeMillis(),DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_DATE| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		new GetReferencesThread().execute();	
		
	}
}
