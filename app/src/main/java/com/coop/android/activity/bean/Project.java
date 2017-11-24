package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobRelation;

public class Project extends BmobObject {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Project";

	private String mProjectName;
	private String mProjectContent;
	private BmobDate mProjectCreateTime;
	private String mProjectID;

	private User mLeader;	

	private BmobRelation mTasks;
	private BmobRelation mDirectories;
	
	public String getProjectID() {
		return mProjectID;
	}

	public void setProjectID(String projectID) {
		this.mProjectID = projectID;
	}

	public String getProjectName() {
		return mProjectName;
	}

	public void setProjectName(String ProjectName) {
		this.mProjectName = ProjectName;
	}

	public String getProjectContent() {
		return mProjectContent;
	}

	public void setProjectContent(String ProjectContent) {
		this.mProjectContent = ProjectContent;
	}

	public BmobDate getProjectCreateTime() {
		return mProjectCreateTime;
	}

	public  void setProjectCreateTime(BmobDate projectCreateTime) {
		this.mProjectCreateTime = projectCreateTime;
	}

	public User getLeader() {
		return mLeader;
	}

	public void setLeader(User leader) {
		this.mLeader = leader;
	}


	public BmobRelation getTasks() {
		return mTasks;
	}

	public void setTasks(BmobRelation tasks) {
		this.mTasks = tasks;
	}


	public BmobRelation getDirectories() {
		return mDirectories;
	}

	public void setDirectories(BmobRelation Directories) {
		this.mDirectories = Directories;
	}

	@Override
	public String toString() {
		return mProjectName;
	}
}
