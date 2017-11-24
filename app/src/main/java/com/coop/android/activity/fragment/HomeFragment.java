package com.coop.android.activity.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.ProjectActivity;
import com.coop.android.activity.R;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Student;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.toast.CustomToast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HomeFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,OnRefreshListener<ListView>,DialogInterface.OnClickListener {

	public static final String Project_TAG = "com.srcoop.android.activity.fragment.HomeFragment";

	private User mUser;	
	private Project clickProject;
	private Project mProject;
	
	private int type;
	private int position_operation;
	private String ProjectOwnerId;
	
	private Button mAddProjectBtn;
	private CustomToast CustomToast=new CustomToast();
	
	private BaseDialog mBaseDialog;
	private ShortMessageDialog mEditTextDialog;
	
	private PullToRefreshListView mPullRefreshListView;
	private LinkedList<Project> mProjectList=	 new LinkedList<Project>();
	private ArrayList<String> mProjectIDList=	new ArrayList<String>();
	private ProjectAdapter listAdapter;
	private String title;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View homefragment = inflater.inflate(R.layout.fragment_menuitem_home, null, false);
		initiateViews(homefragment);
		mPullRefreshListView.setAdapter(listAdapter);	
		initiateEvents();

		ViewGroup parent = (ViewGroup)homefragment.getParent();
		if (parent != null) {
			parent.removeView(homefragment);
		}
		return homefragment;
	}
	
	/*
	 * the needed operation when create the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getUser();
		listAdapter = new ProjectAdapter(getActivity(), mProjectList);
		queryProjects_L();
		queryProjects_M();
	}
	
	/*
	 * get the type of current user and get the instance of current user
	 * if type==0, user is a teacher
	 * if tupe==1, user is a student
	 */
	private void getUser() {
		mUser = BmobUser.getCurrentUser(User.class);
	}
	/*
	 * initiate the views will been seen in this homefragment
	 */

	private void initiateViews(View homefragment) {
		((TextView) getActivity().findViewById(R.id.activity_title)).setText("Home");
		
		mAddProjectBtn = ((Button) getActivity().findViewById(R.id.title_bar_right_menu));
		mAddProjectBtn.setBackgroundResource(R.drawable.button_add);
		mAddProjectBtn.setClickable(true);
		
		mPullRefreshListView = (PullToRefreshListView) homefragment.findViewById(R.id.mprojectlistview);
		mBaseDialog = new BaseDialog(getActivity());
		mEditTextDialog = new ShortMessageDialog(getActivity());
		/*
		 * system will present different warnning note for different types of users
		 * if the user is a teacher,the system will present
		 * or it will present
		 */
		
		mEditTextDialog.setTitle("Add Topic");
		mEditTextDialog.setHint("Add Topic Name");

	}

	private void initiateEvents() 
	{
		mAddProjectBtn.setOnClickListener(this);
		mPullRefreshListView.setOnRefreshListener(this);	
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setOnItemLongClickListener(this);
		mBaseDialog.setButton1("Sure",this);
		mBaseDialog.setButton2("Cancel",this);
		mEditTextDialog.setButton1("Sure",this);
		mEditTextDialog.setButton2("Cancel",this);
	}
	
	private void queryProjects_L() {
		BmobQuery<Project> query = new BmobQuery<Project>();
		query.addWhereRelatedTo("mProjects_L", new BmobPointer(mUser));
		query.addWhereNotContainedIn("mProjectID", mProjectIDList);
	
		query.findObjects(new FindListener<Project>() {
			@Override
			public void done(List<Project> list, BmobException e) {
				for (Project i : list) {
					if (mProjectList.indexOf(i) >= 0)
						continue;
					mProjectList.addFirst(i);
					mProjectIDList.add(i.getProjectID());
				}
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	private void queryProjects_M() {
		BmobQuery<User_Project> query = new BmobQuery<User_Project>();
		query.addWhereEqualTo("mEmail", mUser.getEmail().toString());	
		query.addWhereNotContainedIn("mProjectID", mProjectIDList);
		query.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e == null)  {
					for (User_Project i : list) {
						BmobQuery<Project> query_p = new BmobQuery<Project>();
						query_p.getObject(i.getObjectId(), new QueryListener<Project>() {
							@Override
							public void done(Project project, BmobException e) {
								if (e == null) {
									mProjectList.addFirst(project);
									mProjectIDList.add(project.getProjectID());
									listAdapter.notifyDataSetChanged();
								} else {
									CustomToast.showCustomToast(e.toString(),getActivity());
								}
							}
						});
					}
				} else {
					CustomToast.showCustomToast("Information Lost", getActivity());
				}
			}
		});
	}

	private void addProject() {
		mProject = new Project();
		mProject.setProjectName(title);
		BmobDate date = new BmobDate(new Date());
		mProject.setProjectCreateTime(date);
		mProject.setLeader(mUser);
		mProject.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Topic Creation Successed : " + title, getActivity());
					BmobRelation mProjects = new BmobRelation();
					mProjects.add(mProject);
					mUser.setProjects_L(mProjects);
					mUser.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								mProjectList.addFirst(mProject);
								mProjectIDList.add(mProject.getProjectID());
								listAdapter.notifyDataSetChanged();
								CustomToast.showCustomToast("Topic Creation Successed", getActivity());
							} else {
								CustomToast.showCustomToast("Topic Creation Failed", getActivity());
							}
						}
					});

					mProject.setProjectID(mProject.getObjectId().toString());
					mProject.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								CustomToast.showCustomToast("ID Creation Successed",getActivity());
							} else {
								CustomToast.showCustomToast("ID Creation Failed",getActivity());
							}
						}
					});
				} else {
					CustomToast.showCustomToast(e.toString(), getActivity());
				}
			}
		});
	}
	
	// Delete Topic
	private void deleteProject(int position)
	{		
		this.position_operation=position;
		clickProject = mProjectList.get(position-1);
		ProjectOwnerId = clickProject.getLeader().getObjectId();
		if(mUser.getObjectId().equals(ProjectOwnerId))
		{
			mBaseDialog.setTitle("提示");
			mBaseDialog.setMessage("确认删除课题？删除课题后与课题相关的所有资料将会被删除，无法访问");
		}
		else
		{
			mBaseDialog.setTitle("提示");
			mBaseDialog.setMessage("确认退出课题？退出后将不可以访问与该课题相关的资料");	
		}
		mBaseDialog.show();
	}

	private void DismissProject()
	{
		clickProject.delete(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Delete Success", getActivity());
					listAdapter.notifyDataSetChanged();
				} else {
					CustomToast.showCustomToast("Delete Failed", getActivity());
				}
			}
		});

		// If current user is teacher, Delete the relation
		BmobRelation membersProject = new BmobRelation();
		membersProject.remove(clickProject);
		mUser.setProjects_L(membersProject);
		mUser.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Project Delete Success", getActivity());
					mProjectList.remove(position_operation-1);
					listAdapter.notifyDataSetChanged();
				} else {
					CustomToast.showCustomToast("Project Delete Failed", getActivity());
				}

			}
		});
	}
	
	private void BackoutProject()
	{
		mBaseDialog.dismiss();
		// 检查删除者是否为创建者
		
		BmobQuery<User_Project> query = new BmobQuery<User_Project>();
		query.addWhereEqualTo("mProject",clickProject.getObjectId().toString());
		query.addWhereEqualTo("mEmail", mUser.getEmail());
		query.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e == null) {
					list.get(0).delete(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							CustomToast.showCustomToast("Exit Project Success", getActivity());
							mProjectList.remove(position_operation-1);
							listAdapter.notifyDataSetChanged();
						}
					});
				} else {
					CustomToast.showCustomToast("Can not get relation！",getActivity());
				}
			}
		});
	}
	

	private class GetDataTask extends AsyncTask<Void, Void, String[]>
	{
		@Override
		protected String[] doInBackground(Void... params) {
			try {
				queryProjects_L();
				queryProjects_M();
				Thread.sleep(2000);
			} catch (InterruptedException e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			listAdapter.notifyDataSetChanged();
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	private class ProjectAdapter extends ArrayAdapter<Project> {
		public ProjectAdapter(Context context, List<Project> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_project, parent, false);
			}

			TextView mTvProjectName = (TextView) convertView.findViewById(R.id.tv_project_name);
			TextView mTvProjectDesc = (TextView) convertView.findViewById(R.id.tv_project_desc);
			
			mTvProjectName.setText(getItem(position).getProjectName());
			mTvProjectDesc.setText(getItem(position).getProjectContent());
			return convertView;
		}
	}
	
	/*
	 * the funtion to implement the listener to display the dialog to add new Project
	 */
	
	@Override
	public void onClick(View v) {
		if (v == mAddProjectBtn) {
			mEditTextDialog.show();
		}
	}
	/*
	 * the function to display a certain Project page one user onclick the Project
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
	{
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(),ProjectActivity.class);
		Project Project = mProjectList.get(position-1);
		intent.putExtra("Project", Project);
		startActivity(intent);
	}
	
	/*
	 * the function to refresh the Project list when user pull the list
	 */

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) 
	{
		// TODO Auto-generated method stub
		String label = DateUtils.formatDateTime(getActivity(),System.currentTimeMillis(),DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_DATE| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		new GetDataTask().execute();		
	}
	
	/*
	 * the function to delete the Project when user click it for a long time
	 */

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) 
	{
		// TODO Auto-generated method stub
		deleteProject(position);
		return true;
	}

	/*
	 * the listener of different type of dialogs
	 * when user put the button of confirm, the which ==0
	 * when user put the button of cancel, the which ==1
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		//CustomToast.showCustomToast(dialog.toString(), getActivity());
		if (dialog == mEditTextDialog) {
			//CustomToast.showCustomToast(dialog.toString(), getActivity());
			if (which == 0) {
				if (mEditTextDialog.getText() == null) {
					mEditTextDialog.requestFocus();
					CustomToast.showCustomToast("Please Input Topic Name", getActivity());
				} else {
					mEditTextDialog.dismiss();
					title = mEditTextDialog.getText().toString();
					addProject();
					mEditTextDialog.setTextNull();
				}
			} else if (which == 1) {
				dialog.cancel();
			}
		}
		 
		else if(dialog==mBaseDialog) {
			if (which == 0) {
				mBaseDialog.dismiss();
				if (mUser.getObjectId().equals(ProjectOwnerId)) {
					DismissProject();
				} else {
					BackoutProject();
				}			
			} else if(which==1) {
				mBaseDialog.cancel();				
			}			
		}
	}
}
