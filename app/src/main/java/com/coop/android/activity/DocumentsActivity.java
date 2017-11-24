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
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.coop.android.activity.bean.Directory;
import com.coop.android.activity.bean.Document;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ConfirmDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.fragment.ProjectFileFragment;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.util.DownUtil;
import com.coop.android.activity.util.FileUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class DocumentsActivity extends FragmentActivity implements View.OnClickListener,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,DialogInterface.OnClickListener, OnRefreshListener<ListView>
{
	public static final String DOC_TAG = "com.srcoop.android.activity.DocumentsDisplay";
	private CustomToast CustomToast=new CustomToast();	
	private int operation_position;
	
	private Project Project;
	private BmobFile mBmobFile;
	private Directory mDirectory;
	private Document mDoc;

	private BaseDialog mDelDialog;
	private ConfirmDialog mAddDocDialog;
	private LoadingDialog mWaitingAddDocDialog;
	
	private Button mAddDocBtn;
	private Button mBackBtn;
	private PullToRefreshListView mPullRefreshListView;	
	private LinkedList<Document> mDocumentList = new LinkedList<Document>();
	private DocAdapter listAdapter;

	private Bundle fileBundle;
	private String downloadDir;
	private String filePath;
	private String fileName;

	private DownUtil downUtil;
	private FileUtil fileUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_documentslist);
		Project = (Project)getIntent().getSerializableExtra("Project");
		mDirectory = (Directory)getIntent().getSerializableExtra("Directory");
		querydocuments();
		initiateViews();
		initiateEvents();
	}
	
	private void initiateViews() {
		mWaitingAddDocDialog = new LoadingDialog(DocumentsActivity.this,"Reference Resources Creation，Please Waiting...");
				
		mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.filelist_listview_files);
		listAdapter = new DocAdapter(DocumentsActivity.this, mDocumentList);	
		mPullRefreshListView.setAdapter(listAdapter);
		
		mDelDialog  = new BaseDialog(DocumentsActivity.this);	
		mDelDialog.setTitle("Note");
		mDelDialog.setMessage("Confirm Delete？");

		mAddDocDialog = new ConfirmDialog(DocumentsActivity.this);
		mAddDocDialog.setTitle("FIle Path");
		mAddDocDialog.setText("Empty");
	
		mAddDocBtn = (Button) findViewById(R.id.filelist_image_addfiles);			
		mBackBtn = (Button) findViewById(R.id.filelist_image_back);	
	}
	
	private void initiateEvents() {
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setOnItemLongClickListener(this);
		mPullRefreshListView.setOnRefreshListener(this);
		
		mDelDialog.setButton1("Sure",this);
		mDelDialog.setButton2("Cancel",this);
		
		mAddDocDialog.setButton1("Sure",this);
		mAddDocDialog.setButton2("Cancel",this);
		mAddDocBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
	}

	
	private class GetDocumentsThread extends AsyncTask<Void, Void, String[]> {
		@Override
		protected String[] doInBackground(Void... params) {
			try {
				querydocuments();
				Thread.sleep(2000);
			} catch (InterruptedException e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			listAdapter.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
	
	private void querydocuments() {
		BmobQuery<Document> query = new BmobQuery<Document>();	
		query.addWhereRelatedTo("mDocuments", new BmobPointer(mDirectory));
		query.findObjects(new FindListener<Document>() {
			@Override
			public void done(List<Document> list, BmobException e) {
				if (e == null) {
					mDocumentList.clear();
					for (Document d : list)
						mDocumentList.addFirst(d);
					listAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	/* addRefThread <--0.Thread for add reference recourses */
	private class addRefThread extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitingAddDocDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				File file=new File(filePath);
				if(!fileName.equals("init")) {
					mBmobFile = new BmobFile(file);
					fileUtil = new FileUtil(filePath);
					mBmobFile.uploadblock(new UploadFileListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								mDoc = new Document();
								mDoc.setName(BmobUser.getCurrentUser(User.class).getName());
								mDoc.setSize(fileUtil.getFileSize());
								mDoc.setType(fileUtil.getFileType());
								mDoc.setFileUrl(mBmobFile.getFileUrl());
								mDoc.setFileName(mBmobFile.getFilename());
								mDoc.save(new SaveListener<String>() {
									@Override
									public void done(String s, BmobException e) {
										if (e == null) {
											addDocToTask();
										} else {
											CustomToast.showCustomToast("Recourses Upload Failed:" + e.toString(),DocumentsActivity.this);
										}
									}
								});
							} else {
								CustomToast.showCustomToast("Recourses Upload Failed, Make Sure Document Has Suffix:" + e.toString(),DocumentsActivity.this);
							}
						}
					});
				} else {
					CustomToast.showCustomToast("This File Can Not Be Upoad",DocumentsActivity.this);
				}
				Thread.sleep(1100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitingAddDocDialog.dismiss();
		}
	}
	
	private class DownloadThread extends AsyncTask<Void, Integer, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{super.onPreExecute();}

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
			CustomToast.showCustomToast("Download Finished，Save As" + downloadDir, DocumentsActivity.this);
		}
	}

	private void addDocToTask() {
		BmobRelation mDocs = new BmobRelation();
		mDocs.add(mDoc);
		mDirectory.setDocuments(mDocs);
		mDirectory.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					mDocumentList.addFirst(mDoc);
					listAdapter.notifyDataSetChanged();
					CustomToast.showCustomToast("Add Reference Resources Finished",DocumentsActivity.this);
					filePath = null;
				} else {
					CustomToast.showCustomToast("Add Reference Resources Failed:" + e.toString(), DocumentsActivity.this);
				}
			}
		});
	}

	private class DocAdapter extends ArrayAdapter<Document> {
		public DocAdapter(Context context, List<Document> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(final int position, View convertView,ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item_document, parent, false);
			}

			Button mDownLoadBtn = (Button) convertView.findViewById(R.id.document_button_download);			
			mDownLoadBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					 downloadDir= "/mnt/sdcard/Scoop/Task/" + mDirectory.getName()+"/Reference";
						if (isFolderExists(downloadDir)) {
							downUtil = new DownUtil(getItem(position).getFileUrl(),downloadDir + "/"+ getItem(position).getFileName(), 6);
							new DownloadThread().execute();
						}
				}
			});

			TextView mDocName= (TextView) convertView.findViewById(R.id.document_textview_title);
			TextView mDocSize= (TextView) convertView.findViewById(R.id.document_textview_size);
			TextView mDocType= (TextView) convertView.findViewById(R.id.document_textview_type);
			TextView mDocTime= (TextView) convertView.findViewById(R.id.document_textview_time);
			TextView mDocFrom = (TextView) convertView.findViewById(R.id.document_textview_from);

			LinearLayout mLayout = (LinearLayout) findViewById(R.id.reference_item_layout);
			
			mDocName.setText(getItem(position).getFileName());		
			mDocFrom.setText(getItem(position).getName());
			mDocSize.setText(getItem(position).getSize());
			mDocType.setText(getItem(position).getType());
			mDocTime.setText(getItem(position).getCreatedAt());

			return convertView;
		}
	}


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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
		// TODO Auto-generated method stub
		deleteDocument(position);
		return true;
	}
	
	private void deleteDocument(int position){
		operation_position=position-1;
		mDoc = mDocumentList.get(operation_position);
		if(BmobUser.getCurrentUser(User.class).getEmail().equals(mDoc.getEmail())) {
			mDelDialog.show();
		} else {
			CustomToast.showCustomToast("You Do Not Have Authority To Delete",DocumentsActivity.this);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1) {
			if (dialog == mAddDocDialog) {
				mAddDocDialog.cancel();
				mAddDocDialog.setTextNull();
			}
			dialog.cancel();
		}
		else if (which == 0) {
			if (dialog == mAddDocDialog) {
				if (filePath == null) {
					CustomToast.showCustomToast("Add Reference Resources Failed",DocumentsActivity.this);
					mAddDocDialog.dismiss();
				} else {
					mAddDocDialog.dismiss();
					mAddDocDialog.setTextNull();
					try {
						new addRefThread().execute();
					} catch(Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
			else if (dialog == mDelDialog) {
				mDelDialog.dismiss();
				BmobRelation fileDocument  = new BmobRelation();
				fileDocument.remove(mDoc);
				mDirectory.setDocuments(fileDocument);
				mDirectory.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							mDoc.delete(new UpdateListener() {
								@Override
								public void done(BmobException e) {
									if (e == null) {
										CustomToast.showCustomToast("Reference Resources Finished",DocumentsActivity.this);
										mDocumentList.remove(operation_position);
										listAdapter.notifyDataSetChanged();
									} else {
										CustomToast.showCustomToast("Reference Resources Failed",DocumentsActivity.this);
									}
								}
							});
						} else {
							CustomToast.showCustomToast("Reference Resources Failed",DocumentsActivity.this);
						}
					}
				});
			}
		}		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mAddDocBtn) {
			Intent intent = new Intent(DocumentsActivity.this,FileExplorerActivity.class);
			startActivityForResult(intent, 1);
		} else if(v == mBackBtn) {
			Intent intent_back = new Intent(DocumentsActivity.this,ProjectActivity.class);
			intent_back.putExtra("Project", Project);
			startActivity(intent_back);
			finish();
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		String label = DateUtils.formatDateTime(DocumentsActivity.this,System.currentTimeMillis(),DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_DATE| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		new GetDocumentsThread().execute();	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) { // resultCode is mark for call back functio
		case RESULT_OK:
			fileBundle = data.getExtras();
			filePath = fileBundle.getString("path"); 
			fileName = fileBundle.getString("name"); 
			mAddDocDialog.setText(fileName);
			mAddDocDialog.show();
			break;
		default:
			break;
		}
	}
}
