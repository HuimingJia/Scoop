package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;

public class User_Project extends BmobObject{
	
	private static final long serialVersionUID = 1L;
	private String mEmail;
	private String mProjectID;
	private User mMember;
	private Project mProject;
	
	
	public String getEmail()
	{return mEmail;}
	public void setEmail(String email)
	{this.mEmail=email;}
	
	public String getProjectID()
	{return mProjectID;}
	public void setProjectID(String projectID)
	{this.mProjectID=projectID;}
	
	public User getMember()
	{return mMember;}
	
	public void setMember(User user)
	{this.mMember=user;}
	
	public Project getProject()
	{return mProject;}
	
	public void setProject(Project Project)
	{this.mProject=Project;}
	
    @Override
    public String toString()
    {return "";}
}
