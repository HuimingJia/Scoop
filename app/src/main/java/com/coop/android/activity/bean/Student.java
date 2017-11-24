package com.coop.android.activity.bean;

import cn.bmob.v3.datatype.BmobRelation;

public class Student extends User {

	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Student";
	
	private BmobRelation mStudent_Project;	
	
	public BmobRelation getStudent_Project() {return mStudent_Project;}
	public void setStudent_Project(BmobRelation student_Project) {this.mStudent_Project = student_Project;}

	
    @Override
    public String toString()
    {return "";}

}
