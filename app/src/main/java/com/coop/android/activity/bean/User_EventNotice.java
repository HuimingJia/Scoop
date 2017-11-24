package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;

public class User_EventNotice extends BmobObject{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User mUser;
	private EventNotice mEventNotice;
	
	public User getUser(){return mUser;}	
	public void setUser(User user){this.mUser=user;}
	
	public EventNotice getEventNotice(){return mEventNotice;}
	public void setEventNotice(EventNotice eventNotice){this.mEventNotice=eventNotice;}
	
    @Override
    public String toString()
    {return "";}
}
