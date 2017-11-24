package com.coop.android.activity.fragment;

import java.util.Date;
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
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.R;
import com.coop.android.activity.TaskActivity;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.HandyTextView;

public class ProjectTaskFragment extends Fragment implements  View.OnClickListener,DialogInterface.OnClickListener,AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{

	public static final String TASK_TAG = "com.srcoop.android.activity.fragment.ProjectTaskListFragment";
	public static final String Project_TAG = "com.srcoop.android.activity.fragment.ProjectTaskListFragment";

	private int operation_position;
	private CustomToast CustomToast=new CustomToast();
	private ListView mListView;
	private TextView mProjectName;
	private Button mAddTaskBtn;
	
	
	private Task task;
	private Project Project;
	
	private LinkedList<Task> mTaskList = new LinkedList<Task>();
	private TaskAdapter listAdapter;

	private BaseDialog mBaseDialog;
	private ShortMessageDialog mAddTaskDialog;
	private LoadingDialog mWaitingAddTaskDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Project = (Project) getActivity().getIntent().getSerializableExtra("Project");		
		queryTasks();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mWaitingAddTaskDialog = new LoadingDialog(getActivity(),"Task Creation，Please Waiting...");
		View Projecttaskfragment = inflater.inflate(R.layout.fragment_tasklist, null);
		initiateViews(Projecttaskfragment);
		initiateEvents();
		return Projecttaskfragment;
	}
	
	private void initiateViews(View Projecttaskfragment) {
		mProjectName = (TextView) getActivity().findViewById(R.id.project_activity_title);
		mProjectName.setText(Project.getProjectName());
		
		mAddTaskBtn = (Button) getActivity().findViewById(R.id.btn_add_task);
		
		mBaseDialog   = new BaseDialog(getActivity());	
		mAddTaskDialog = new ShortMessageDialog(getActivity());
		
		mAddTaskDialog.setTitle("Add Task");
		mAddTaskDialog.setHint("Type Task Title");
		
		mBaseDialog.setTitle("Note");
		mBaseDialog.setMessage("Confirm Delete? ALl The Reference Will Be Delete");

		mListView = (ListView) Projecttaskfragment.findViewById(R.id.temp_tasklist);
		listAdapter = new TaskAdapter(getActivity(), mTaskList);
		mListView.setAdapter(listAdapter);		
	}
	
	private void initiateEvents() {
		mAddTaskBtn.setOnClickListener(this);
		
		mBaseDialog.setButton1("Sure",this);
		mBaseDialog.setButton2("Cancel",this);
		mAddTaskDialog.setButton1("Sure",this);
		mAddTaskDialog.setButton2("Cancel",this);
		
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);	
	}
	
	private void queryTasks() {
		BmobQuery<Task> query = new BmobQuery<Task>();
		query.addWhereRelatedTo("mTasks", new BmobPointer(Project));
		query.findObjects(new FindListener<Task>() {
			@Override
			public void done(List<Task> list, BmobException e) {
				if (e == null) {
					for (Task t : list) {
						mTaskList.addFirst(t);
					}
					listAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void deleteTask(int position){
		operation_position=position;
		task = mTaskList.get(operation_position);
		if(BmobUser.getCurrentUser(User.class).getEmail().equals(Project.getLeader().getEmail())){
			mBaseDialog.show();
		}else
		{
			CustomToast.showCustomToast("You Do Not Have Authority To Delete",getActivity());
		}
	}
	
	
	private void addTask(String taskName) {
		task = new Task();
		task.setTaskName(taskName);
		BmobDate date = new BmobDate(new Date());
		task.setTaskCreateTime(date);
		task.setProject(Project);
		task.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e != null) {
					CustomToast.showCustomToast("Task Creation Failed",getActivity());
				}
			}
		});
	}

	private void addTaskToProject() {
		BmobRelation mTasks = new BmobRelation();
		mTasks.add(task);
		Project.setTasks(mTasks);
		Project.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					mTaskList.addFirst(task);
					listAdapter.notifyDataSetChanged();
					CustomToast.showCustomToast("Task Creation Finished",getActivity());
				} else {
					CustomToast.showCustomToast("Task Creation Failed",getActivity());
				}
			}
		});
	}


	private class addTaskThread extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitingAddTaskDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				addTask(params[0]);
				Thread.sleep(1100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitingAddTaskDialog.dismiss();
		}
	}

	private class TaskAdapter extends ArrayAdapter<Task> {
		public TaskAdapter(Context context, List<Task> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_task, parent, false);
			}
			TextView mTaskName = (TextView) convertView.findViewById(R.id.tv_task_name);
			TextView mTaskDesc = (TextView) convertView.findViewById(R.id.tv_task_desc);
			mTaskName.setText(getItem(position).getTaskName());			
			mTaskDesc.setText(getItem(position).getTaskContent());
			return convertView;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which==1) {
			dialog.cancel();
		} else {
			if (dialog==mAddTaskDialog) {
				String taskName = mAddTaskDialog.getText();
				if (taskName == null) {
					mAddTaskDialog.requestFocus();
					CustomToast.showCustomToast("Task Title Can Not Be Empty",getActivity());
				} else {
					mAddTaskDialog.dismiss();
					mAddTaskDialog.setTextNull();
					new addTaskThread().execute(taskName);
				}		
			} else if (dialog==mBaseDialog) {
				mBaseDialog.dismiss();
				BmobRelation ProjectTask  = new BmobRelation();
				ProjectTask.remove(task);
				Project.setTasks(ProjectTask);
				Project.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							task.delete(new UpdateListener() {
								@Override
								public void done(BmobException e) {
									if (e == null) {
										CustomToast.showCustomToast("Task Delete Finished",getActivity());
										mTaskList.remove(operation_position);
										listAdapter.notifyDataSetChanged();
									} else {
										CustomToast.showCustomToast("Task Delete Failed",getActivity());
									}
								}
							});
						} else {
							CustomToast.showCustomToast("Task Delete Failed",getActivity());
						}
					}
				});
			}
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(),TaskActivity.class);
		Task task = mTaskList.get(position);
		intent.putExtra("Project", Project);
		intent.putExtra("Task", task);
		startActivity(intent);	
		getActivity().finish();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
		// TODO Auto-generated method stub
		deleteTask(position);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mAddTaskBtn) {
			mAddTaskDialog.show();			
		}
		
	}

}
