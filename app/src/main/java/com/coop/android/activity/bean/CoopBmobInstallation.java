package com.coop.android.activity.bean;

import cn.bmob.v3.BmobInstallation;
import android.R.string;
import android.content.Context;

public class CoopBmobInstallation extends BmobInstallation {


//	public CoopBmobInstallation(Context context) {
//		super(context);
//		// TODO Auto-generated constructor stub
//	}
	private static final long serialVersionUID = 1L;

	private String mEmail;
	private String mObjectID;
	
	public String getObjectID()
	{
		return mObjectID;
	}
	public void setObjectID(String objectID)
	{
		this.mObjectID=objectID;
	}
	
    public String getEmail() {
        return mEmail;
    }
    public void setEmail(String Email) {
        this.mEmail = Email;
    }
}