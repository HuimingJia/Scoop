package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Comment extends BmobObject {
	private static final long serialVersionUID = 1L;
	private String mContent;
	private Discussion mDiscussion;
	private User mCreator;
	private String mCreatorName;
	private BmobFile mCreatorPhoto;
	private int mRank;
	
	
	public BmobFile getCreatorPhoto()
	{
		return mCreatorPhoto;
	}
	public void setCreatorPhoto(BmobFile photo)
	{
		this.mCreatorPhoto=photo;
	}
	public int getRank()
	{
		return mRank;
	}
	public void setRank(int Rank)
	{
		this.mRank=Rank;
	}
	
    public String getCreatorName() {
		return mCreatorName;
	}

	public void setCreatorName(String creatorName) {
		this.mCreatorName = creatorName;
	}
	
    public String getContent() {
		return mContent;
	}

	public void setContent(String Content) {
		this.mContent = Content;
	}

	public Discussion getDisscusion() {
		return mDiscussion;
	}

	public void setDisscusion(Discussion discussion) {
		this.mDiscussion = discussion;
	}

	public User getCreator() {
		return mCreator;
	}

	public void setCreator(User creator) {
		this.mCreator = creator;
	}

}