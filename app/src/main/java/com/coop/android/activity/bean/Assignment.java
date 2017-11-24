package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;

public class Assignment extends BmobObject {

	private static final long serialVersionUID = 1L;

	private String mTaskID;
	private String mAssName;
	private String mAssRequirement;
	private BmobDate mAssStartTime;
	private BmobDate mAssFinishTime;
	private Boolean isFinished = false;

	private Task mTask;

	public String getTaskID() {
		return mTaskID;
	}

	public void setTask(String taskID) {
		mTaskID = taskID;
	}
	
	public Task getTask() {
		return mTask;
	}
	public void setTask(Task task) {
		mTask = task;
	}

	public String getAssName() {
		return mAssName;
	}

	public void setAssName(String assName) {
		mAssName = assName;
	}

	public String getAssRequirement() {
		return mAssRequirement;
	}


	public void setAssRequirement(String assRequirement) {
		mAssRequirement = assRequirement;
	}

	public BmobDate getAssStartTime() {
		return mAssStartTime;
	}

	public void setAssStartTime(BmobDate assStartTime) {
		mAssStartTime = assStartTime;
	}

	public BmobDate getAssFinishTime() {
		return mAssFinishTime;
	}

	public void setAssFinishTime(BmobDate assFinishTime) {
		mAssFinishTime = assFinishTime;
	}



	public Boolean getIsFinished() {
		return isFinished;
	}

	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}

	@Override
	public String toString() {
		return "Assignment [mAssName=" + mAssName + ", mAssRequirement="
				+ mAssRequirement + ", mAssStartTime=" + mAssStartTime
				+ ", mAssFinishTime=" + mAssFinishTime + ", isFinished="
				+ isFinished + ", mTask=" + mTask + "]";
	}
	
}
