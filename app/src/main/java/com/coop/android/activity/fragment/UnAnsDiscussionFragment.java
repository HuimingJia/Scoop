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
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.DiscussionDetailActivity;
import com.coop.android.activity.R;
import com.coop.android.activity.bean.Discussion;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.HandyTextView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class UnAnsDiscussionFragment extends Fragment implements  View.OnClickListener,DialogInterface.OnClickListener,AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
	private int operation_position;
	private CustomToast CustomToast=new CustomToast();
		
	private Task mTask;
	private Project mProject;
	private Discussion mUnAnsDiscussion;
	
	private PullToRefreshListView mPullRefreshListView;	
	private LinkedList<Discussion> mUnAnsDiscussionList = new LinkedList<Discussion>();
	private AnsDiscussionAdapter listAdapter;

	private BaseDialog mBaseDialog;
	private ShortMessageDialog mAddTaskDialog;
	private LoadingDialog mWaitingAddTaskDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		mProject = (Project) getActivity().getIntent().getSerializableExtra("Project");
		mTask= (Task) getActivity().getIntent().getSerializableExtra("Task");
		listAdapter = new AnsDiscussionAdapter(getActivity(), mUnAnsDiscussionList);
		queryAnsDiscussions();
	}	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		mWaitingAddTaskDialog = new LoadingDialog(getActivity(),"Discussion Creation，Please Waiting...");
		View unansdiscussionfragment = inflater.inflate(R.layout.fragment_unansdiscussion, null);
		initiateViews(unansdiscussionfragment);
		mPullRefreshListView.setAdapter(listAdapter);	
		initiateEvents();
		
		return unansdiscussionfragment;
	}
	
	private void initiateViews(View unansdiscussionfragment)
	{			
		mBaseDialog   = new BaseDialog(getActivity());	
		mAddTaskDialog = new ShortMessageDialog(getActivity());
		
		mAddTaskDialog.setTitle("Add Discussion");
		mAddTaskDialog.setHint("Type Title");
		
		mBaseDialog.setTitle("Note");
		mBaseDialog.setMessage("Confirm Delete Disccussion? Comments Will De Delete");

		mPullRefreshListView = (PullToRefreshListView) unansdiscussionfragment.findViewById(R.id.unanswereddiscussion_listview);			
	}
	
	private void initiateEvents() {
		mBaseDialog.setButton1("确定",this);
		mBaseDialog.setButton2("取消",this);
		
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setOnItemLongClickListener(this);	
	}
	
	private void queryAnsDiscussions() {
		BmobQuery<Discussion> query = new BmobQuery<Discussion>();
		query.addWhereRelatedTo("mDiscussions", new BmobPointer(mTask));
		query.findObjects(new FindListener<Discussion>() {
			@Override
			public void done(List<Discussion> list, BmobException e) {
				if (e == null) {
					for (Discussion t : list)
						if(t.getState()==0)
						{mUnAnsDiscussionList.addFirst(t);}
					listAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void deleteDiscussion(int position){
		operation_position=position-1;
		mUnAnsDiscussion = mUnAnsDiscussionList.get(operation_position);
		if(BmobUser.getCurrentUser(User.class).getObjectId().equals(mProject.getLeader().getObjectId())){
			mBaseDialog.show();
		}else
		{
			CustomToast.showCustomToast("You Do Not Have Authority To Delete",getActivity());
		}
	}
	

	private class AnsDiscussionAdapter extends ArrayAdapter<Discussion> {
		public AnsDiscussionAdapter(Context context, List<Discussion> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_discussion, parent, false);
			}
			TextView mDiscussionFrom = (TextView) convertView.findViewById(R.id.discussion_textview_from);
			TextView mDiscussionTitle = (TextView) convertView.findViewById(R.id.discussion_textview_title);
			TextView mDiscussionDescription = (TextView) convertView.findViewById(R.id.discussion_textview_description);
			Button mCheck =(Button)convertView.findViewById(R.id.discussion_button_check);			
			
			if (getItem(position).getName() == null |getItem(position).getName().equals(""))
				mDiscussionFrom.setText("UNSET");
			else
				mDiscussionFrom.setText(getItem(position).getName());
			
			mDiscussionTitle.setText(getItem(position).getTitle());
			
			if (getItem(position).getDescription()==null |getItem(position).getDescription().equals(""))
				mDiscussionDescription.setText("UNSET");
			else
				mDiscussionDescription.setText(getItem(position).getDescription());
			
			mCheck.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View arg0)
				{
					Intent intent = new Intent(getActivity(),DiscussionDetailActivity.class);
					intent.putExtra("Project", mProject);
					intent.putExtra("Task", mTask);
					intent.putExtra("Discussion", getItem(position));
					startActivity(intent);	
					getActivity().finish();
					
				}
			});
			return convertView;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which==1) {
			dialog.cancel();
		} else {
			if(dialog==mBaseDialog) {
				mBaseDialog.dismiss();
				BmobRelation TaskDiscussion  = new BmobRelation();
				TaskDiscussion.remove(mUnAnsDiscussion);
				mTask.setDiscussions(TaskDiscussion);
				mTask.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							mUnAnsDiscussion.delete(new UpdateListener() {
								@Override
								public void done(BmobException e) {
									if (e == null) {
										CustomToast.showCustomToast("Discussion Delete Finished",getActivity());
										mUnAnsDiscussionList.remove(operation_position);
										listAdapter.notifyDataSetChanged();
									} else {
										CustomToast.showCustomToast("Discussion Delete Failed",getActivity());
									}
								}
							});
						} else {
							CustomToast.showCustomToast("Discussion Delete Failed",getActivity());
						}
					}
				});
			}
		}
		
	}

	public void AdapterNotify()
	{
		listAdapter.notifyDataSetChanged();;
	}
	
	public void UpdateList(Discussion mDiscussion)
	{mUnAnsDiscussionList.add(mDiscussion);}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
		// TODO Auto-generated method stub
		deleteDiscussion(position);
		return true;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
