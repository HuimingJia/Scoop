package com.coop.android.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;


import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import com.coop.android.activity.bean.Assignment;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Student;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.DoubleEditDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.dialog.ListDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.HandyTextView;
import com.coop.android.activity.view.HorizontalListView;

public class TaskActivity extends FragmentActivity implements OnClickListener, OnItemClickListener, OnItemLongClickListener, android.content.DialogInterface.OnClickListener, OnLongClickListener {
	private CustomToast CustomToast=new CustomToast();
	private int operation_position;
	private Project mProject;
	private Task mTask;
	private User mUser;
	private Assignment mAssignment;
	
	private Button mBackBtn;
	private Button mEditBtn;
	private Button mAddCarrierBtn;
	private Button mAddAssignBtn;
	
	private EditText mTitle;// The title of Task
	private RelativeLayout mReferenceBtn;// Reference information button
	private RelativeLayout mDiscussionBtn;// Problem information button
	
	private DoubleEditDialog mAssignDialog;
	private LoadingDialog mFlippingLoadingDialog;
	private ShortMessageDialog mEditTitleDialog;
	private BaseDialog mConfDelCarDialog;// confirm dialog
	private BaseDialog mConfDelAssDialog;// confirm dialog
	private ListDialog mMemberListDialog;
	
	private ArrayList<User> mCarriersList = new ArrayList<User>();// all stages for current tasks
	private ArrayList<User> mCandidateList = new ArrayList<User>();
	private ArrayList<Assignment> mAssignmentList =new ArrayList<Assignment>();
	
	private ListView mCandidateView;
	private ListView mAssListView;
	private HorizontalListView mCarriersListView;
	
	private CarrierAdapter mCarriersAdapter;
	private AssignmentAdapter mAssignmentAdapter;
	private CandidateAdapter mCandidateAdapter; 
	private int [] selectedStatue;
	
	private boolean mEditOrSave = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_taskdetail);
		mUser = BmobUser.getCurrentUser(User.class);
		mProject = (Project)getIntent().getSerializableExtra("Project");
		mTask 	 = (Task)getIntent().getSerializableExtra("Task");
				
		initViews();	
		initEvents();
		queryAssignments();
		queryCarriers();
		queryCandidates();	
	}
	
	private void queryAssignments() {
		mAssignmentList.clear();
		mAssignmentAdapter.clear();
		BmobQuery<Assignment> query_a = new BmobQuery<Assignment>();		
		query_a.addWhereRelatedTo("mAssignments", new BmobPointer(mTask));
		query_a.order("createdAt");
		query_a.findObjects(new FindListener<Assignment>() {
			@Override
			public void done(List<Assignment> list, BmobException e) {
				for (int i = 0; i < list.size(); i++) {
					mAssignmentList.add(i, list.get(i));
					mAssignmentAdapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	private void queryCarriers() {
		mCarriersList.clear();
		mCarriersAdapter.clear();
		BmobQuery<User> query_u = new BmobQuery<User>();		
		query_u.addWhereRelatedTo("mCarriers", new BmobPointer(mTask));
		query_u.findObjects(new FindListener<User>() {
			@Override
			public void done(List<User> list, BmobException e) {
				for (User u : list) {
					if (mCarriersList.indexOf(u) >= 0) {
						continue;
					}
					mCarriersList.add(u);
				}
				mCarriersAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private void queryCandidates() {
		mCandidateList.clear();
		mCandidateAdapter.clear();
		final BmobQuery<User_Project> query = new BmobQuery<User_Project>();
		query.addWhereEqualTo("mProject", mProject.getObjectId().toString());
		query.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e != null) {
					for (User_Project i : list) {
						BmobQuery<User> query_u = new BmobQuery<User>();
						query_u.getObject(i.getObjectId(), new QueryListener<User>() {
							@Override
							public void done(User user, BmobException e) {
								if (e == null) {
									if (user != null) {
										int i;
										for (i = 0; i < mCarriersList.size(); i++) {
											if (mCarriersList.get(i).getEmail().equals(user.getEmail()))
												break;
										}

										if(i >= mCarriersList.size()) {
											mCandidateAdapter.notifyDataSetChanged();
											selectedStatue=new int[mCandidateList.size()];
											for (int n=0; n<mCandidateList.size(); n++)
												selectedStatue[n] = 0;
										}

									}
								}
							}
						});
					}
				} else {
					CustomToast.showCustomToast("No Member Join In Right Now!",TaskActivity.this);
				}
			}
		});
	}
	
	

	private void initViews() {
		mBackBtn = (Button) findViewById(R.id.task_button_back);	
		mEditBtn	 = (Button) findViewById(R.id.btn_task_edit);
		
		mTitle = (EditText) findViewById(R.id.task_textview_title);
		mTitle.setText(mTask.getTaskName());
		
		mAddCarrierBtn = (Button) findViewById(R.id.task_button_addcarrier);
		mAddAssignBtn	 = (Button) findViewById(R.id.task_button_addassign);
		
		mCarriersAdapter = new CarrierAdapter(this,mCarriersList);
		mCandidateAdapter= new CandidateAdapter(this,mCandidateList);
		mAssignmentAdapter =new AssignmentAdapter(this,mAssignmentList); 
		
		mCarriersListView = (HorizontalListView) findViewById(R.id.task_listview_carrier);
		mCarriersListView.setAdapter(mCarriersAdapter);
		
		mAssListView = (ListView) findViewById(R.id.task_listView_assigns);
		mAssListView.setAdapter(mAssignmentAdapter);

		mEditTitleDialog= new ShortMessageDialog(this);
		mAssignDialog = new DoubleEditDialog(this);
		mMemberListDialog = new ListDialog(TaskActivity.this);
		mFlippingLoadingDialog = new LoadingDialog(this, "Update Saving....");
		
		mCandidateView=mMemberListDialog.getListView();
		mCandidateView.setAdapter(mCandidateAdapter);
		
		mConfDelCarDialog = new BaseDialog(this);
		mConfDelAssDialog = new BaseDialog(this);
		

		mEditTitleDialog.setTitle("Type Title");;
		mEditTitleDialog.setHint(mTask.getTaskName());
		
		mConfDelCarDialog.setTitle("Delete Member");
		mConfDelCarDialog.setMessage("Are You Sure Delete Member, Will Not Send Notification");
		
		mConfDelAssDialog.setTitle("Delete Assignment");
		mConfDelAssDialog.setMessage("Are You Sure Delete Assignment, irrevocable");
		
		mAssignDialog.setTitle("Add Assignment");
		mAssignDialog.setNameHint("Assignment Title");
		mAssignDialog.setDescHint("Assignment Desc");
		
		mMemberListDialog.setTitle("Add Executor");
		mMemberListDialog.setTitleLineVisibility(View.GONE);
		mCandidateView.setAdapter(mCandidateAdapter);
				
		mReferenceBtn = (RelativeLayout) findViewById(R.id.layout_reference);
		mDiscussionBtn = (RelativeLayout) findViewById(R.id.layout_discussion);	
	}

	private void initEvents() {
		mTitle.setOnLongClickListener(this);
		mBackBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);

		mAddAssignBtn.setOnClickListener(this);
		mAddCarrierBtn.setOnClickListener(this);

		mReferenceBtn.setOnClickListener(this);
		mDiscussionBtn.setOnClickListener(this);

		mCarriersListView.setOnItemLongClickListener(this);

		mAssListView.setOnItemClickListener(this);
		mAssListView.setOnItemLongClickListener(this);

		mConfDelCarDialog.setButton1("Sure",this);
		mConfDelCarDialog.setButton2("Cancel",this);

		mConfDelAssDialog.setButton1("Sure",this);
		mConfDelAssDialog.setButton2("Cancel",this);

		mMemberListDialog.setButton1("Sure",this);
		mMemberListDialog.setButton2("Cancel",this);

		mAssignDialog.setButton1("Sure",this);
		mAssignDialog.setButton2("Cancel",this);

		mEditTitleDialog.setButton1("Sure",this);
		mEditTitleDialog.setButton2("Cancel",this);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		operation_position=position;
		if (parent == mAssListView) {
			mConfDelAssDialog.show();
		} else if (parent == mCarriersListView) {
			operation_position=position;
			mConfDelCarDialog.show();
		} else if (parent == mAssListView) {
			operation_position=position;
			mConfDelAssDialog.show();
		}
		return true;
	}
	
	private void DeleteAss() {
		mAssignment=mAssignmentList.get(operation_position);
		mAssignment.delete(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Assignment Finished", TaskActivity.this);
				}
			}
		});

		BmobRelation assignments = new BmobRelation();
		assignments.remove(mAssignment);
		mTask.setAssigns(assignments);

		mTask.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Delete Finished",TaskActivity.this);
					mAssignmentList.remove(operation_position);
					mAssignmentAdapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	private void DeleteCarrier() {
		BmobRelation carriers = new BmobRelation();
		carriers.remove(mCarriersList.get(operation_position));
		mTask.setCarriers(carriers);
		mTask.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Delete Finished",TaskActivity.this);
					mCarriersList.remove(operation_position);
					mCarriersAdapter.notifyDataSetChanged();
				}
			}
		});
	}
		
	private void AddAssignment(String name,String descrip) {
		mAssignment=new Assignment();
		mAssignment.setAssName(name);
		mAssignment.setAssRequirement(descrip);
		mAssignment.setAssStartTime(new BmobDate(new Date()));
		mAssignment.setIsFinished(false);
		mAssignment.setTask(mTask);
		mAssignment.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e == null) {
					BmobRelation mAssignments = new BmobRelation();
					mAssignments.add(mAssignment);
					mTask.setAssigns(mAssignments);
					new saveTaskEdited().execute();
				}
			}
		});
	}
	
	private void AddCarrier() {
		BmobRelation mCarriers = new BmobRelation();
		for(int i=0; i < selectedStatue.length; i++)
			if (selectedStatue[i] == 1) {
				mCarriers.add(mCandidateList.get(i));
				mTask.setCarriers(mCarriers);			
			}	
	}
	
	private void SaveEditTitle() {
		mFlippingLoadingDialog.show();
		mTask.setTaskName(mEditTitleDialog.getText().toString());
		mTask.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Update Finished",TaskActivity.this);
					mTitle.setText(mEditTitleDialog.getText().toString());
				}
			}
		});

		mFlippingLoadingDialog.dismiss();
	}
	
	private class saveTaskEdited extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mFlippingLoadingDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				mTask.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							CustomToast.showCustomToast("Update Finished",TaskActivity.this);
							queryAssignments();
							queryCarriers();
							queryCandidates();
						}
					}
				});
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mFlippingLoadingDialog.dismiss();
			if (result) {
				CustomToast.showCustomToast("Uodate Finished",TaskActivity.this);
				mAddCarrierBtn.setVisibility(View.GONE);
				mAddAssignBtn.setVisibility(View.GONE);
				mEditBtn.setBackgroundResource(R.drawable.button_edit);
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(parent==mCandidateView) {

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mBackBtn) {
			Intent intent_back = new Intent(TaskActivity.this,ProjectActivity.class);
			intent_back.putExtra("Project",mProject);
			startActivity(intent_back);
			finish();
		} else if (v == mEditBtn) {
			if (!mEditOrSave) {
				if(!mProject.getLeader().getObjectId().equals(BmobUser.getCurrentUser(User.class).getObjectId())) {
					CustomToast.showCustomToast("You Do Not Have Authority To Operate",TaskActivity.this);
				} else {
						mAddAssignBtn.setVisibility(View.VISIBLE);
						mAddCarrierBtn.setVisibility(View.VISIBLE);
						mEditBtn.setBackgroundResource(R.drawable.button_confirm);
				}
			} else {
				mAddCarrierBtn.setVisibility(View.GONE);
				mAddAssignBtn.setVisibility(View.GONE);
				mEditBtn.setBackgroundResource(R.drawable.button_edit);
				
			}
			mEditOrSave = !mEditOrSave;
		} else if (v == mAddAssignBtn) {
			if (mCarriersList.size() == 0) {
				CustomToast.showCustomToast("Topic Has Zero Member, Please Add Member First!",TaskActivity.this);
			} else {
				mAssignDialog.show();
			}
		} else if (v == mAddCarrierBtn) {
			//mCandidateList.clear();	
			//mCandidateAdapter.clear();
			queryCandidates();
			mMemberListDialog.show();
		} else if (v == mReferenceBtn) {
			Intent intent = new Intent(this,ReferencesActivity.class);
			intent.putExtra("Project",mProject);
			intent.putExtra("Task",mTask);
			startActivity(intent);
			finish();
		} else if (v == mDiscussionBtn) {
			Intent intent = new Intent(this,DiscussionsActivity.class);
			intent.putExtra("Project",mProject);
			intent.putExtra("Task",mTask);
			startActivity(intent);
			finish();
		}	
	}
	
	private class CarrierAdapter extends ArrayAdapter<User> {
		public CarrierAdapter(Context context, List<User> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = TaskActivity.this.getLayoutInflater().inflate(R.layout.item_carrier, parent, false);
			}
			TextView mTaskName = (TextView) convertView.findViewById(R.id.task_textview_carrier);
	
			
			if((getItem(position).getName()).equals("") || (getItem(position).getName() == null))
				mTaskName.setText("UNSET");
			else
				mTaskName.setText(getItem(position).getName());
			return convertView;
		}
	}
	
	private class CandidateAdapter extends ArrayAdapter<User> {
		public CandidateAdapter(Context context, List<User> objects) {
			super(context, 0,objects);
		}
		@SuppressLint("ViewHolder")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			convertView = TaskActivity.this.getLayoutInflater().inflate(R.layout.item_listdialog, parent,false);
			String name = getItem(position).getName();
			String email = getItem(position).getEmail();
			if (name == null || name == "")
				name="UNSET";
			
			final TextView textview=(TextView) convertView.findViewById(R.id.listitem_dialog_text);
			textview.setText(name + "(" + email + ")");
			textview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(selectedStatue[position] == 0) {
						textview.setBackgroundResource(R.drawable.background_corner);
						mCandidateAdapter.notifyDataSetChanged();				
						selectedStatue[position] = 1;
					} else {
						textview.setBackgroundResource(R.drawable.background_corner3);
						mCandidateAdapter.notifyDataSetChanged();
						selectedStatue[position] = 0;
					}				
				}
			});
			return convertView;
		}
	}
	
	
	private class AssignmentAdapter extends ArrayAdapter<Assignment> {
		public AssignmentAdapter(Context context, List<Assignment> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			//if(convertView==null)
			convertView = TaskActivity.this.getLayoutInflater().inflate(R.layout.item_assignment, parent, false);
			TextView mAssName = (TextView) convertView.findViewById(R.id.assignment_textview_name);
			TextView mAssDesc = (TextView) convertView.findViewById(R.id.assignment_textview_desc);
			TextView mAssStartTime = (TextView) convertView.findViewById(R.id.assignment_textview_starttime);
			
			mAssName.setText(getItem(position).getAssName());
			mAssDesc.setText(getItem(position).getAssRequirement());
			mAssStartTime.setText(getItem(position).getAssStartTime().getDate().toString());
						
			TextView mAssFinishTime = (TextView) convertView.findViewById(R.id.assignment_textview_finishtime);				
			final ImageView imageview = (ImageView) convertView.findViewById(R.id.assignment_button_finish);
			final RelativeLayout layout = (RelativeLayout)convertView.findViewById(R.id.assignment_button_layout);
			
			final int position_o = position;
			if (getItem(position).getIsFinished()) {
				imageview.setBackgroundResource(R.drawable.background_circlebutton_clicked);
				layout.setBackgroundResource(R.drawable.background_assboard_finished);
				mAssFinishTime.setText(getItem(position).getAssFinishTime().getDate().toString());
			}
			
			imageview.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(!mAssignmentList.get(position_o).getIsFinished()) {
						operation_position=position_o;
						BaseDialog mConfDialog = new BaseDialog(TaskActivity.this);// Dialog
						mConfDialog.setTitle("Finish Task");
						mConfDialog.setMessage("Confirm Finish Task, Can Not Converse!");
						mConfDialog.show();
						mConfDialog.setButton1("Sure", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							//	CustomToast.showCustomToast("Assignfinish:position"+position_o,TaskActivity.this);
								imageview.setBackgroundResource(R.drawable.background_circlebutton_clicked);
								layout .setBackgroundResource(R.drawable.background_assboard_finished);
								imageview.setEnabled(false);
								getItem(position).setIsFinished(true);
								getItem(position).setAssFinishTime(new BmobDate(new Date()));
								getItem(position).update(new UpdateListener() {
									@Override
									public void done(BmobException e) {
										if (e == null) {
											CustomToast.showCustomToast("Operation Finished",TaskActivity.this);
											queryAssignments();
										}
									}
								});
								//FinishAssign();
								dialog.cancel();
							}
						});
						mConfDialog.setButton2("Cancel",new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						});
					}
					//mFinishTime=new ArrayList<TextView>();
				}
			});
			return convertView;
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1)
			dialog.cancel();
		else {
			if (dialog == mConfDelCarDialog) {
				DeleteCarrier();
				dialog.cancel();
			} else if(dialog == mConfDelAssDialog) {
				DeleteAss();
				dialog.cancel();
			} else if(dialog == mAssignDialog) {
				String name = mAssignDialog.getNameText();
				String descript = mAssignDialog.getDescText();
				if (name == null || name.equals(""))
					name="UNSET";
				if (descript == null || descript.equals(""))
					descript="UNSET";
				dialog.cancel();
				AddAssignment(name,descript);
				//new saveTaskEdited().execute(); 
			} else if (dialog == mMemberListDialog) {
				AddCarrier();
				dialog.cancel();
				new saveTaskEdited().execute();
			} else if (dialog == mEditTitleDialog) {
				if (mEditTitleDialog.getText() != null && !mEditTitleDialog.getText().equals("")) {
					SaveEditTitle();
					dialog.cancel();
				} else
					CustomToast.showCustomToast("Can Not Be Empty",TaskActivity.this);
				
			}
		}
		
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if (v == mTitle) {
			if (!mProject.getLeader().getObjectId().equals(BmobUser.getCurrentUser(User.class).getObjectId())) {
				CustomToast.showCustomToast("You Do Not Have Authority To Operate",TaskActivity.this);
			} else {
				mEditTitleDialog.show();
			}
		}
		return true;
	}

}
