package com.coop.android.activity.bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

public class User extends BmobUser {

	private static final long serialVersionUID = 1L;
	
	private String mName;
	private String mTel;
	
	private String mMajor;
	
	private BmobFile mPhoto;
	private BmobRelation mEventNotices;
	private BmobRelation mProjects_L;
	private BmobRelation mProjects_M;
	
	public BmobRelation getProjects_L()
	{return mProjects_L;}
	
	public void setProjects_L(BmobRelation mProjects_L)
	{this.mProjects_L=mProjects_L;}
	
	public BmobRelation getProjects_M()
	{return mProjects_M;}
	public void setProjects_M(BmobRelation mProjects_M)
	{this.mProjects_M=mProjects_M;}
		
	public String getName() {return mName;}
	public void setName(String name) {this.mName = name;}

	public String getTel() {return mTel;}
	public void setTel(String tel) {this.mTel = tel;}
	
	public BmobFile getPhoto() {return mPhoto;}
	public void setPhoto(BmobFile photo) {this.mPhoto = photo;}
	
	
	public String getMajor() {return mMajor;}
	public void setMajor(String mMajor) {this.mMajor = mMajor;}
	
	
	public BmobRelation getEventNotices() {return mEventNotices;}
	public void setEventNotices(BmobRelation user_eventnotices) {this.mEventNotices = user_eventnotices;}
	
    @Override
    public String toString()
    {return "";}
}
