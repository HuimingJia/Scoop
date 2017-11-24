package com.coop.android.activity.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Directory extends BmobObject{
	private static final long serialVersionUID = 1L;	
	public static final String FILE = "File";

	private String mEmail;
	private String mName;
	private Project mProject;
	private BmobRelation mDocuments;
	
	public String getEmail(){return mEmail;}	
	public void setEmail(String mEmail){this.mEmail=mEmail;}
	
	//对name（文件名）的操作
	public String getName(){return mName;}	
	public void setName(String name){this.mName=name;}
	
	//对Project的操作
    public Project getProject() {
		return mProject;
	}
	public void setProject(Project Project) {
		mProject = Project;
	}
	public void setDocuments(BmobRelation documents) {
		mDocuments = documents;
	}
	
	public BmobRelation getDocuments() {
		return mDocuments;
	}
	
}
