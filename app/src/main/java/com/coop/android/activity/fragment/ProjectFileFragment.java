package com.coop.android.activity.fragment;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.DocumentsActivity;
import com.coop.android.activity.R;
import com.coop.android.activity.bean.Directory;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.HandyTextView;

public class ProjectFileFragment extends Fragment implements  View.OnClickListener,DialogInterface.OnClickListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener{

	public static final String FILE_TAG = "com.srcoop.android.activity.fragment.ProjectFileFragment";
	public static final String Project_TAG= "com.srcoop.android.activity.fragment.ProjectFileFragment";
	private Project Project;
	private Directory mDirectory;
	
	private BaseDialog mDelDialog;
	private LinkedList<Directory> mDirectoryList = new LinkedList<Directory>();	
	private LoadingDialog mWaitingAddFileDialog;
		
	private FileAdapter listAdapter;
	private ShortMessageDialog mAddFileDialog;
	
	private TextView mProjectName;
	private ListView mListView;
	

	private Button mAddFileBtn;
	private int operation_position;
	private CustomToast CustomToast=new CustomToast();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Project = (Project) getActivity().getIntent().getSerializableExtra("Project");
		queryfiles();
	}
	
	private void queryfiles() {
		BmobQuery<Directory> query = new BmobQuery<Directory>();	
		query.addWhereRelatedTo("mDirectories", new BmobPointer(Project));
		query.findObjects(new FindListener<Directory>() {
			@Override
			public void done(List<Directory> list, BmobException e) {
				if (e == null) {
					for (Directory f : list)
						mDirectoryList.addFirst(f);
					listAdapter.notifyDataSetChanged();
				} else {

				}
			}
		});
	}
	
	private void initiateViews(View Projectfilefragment) {
		mProjectName = (TextView) getActivity().findViewById(R.id.project_activity_title);
		mProjectName.setText(Project.getProjectName());
		
		
		mAddFileBtn = (Button) getActivity().findViewById(R.id.btn_add_file);
		
		mWaitingAddFileDialog = new LoadingDialog(getActivity(),"New Folder Creation, Please Waiting...");

		mDelDialog  = new BaseDialog(getActivity());	
		mDelDialog.setTitle("Note");
		mDelDialog.setMessage("Confirm Delete？");
		
		mAddFileDialog = new ShortMessageDialog(getActivity());
		mAddFileDialog.setTitle("New Folder");
		mAddFileDialog.setHint("Folder Name");

		mListView = (ListView) Projectfilefragment.findViewById(R.id.temp_tasklist);
		listAdapter = new FileAdapter(getActivity(), mDirectoryList);
		mListView.setAdapter(listAdapter);
	}

	private void initiateEvent() {
		mDelDialog.setButton1("Sure",this);
		mDelDialog.setButton2("Cancel",this);
		
		mAddFileDialog.setButton1("Sure",this);
		mAddFileDialog.setButton2("Cancel",this);
		
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mAddFileBtn.setOnClickListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View Projectfilefragment = inflater.inflate(R.layout.fragment_tasklist, null);
		initiateViews(Projectfilefragment);
		initiateEvent();
		return Projectfilefragment;
	}

	/* new thread to add the fold to Project*/
	private class addFileThread extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitingAddFileDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				addFile(params[0]);
				Thread.sleep(1100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitingAddFileDialog.dismiss();
		}
	}

	/* 
	 * this function will evoke the funtion of addfiletoProject and be evoked by the funtion addthread
	 */
	private void addFile(String fileName) {
		mDirectory = new Directory();
		mDirectory.setName(fileName);
		mDirectory.setProject(Project);
		mDirectory.setEmail(BmobUser.getCurrentUser(User.class).getEmail());
		mDirectory.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e != null) {
					CustomToast.showCustomToast("Document Upload Failed",getActivity());
				}
			}
		});
	}

	/*
	 * this funtion is to add create a new folder in the system
	 */
	private void addFileToProject() {
		BmobRelation mDirectories = new BmobRelation();
		mDirectories.add(mDirectory);
		Project.setDirectories(mDirectories);
		Project.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					mDirectoryList.addFirst(mDirectory);
					listAdapter.notifyDataSetChanged();
					CustomToast.showCustomToast("Document Upload Finished",getActivity());
				} else {
					CustomToast.showCustomToast("Document Upload Failed",getActivity());
				}
			}
		});
	}

	/* FileAdapter Class */
	private class FileAdapter extends ArrayAdapter<Directory> {
		public FileAdapter(Context context, List<Directory> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_directory, parent, false);
			}
			TextView mTvFileName = (TextView) convertView.findViewById(R.id.tv_task_name);
			mTvFileName.setText(getItem(position).getName());
			return convertView;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 * if use confirm to add a folder,the system will add it,otherwise it will cancel the dialog
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which==0)
		{
			if(dialog==mAddFileDialog)
			{
				String achiName = mAddFileDialog.getText();
				if (achiName == null) {
					mAddFileDialog.requestFocus();
				} else {
					mAddFileDialog.dismiss();
					mAddFileDialog.setTextNull();
					new addFileThread().execute(achiName);
				}
			}
			else if(dialog==mDelDialog)
			{
				mDelDialog.dismiss();
				BmobRelation ProjectDirectory  = new BmobRelation();
				ProjectDirectory.remove(mDirectory);
				Project.setDirectories(ProjectDirectory);
				Project.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							mDirectory.delete(new UpdateListener() {
								@Override
								public void done(BmobException e) {
									if (e == null) {
										CustomToast.showCustomToast("Delete Reference Finished",getActivity());
										mDirectoryList.remove(operation_position);
										listAdapter.notifyDataSetChanged();
									} else {
										CustomToast.showCustomToast("Delete Reference Failed",getActivity());
									}

								}
							});
						} else {
							CustomToast.showCustomToast("Delete Reference Failed",getActivity());
						}
					}
				});
			}
		} else {
			dialog.cancel();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 * when click the item of the list,user will be displayed the files list of contained by the folder
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub{
			Intent intent = new Intent(getActivity(),DocumentsActivity.class);
			Directory directory = mDirectoryList.get(position);
			intent.putExtra("Project", Project);
			intent.putExtra("Directory", directory);
			startActivity(intent);
			getActivity().finish();
		}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mAddFileBtn)
		{
			mAddFileDialog.show();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
		// TODO Auto-generated method stub
		deleteDirectory(position);
		return true;
	}
	
	private void deleteDirectory(int position){
		operation_position=position;
		mDirectory = mDirectoryList.get(operation_position);
		if(BmobUser.getCurrentUser(User.class).getEmail().equals(mDirectory.getEmail()))
		{
			mDelDialog.show();
		}else
		{
			CustomToast.showCustomToast("You do not have authority to delete",getActivity());
		}
	}
}