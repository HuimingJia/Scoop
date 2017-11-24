package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Reference extends BmobObject {
	private static final long serialVersionUID = 1L;
	private String mName;
	private String mType;
	private String mSize;
	private String mDescription;
	private String mEmail;
	
	private String mFileUrl;
	private String mFileName;

	//@junsheng use it 
	private Task mTask;
	private BmobFile mReference;

	
	public String getDescription()
	{
		return mDescription;
	}
	public void setDescription(String Description)
	{
		this.mDescription=Description;
	}
	
	public String getEmail()
	{
		return mEmail;
	}
	public void setgetEmail(String Email)
	{
		this.mEmail=Email;
	}
	
    public String getFileUrl() {
		return mFileUrl;
	}

	public void setFileUrl(String mFileUrl) {
		this.mFileUrl = mFileUrl;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	@Override
    public String toString()
    {return "";}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
	}

	public String getSize() {
		return mSize;
	}

	public void setSize(String size) {
		mSize = size;
	}

	public BmobFile getReference() {
		return mReference;
	}

	public void setReference(BmobFile reference) {
		mReference = reference;
	}

	public Task getTask() {
		return mTask;
	}

	public void setTask(Task task) {
		mTask = task;
	}

}
