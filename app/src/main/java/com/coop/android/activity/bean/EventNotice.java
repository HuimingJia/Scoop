package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobRelation;

public class EventNotice  extends BmobObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mTitle;
	private String mContent;
	private BmobDate mPostTime;
	private Boolean isRead;
	private User publisher;
	private BmobRelation mUser_EventNotice;
	
	public String getTitle(){return mTitle;}	
	public void setTile(String title){this.mTitle=title;}
	
	public String getContent(){return mContent;}	
	public void setContent(String content){this.mContent=content;}
	
	public BmobRelation getUser_EventNotice(){return mUser_EventNotice;}
	public void setUser_EventNotice(BmobRelation user_eventnotice){this.mUser_EventNotice=user_eventnotice;}

    public BmobDate getPostTime() {
		return mPostTime;
	}
	public void setPostTime(BmobDate mPostTime) {
		this.mPostTime = mPostTime;
	}
	public Boolean getIsRead() {
		return isRead;
	}
	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}
	public User getPublisher() {
		return publisher;
	}
	public void setPublisher(User publisher) {
		this.publisher = publisher;
	}
	@Override
    public String toString()
    {return "";}

}
