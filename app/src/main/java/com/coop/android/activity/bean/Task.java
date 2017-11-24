package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobRelation;

public class Task extends BmobObject {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Task";

	private String mTaskName;
	private String mTaskContent;
	private BmobDate mTaskCreateTime;
	private BmobDate mTaskFinishTime;

	private BmobRelation mAssignments;
	private BmobRelation mDiscussions;
	private BmobRelation mReferences;
	private BmobRelation mCarriers;

	private Project mProject;

	public String getTaskName() {
		return mTaskName;
	}

	public void setTaskName(String taskName) {
		mTaskName = taskName;
	}

	public String getTaskContent() {
		return mTaskContent;
	}

	public void setTaskContent(String taskContent) {
		mTaskContent = taskContent;
	}

	public BmobDate getTaskCreateTime() {
		return mTaskCreateTime;
	}

	public void setTaskCreateTime(BmobDate taskCreateTime) {
		mTaskCreateTime = taskCreateTime;
	}

	public BmobDate getTaskFinishTime() {
		return mTaskFinishTime;
	}

	public void setTaskFinishTime(BmobDate taskFinishTime) {
		mTaskFinishTime = taskFinishTime;
	}

	public BmobRelation getAssigns() {
		return mAssignments;
	}

	public void setAssigns(BmobRelation assignments) {
		mAssignments = assignments;
	}

	public BmobRelation getDiscussions() {
		return mDiscussions;
	}

	public void setDiscussions(BmobRelation discussions) {
		mDiscussions = discussions;
	}
	
	public BmobRelation getCarriers() {
		return mCarriers;
	}

	public void setCarriers(BmobRelation carriers) {
		mCarriers = carriers;
	}

	public BmobRelation getReferences() {
		return mReferences;
	}

	public void setReferences(BmobRelation references) {
		mReferences = references;
	}

	public Project getProject() {
		return mProject;
	}

	public void setProject(Project Project) {
		mProject = Project;
	}

    @Override
    public String toString()
    {return "";}


}
