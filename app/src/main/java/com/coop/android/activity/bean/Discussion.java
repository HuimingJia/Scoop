package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Discussion extends BmobObject{

	private static final long serialVersionUID = 1L;
	private String mTitle;
	private String mName;
	private String mDescription;
	private int mState;
	
	private User mCreator;
	private Task mTask;
	
	private BmobRelation mComments;
	
	public String getName(){return mName;}	
	public void setName(String name){this.mName=name;}
	
	public Task getTask() {return mTask;}
	public void setTask(Task task) {this.mTask = task;}
	
	public String getTitle(){return mTitle;}	
	public void setTile(String title){this.mTitle=title;}
	
	public String getDescription(){return mDescription;}
	public void setDescription(String description){this.mDescription=description;}
	
	public int getState(){return mState;}	
	public void setState(int mState){this.mState=mState;}
	
	public User getUser(){return mCreator;}
	public void setUser(User Creator){this.mCreator=Creator;}
	
	public BmobRelation getComments(){return mComments;}
	public void setComments(BmobRelation mComments){this.mComments=mComments;}
	
    @Override
    public String toString()
    {return "";}

}
