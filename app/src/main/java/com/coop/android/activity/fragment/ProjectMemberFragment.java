package com.coop.android.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.coop.android.activity.R;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Student;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.CircleImageView;
import com.coop.android.activity.view.HandyTextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

/**
 * @author SS 导航栏对应的成员模块
 */
public class ProjectMemberFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,DialogInterface.OnClickListener {
	private CircleImageView tportrait;
	private TextView tname;
	private TextView temail;
	private TextView tnumber;
	private Button mAddMemberBtn;
	
	private String Email;
	private int opertion_position;

	private BaseDialog mConfirmAddDialog;
	private BaseDialog mConfirmDelMember;
	private ShortMessageDialog mAddMemberDialog;
	private CustomToast CustomToast=new CustomToast();

	private Project mProject;
	private User mMember;
	private User currentUser;
	private User_Project mMember_Project;

	private ListView mListView;		
	private LinkedList<User> mMembersList = new LinkedList<User>();
	private MembersAdapter listAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProject = (Project) getActivity().getIntent().getSerializableExtra("Project");
		listAdapter = new MembersAdapter(getActivity(), mMembersList);
		queryMembers();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		currentUser=BmobUser.getCurrentUser(User.class);
		View Projectmemberfragment = inflater.inflate(R.layout.fragment_memberlist, null,false);		
		initiateViews(Projectmemberfragment);
		mListView.setAdapter(listAdapter);
		initiateEvents();
		ViewGroup parent = (ViewGroup)Projectmemberfragment.getParent();
		if (parent != null)
			parent.removeView(Projectmemberfragment);
		return Projectmemberfragment;
	}
	
	private void initiateViews(View Projectmemberfragment) {
		((TextView) getActivity().findViewById(R.id.project_activity_title)).setText(mProject.getProjectName());
		
		tportrait = (CircleImageView)Projectmemberfragment.findViewById(R.id.memberlist_image_tportrait);
		mListView = (ListView) Projectmemberfragment.findViewById(R.id.memberlist);
		
		tname = (TextView) Projectmemberfragment.findViewById(R.id.memberlist_textview_tname);
		temail = (TextView) Projectmemberfragment.findViewById(R.id.memberlist_textview_temail);
		tnumber = (TextView) Projectmemberfragment.findViewById(R.id.memberlist_textview_tnumber);
		mAddMemberBtn = (Button) getActivity().findViewById(R.id.btn_add_member);

		mAddMemberDialog = new ShortMessageDialog(getActivity());
		mConfirmAddDialog = new BaseDialog(getActivity());
		mConfirmDelMember = new BaseDialog(getActivity());
		
		mAddMemberDialog.setTitle("Add New Member");
		mAddMemberDialog.setHint("Member's Email");

		mConfirmAddDialog.setTitle("Confirm Message");
		mConfirmDelMember.setTitle("Confirm Message");
		mConfirmDelMember.setMessage("Are You Sure Remove This Member?");
		
		BmobQuery<User> query_t = new BmobQuery<User>();
		query_t.getObject(mProject.getLeader().getObjectId(), new QueryListener<User>() {
			@Override
			public void done(User user, BmobException e) {
				if (e == null) {
					if (user.getName()==null) {
						tname.setText("UNSET");
					} else {
						tname.setText(user.getName());
					}

					if(user.getTel().equals("") || user.getTel() == null) {
						temail.setText("UNSET");
					} else {
						tnumber.setText(user.getTel());
					}

					if(user.getPhoto()!=null) {
						tportrait.setImageResource(R.drawable.blade);
//						user.getPhoto().loadImage(getActivity(),tportrait);
					}
					temail.setText(user.getEmail()+ "");
				} else {
					CustomToast.showCustomToast("Can Not Get Creator Information！",getActivity());
				}
			}
		});
	}
	
	private void initiateEvents() {
		mAddMemberBtn.setOnClickListener(this);
		mAddMemberDialog.setButton1("Sure", this);
		mAddMemberDialog.setButton2("Cancel", this);

		mConfirmAddDialog.setButton1("Sure",this);
		mConfirmAddDialog.setButton2("Cancel",this);
		
		mConfirmDelMember.setButton1("Sure",this);
		mConfirmDelMember.setButton2("Cancel",this);
			
		mListView.setOnItemClickListener(this);		
		mListView.setOnItemLongClickListener(this);	
	}

	
	private void queryMembers() {
		BmobQuery<User_Project> query = new BmobQuery<User_Project>();
		query.addWhereEqualTo("mProject", mProject.getObjectId().toString());
		query.findObjects(new FindListener<User_Project>() {
			@Override
			public void done(List<User_Project> list, BmobException e) {
				if (e == null) {
					for (User_Project i : list){
						BmobQuery<User> query_s = new BmobQuery<User>();
						query_s.getObject(i.getMember().getObjectId(), new QueryListener<User>() {
							@Override
							public void done(User user, BmobException e) {
								if (e == null) {
									User member=new Student();
									member.setPhoto(user.getPhoto());
									member.setName(user.getName());
									member.setTel(user.getTel());
									member.setEmail(user.getEmail());
									mMembersList.add(member);
									listAdapter.notifyDataSetChanged();
								} else {
									CustomToast.showCustomToast("Can Not Get Member Information",getActivity());
								}
							}
						});
					}
				} else {
					CustomToast.showCustomToast("No Memeber Join In So Far",getActivity());
				}
			}
		});
	}
	
	private void queryMember() {	
		BmobQuery<User> query_member = new BmobQuery<User>();
		query_member.addWhereEqualTo("username", Email);
		query_member.findObjects(new FindListener<User>() {
			@Override
			public void done(List<User> list, BmobException e) {
				if (e == null) {
					if(list != null && list.size() > 0) {
						mMember = list.get(0);
						BmobQuery<User_Project> query_exist = new BmobQuery<User_Project>();
						query_exist.addWhereEqualTo("mEmail", Email);
						query_exist.addWhereEqualTo("mProject", mProject.getObjectId().toString());
						query_exist.findObjects(new FindListener<User_Project>() {
							@Override
							public void done(List<User_Project> list, BmobException e) {
								if (e == null) {
									if (list.size() > 0) {
										CustomToast.showCustomToast("The Member Arealdy In",getActivity());
									} else {
										if (mMember.getName() == null) {
											mConfirmAddDialog.setMessage("Are You Sure Add The User As Member?");
										} else {
											mConfirmAddDialog.setMessage("Confirm Add‘"+mMember.getName()+"'As Member");
										}
										mConfirmAddDialog.show();
									}
								} else {
									if(mMember.getName()==null) {
										mConfirmAddDialog.setMessage("Are You Sure Add The User As Member?");
									} else {
										mConfirmAddDialog.setMessage("Confirm Add‘"+mMember.getName()+"'As Member");
									}
									mConfirmAddDialog.show();
								}
							}
						});
					}
					else
					{
						CustomToast.showCustomToast("Can Not Find Student",getActivity());
					}
				}
			}
		});
	}
	
	private void deleteMember(int position) {
		this.opertion_position = position;
		mMember = mMembersList.get(opertion_position);
		mConfirmDelMember.show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub
		deleteMember(position);
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 0) {
//			(List<Student> objects){
//								if(objects.size()>0)
//								{
//									mStudent = objects.get(0);
//									BmobRelation studentProject = new BmobRelation();
//									BmobRelation ProjectStudent = new BmobRelation();
//
//									studentProject.remove(objects.get(0));
//									ProjectStudent.remove();
//
//									mProject.setProject_Student(ProjectStudent);
//									objects.get(0).setStudent_Project(studentProject);
//
//									mProject.update(getActivity(), new UpdateListener() {
//
//									        @Override
//									        public void onSuccess() {
//									            // TODO Auto-generated method stub
//									        }
//
//									        @Override
//									        public void onFailure(int arg0, String arg1) {
//									            // TODO Auto-generated method stub
//									            CustomToast.showCustomToast("很遗憾，移除失败", getActivity());
//									        }
//									    });
//
//									objects.get(0).update(getActivity(), new UpdateListener() {
//
//								        @Override
//								        public void onSuccess() {
//								            // TODO Auto-generated method stub
//								            CustomToast.showCustomToast("在用户信息中已成功移除该银行卡信息", getActivity());
//											mStudentList.remove(opertion_position-1);
//											listAdapter.notifyDataSetChanged();
//								        }
//
//								        @Override
//								        public void onFailure(int arg0, String arg1) {
//								            // TODO Auto-generated method stub
//								        	 CustomToast.showCustomToast("很遗憾，移除失败2", getActivity());
//								        }
//								    });
//								}
//
//							}
//
//							@Override
//							public void onError(int arg0, String arg1) {
//								// TODO Auto-generated method stub
//							}
//						});
//
//
//					}
//					else
//					{
//						CustomToast.showCustomToast("你无权删除成员",getActivity());
//					}
//				}
		} else if(which==1) {
			dialog.cancel();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mAddMemberBtn) {
			mAddMemberDialog.show();
		}
	}
	
	
	private class MembersAdapter extends ArrayAdapter<User> {
		public MembersAdapter(Context context, List<User> objects)
		{
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_member, parent, false);
			}
			
			if (getItem(position).getPhoto() != null) {
				convertView.findViewById(R.id.memberlist_image_sportrait);
//				convertView.
//				getItem(position).getPhoto().loadImage(getActivity(),((CircleImageView)convertView.findViewById(R.id.memberlist_image_sportrait)),80,80);
			}
				
			if (getItem(position).getName() != null) {
				((TextView) convertView.findViewById(R.id.memberlist_textview_sname)).setText(getItem(position).getName());
			} else {
				((TextView) convertView.findViewById(R.id.memberlist_textview_sname)).setText("UNSET");
			}
			
			((TextView) convertView.findViewById(R.id.memberlist_textview_semail)).setText(getItem(position).getEmail());
			
			if(getItem(position).getTel()!=null) {
				((TextView) convertView.findViewById(R.id.memberlist_textview_snumber)).setText(getItem(position).getTel());
			} else {
				((TextView) convertView.findViewById(R.id.memberlist_textview_snumber)).setText("UNSET");
			}
			return convertView;
		}
	}
	
	private void pushAddRequest() {
	    JSONObject data = new JSONObject();  
	    try {
			data.put("type", "addrequest");
			data.put("projectID",mProject.getObjectId());
			data.put("receiverID",mMember.getObjectId());
			data.put("receiverEmail",mMember.getEmail());
			data.put("receiverName", mMember.getName() == null ? "UNSET" : mMember.getName());
			data.put("senderEmail", currentUser.getEmail());
			data.put("senderName",currentUser.getName() == null ? "UNSET" : currentUser.getName());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    
		BmobPushManager push=new BmobPushManager();
		BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
		query.addWhereEqualTo("mEmail",mMember.getEmail());
		push.setQuery(query);
		push.pushMessage(data);;
	}
	
	private void addMemberToProject() {	
		mMember_Project= new User_Project();	
		mMember_Project.setProject(mProject);
		mMember_Project.setMember(mMember);
		mMember_Project.setEmail(mMember.getEmail());
		mMember_Project.setProjectID(mProject.getObjectId());
		mMember_Project.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Add Memeber Finished",getActivity());
					mMembersList.addFirst(mMember);
					listAdapter.notifyDataSetChanged();
				} else {
					CustomToast.showCustomToast("Add Memeber Failed" + e.toString(), getActivity());
				}
			}
		});
	}


}


