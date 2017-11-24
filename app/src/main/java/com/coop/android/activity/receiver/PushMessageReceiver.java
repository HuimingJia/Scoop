package com.coop.android.activity.receiver;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.coop.android.activity.ProjectActivity;
import com.coop.android.activity.R;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.bean.User_Project;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.toast.CustomToast;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PushMessageReceiver extends BroadcastReceiver implements OnClickListener{
	
	private JSONObject mjsonbject;
	private String RequesterEmail="";
	private String RequesterName="";

	private String ProjectID="";
	
	private String ReceiverID="";
	private String ReceiverEmail="";
	private String ReceiverName="";
	
	private String ReAction="";

	private Context mContext;
	private User mUser=new User();
	private Project mProject=new Project();
	
	private BaseDialog mBaseDialog;
	private BaseDialog mCallBackDialog;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext=context;
		  if(intent.getAction().equals(PushConstants.ACTION_MESSAGE))
		  {
			  String msg=intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			  JSONTokener jsonTokener = new JSONTokener(msg);
			  
			  try {
				mjsonbject =(JSONObject)jsonTokener.nextValue();
				msg =mjsonbject.getString("type");

				if(msg.equals("addrequest"))
				{		
					ReceiverName=mjsonbject.getString("receiverName");
					RequesterEmail=mjsonbject.getString("senderEmail");
					ReceiverID=mjsonbject.getString("receiverID");
					ProjectID=mjsonbject.getString("projectID");
					ReceiverEmail=mjsonbject.getString("receiverEmail");
				 	RequesterName=mjsonbject.getString("senderName");
					initiateBaseDialog();
					mBaseDialog.show();
				}
				else if(msg.equals("addreaction"))
				{
					ReceiverName=mjsonbject.getString("receiverName");
					ReAction=mjsonbject.getString("reaction");
					initiateBaseDialog();
					mCallBackDialog.show();
				}
				else if(msg.equals("message"))
				{
					// ReactionToMessage();
				}
				

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  

		  }
	}
	
	private void initiateBaseDialog()
	{
		mBaseDialog=new BaseDialog(mContext);
		mBaseDialog.setTitle("邀请加入团队");
		mBaseDialog.setMessage(RequesterName+"邀请你进入他的团队，是否同意请求?");
		
		
		mBaseDialog.setButton1("确定",this);
		mBaseDialog.setButton2("取消",this);
	}
	
	private void initiateCallBackDialog()
	{
		mCallBackDialog=new BaseDialog(mContext);
		mCallBackDialog.setTitle("邀请回复");
		mCallBackDialog.setMessage(ReceiverName+ReAction+"了你的邀请");
		
		
		mCallBackDialog.setButton1("确定",this);
	}
	
	private void SendCallBack(int callbackCode)
	{	
	    JSONObject data = new JSONObject();  
	    try {	    	
	    	data.put("type", "addreaction");
		if(callbackCode==0)
			data.put("reaction","拒绝");    
		else
			  data.put("reaction","同意");    

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    
		BmobPushManager push=new BmobPushManager(mContext);	
		BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
		query.addWhereEqualTo("mEmail",RequesterEmail);
		push.setQuery(query);
		push.pushMessage(data);;
	
		
	}
	
	private void AddProject()
	{
			
		BmobQuery<User> query_member = new BmobQuery<User>();
		query_member.addWhereEqualTo("username", ReceiverEmail);
		query_member.findObjects(mContext, new FindListener<User>() {

			@Override
			public void onSuccess(List<User> objects){			
				if(objects.size()>0)
				{   mUser = objects.get(0);
					BmobQuery<Project> query_p = new BmobQuery<Project>();
					query_p.addWhereEqualTo("mProjectID", ProjectID);
					query_p.findObjects(mContext, new FindListener<Project>() {

						@Override
						public void onSuccess(List<Project> objects){
							if(objects.size()>0)
							{   mProject = objects.get(0);
							
								User_Project mMember_Project= new User_Project();	
								mMember_Project.setProject(mProject);
								mMember_Project.setMember(mUser);
								mMember_Project.setEmail(mUser.getEmail());
								mMember_Project.setProjectID(mProject.getObjectId());						
								mMember_Project.save(mContext, new SaveListener() {
							
									@Override
									public void onSuccess() {
									}
									
									@Override
									public void onFailure(int arg0, String arg1) {
									}
								});						
							}
						}

						@Override
						public void onError(int arg0, String arg1) {
							// TODO Auto-generated method stub			
						}
					});
				}
				else
				{
				}
			}
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub			
			}
		});
	
	}
	

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(dialog==mBaseDialog)
		{
			if(which==0)
			{
				SendCallBack(0);
				AddProject();
				dialog.cancel();
			}
			else
			{
				SendCallBack(1);
				dialog.cancel();
			}
		}
		else if(dialog==mCallBackDialog)
		{
			dialog.cancel();
		}
		
	}

}