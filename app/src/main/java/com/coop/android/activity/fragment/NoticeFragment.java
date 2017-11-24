package com.coop.android.activity.fragment;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.QueryListener;

import com.coop.android.activity.R;
import com.coop.android.activity.TaskActivity;
import com.coop.android.activity.bean.EventNotice;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.NotificationDialog;
import com.coop.android.activity.toast.CustomToast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public class NoticeFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,OnRefreshListener<ListView>,DialogInterface.OnClickListener{

	//private ArrayList<EventNotice> mNoticeList;
	
	private EventNotice clickEventNotice;
	
	private int type;
	private int position_operation;
	
	private User mUser;
	private Button mWriNoticeBtn;
	
	private NotificationDialog mNoticeDialog;
	private BaseDialog mBaseDialog;
	private CustomToast CustomToast=new CustomToast();
	
	
	private ArrayList<User> mContactsList=new ArrayList<User>();
	private ArrayList<Project> mProjectList=	 new ArrayList<Project>();
	private ArrayList<String> mProjectIDList=	new ArrayList<String>();
	private ContactsAdapter mContactsAdapter;
	private ListView mContactsListView;
	private PullToRefreshListView mPullRefreshListView;	
	
	private LinkedList<EventNotice> mNoticeList = new LinkedList<EventNotice>();
	private NoticeAdapter listAdapter;
	private int [] selectedStatue={0};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		View noticefragment = inflater.inflate(R.layout.fragment_menuitem_notice,null,false);	
			
		mUser = BmobUser.getCurrentUser(User.class);
		queryProjects();
		initiateViews(noticefragment);
		initiateEvents();
		queryContacts();
		
		ViewGroup parent = (ViewGroup)noticefragment.getParent();
		if (parent != null)
			parent.removeView(noticefragment);	
		return noticefragment;
	}	
	
	private void initiateViews(View noticefragment) 
	{
		((TextView) getActivity().findViewById(R.id.activity_title)).setText("通知");
		mNoticeDialog = new NotificationDialog(getActivity());		
		mNoticeDialog.setTitle("发送消息");
		
		
		mPullRefreshListView = (PullToRefreshListView)noticefragment.findViewById(R.id.mEventNoticeListView);	
		mBaseDialog = new BaseDialog(getActivity());
		
		mWriNoticeBtn = ((Button) getActivity().findViewById(R.id.title_bar_right_menu));
		mWriNoticeBtn.setBackgroundResource(R.drawable.button_edit);	
		
		mContactsAdapter= new ContactsAdapter(getActivity(),mContactsList);
		mContactsListView=mNoticeDialog.getListView();
		mContactsListView.setAdapter(mContactsAdapter);
		mPullRefreshListView.setAdapter(listAdapter);
	}

	private void queryContacts()
	{
		mContactsList.clear();
		mContactsAdapter.clear();
		CustomToast.showCustomToast("projectresult"+mProjectList.size(),getActivity());
		for(int i=0;i<mProjectList.size();i++)
		{
			BmobQuery<User_Project> query = new BmobQuery<User_Project>();
			query.addWhereEqualTo("mProject", mProjectList.get(i).getObjectId().toString());
			query.findObjects(new FindListener<User_Project>() {
				@Override
				public void done(List<User_Project> list, BmobException e) {
					if (e == null) {
						CustomToast.showCustomToast("contactresult" + list.size(),getActivity());
						for (User_Project i : list) {
							BmobQuery<User> query_u = new BmobQuery<User>();
							query_u.getObject(i.getObjectId(), new QueryListener<User>() {
								@Override
								public void done(User user, BmobException e) {
									if (e == null) {
										if (user != null) {
											int i;
											for (i=0; i<mContactsList.size(); i++) {
												if(mContactsList.get(i).getObjectId().equals(user.getObjectId()))
													break;
											}
											if (i >= mContactsList.size()) {
												CustomToast.showCustomToast("AddAction",getActivity());
												mContactsList.add(user);
												mContactsAdapter.notifyDataSetChanged();
												selectedStatue=new int[mContactsList.size()];
												for (int n = 0; n < mContactsList.size(); n++)
													selectedStatue[n]=0;
											}
										}
									}
								}
							});
						}
					} else {
						CustomToast.showCustomToast("No Member Join In So Far!",getActivity());
					}
				}
			});
		}
	
	}
	
	private void queryProjects() {
		mProjectList.clear();
		mProjectIDList.clear();
		BmobQuery<Project> query = new BmobQuery<Project>();
		query.addWhereRelatedTo("mProjects_L", new BmobPointer(mUser));
		query.addWhereNotContainedIn("mProjectID", mProjectIDList);
		query.findObjects(new FindListener<Project>() {
			@Override
			public void done(List<Project> list, BmobException e) {
				if (e == null) {
					for (Project i : list) {
						if (mProjectList.indexOf(i) >= 0)
							continue;
						mProjectList.add(i);
						mProjectIDList.add(i.getProjectID());
					}
					listAdapter.notifyDataSetChanged();
				}
			}
		});

		BmobQuery<User_Project> query_m = new BmobQuery<User_Project>();
		query_m.addWhereEqualTo("mEmail", mUser.getEmail().toString());	
		query_m.addWhereNotContainedIn("mProjectID", mProjectIDList);
		query_m.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e == null) {
					for (User_Project i : list) {
						BmobQuery<Project> query_p = new BmobQuery<Project>();
						query_p.getObject(i.getProjectID(), new QueryListener<Project>() {
							@Override
							public void done(Project project, BmobException e) {
								if (e == null) {
									mProjectList.add(project);
									mProjectIDList.add(project.getProjectID());
									listAdapter.notifyDataSetChanged();
								} else {
									CustomToast.showCustomToast("Information Lost",getActivity());
								}
							}
						});
					}
				}
			}
		});
	}
	
	private void initiateEvents() 
	{
		mWriNoticeBtn.setOnClickListener(this);
		mPullRefreshListView.setOnRefreshListener(this);	
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setOnItemLongClickListener(this);
		mBaseDialog.setButton1("Sure",this);
		mBaseDialog.setButton2("Cancel",this);
		
		mNoticeDialog.setButton1("Sure",this);
		mNoticeDialog.setButton2("Cancel",this);
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUser=BmobUser.getCurrentUser(User.class);
		listAdapter = new NoticeAdapter(getActivity(), mNoticeList);
		queryNotice(0);
		setRetainInstance(true);
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==mWriNoticeBtn)
		{
			queryContacts();
			mNoticeDialog.show();
		}
		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1) {
			dialog.cancel();				
		} else {
			if (dialog == mBaseDialog) {
				
			} else if (dialog == mNoticeDialog) {
				pushMessage();
			}
		}
	}
	
	private void pushMessage() {
		for(int i=0; i < selectedStatue.length; i++) {
			if(selectedStatue[i]==1) {
				CustomToast.showCustomToast("cout<<id:"+mContactsList.get(i).getEmail(),getActivity());
			}
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		String label = DateUtils.formatDateTime(getActivity(),System.currentTimeMillis(),DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_DATE| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		new GetDataTask().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		deleteNotice(position);
		return true;
	}
	
	private void deleteNotice(int position) {
		this.position_operation=position;
		clickEventNotice = mNoticeList.get(position-1);
		mBaseDialog.show();
	}
	
	
	private void queryNotice(int refresh) {
		BmobQuery<EventNotice> query = new BmobQuery<EventNotice>();
		query.addWhereRelatedTo("mUser", new BmobPointer(mUser));

		if (refresh == 0) {
			query.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
			query.setMaxCacheAge(10000L);
		} else if (refresh == 1) {
			query.addWhereNotContainedIn("mTitle", mNoticeList);
		}

		query.findObjects(new FindListener<EventNotice>() {
			@Override
			public void done(List<EventNotice> list, BmobException e) {
				if (e == null) {
					for (EventNotice i : list) {
						if (mNoticeList.indexOf(i) >= 0)
							continue;
						mNoticeList.addFirst(i);
					}
					listAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private class NoticeAdapter extends ArrayAdapter<EventNotice> {
		public NoticeAdapter(Context context, List<EventNotice> objects)
		{
			super(context,0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if (convertView == null) 
			{
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_notice,parent,false);
			}
			
			//设置通知标题，内容，发布者，发布时间
			TextView tv_notice_title = (TextView) convertView.findViewById(R.id.tv_notice_title);
			TextView tv_notice_content = (TextView) convertView.findViewById(R.id.tv_notice_shortmsg);
			TextView tv_publisher = (TextView)convertView.findViewById(R.id.tv_publisher);
			TextView tv_publish_time = (TextView)convertView.findViewById(R.id.tv_publish_time);
			
			tv_notice_title.setText(getItem(position).getTitle());
			tv_notice_content.setText(getItem(position).getContent());
			tv_publisher.setText(getItem(position).getPublisher().getName());//need to feed
			tv_publish_time.setText(getItem(position).getPostTime().getDate());
		
			if (getItem(position).getIsRead()) {
				convertView.findViewById(R.id.tv_notice_unread).setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}


	
	private class GetDataTask extends AsyncTask<Void, Void, String[]>
	{
		@Override
		protected String[] doInBackground(Void... params)
		{
			try 
			{
				queryNotice(1);
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
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
	
	private class ContactsAdapter extends ArrayAdapter<User> {

		public ContactsAdapter(Context context, List<User> objects) {
			super(context, 0,objects);
		}
		@SuppressLint("ViewHolder")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			convertView = getActivity().getLayoutInflater().inflate(R.layout.item_listdialog, parent,false);

			String name = getItem(position).getName();
			String email = getItem(position).getEmail();
			if (name == null || name.equals("")) {
				name = "Unset";
			}
			
			final TextView textview = (TextView) convertView.findViewById(R.id.listitem_dialog_text);
			textview.setText(name + "(" + email + ")");
			textview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedStatue[position]==0) {
						textview.setBackgroundResource(R.drawable.background_corner);
						mContactsAdapter.notifyDataSetChanged();
						selectedStatue[position]=1;
					} else {
						textview.setBackgroundResource(R.drawable.background_corner3);
						mContactsAdapter.notifyDataSetChanged();
						selectedStatue[position]=0;
					}
				}
			});
			return convertView;
		}
	}
	
}
