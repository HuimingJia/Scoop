package com.coop.android.activity;

import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import com.coop.android.activity.bean.User;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.util.NetworkChecker;


public class RegisterActivity extends Activity implements View.OnClickListener,EditText.OnFocusChangeListener{
    private EditText Register_edittext_email;
    private EditText Register_edittext_password;
    private EditText Register_edittext_password2;
    private Button Register_button_finish;
    private Button Register_button_back;
	private CustomToast CustomToast=new CustomToast();;
    
	private User mUser;
    private String email;
    private String password;
    private String password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initiate();
        addlistener();
    }

    public void initiate() {
          Register_edittext_email=(EditText)findViewById(R.id.register_edittext_email);
          Register_edittext_password=(EditText)findViewById(R.id.register_edittext_password);
          Register_edittext_password2=(EditText)findViewById(R.id.register_edittext_password2);
          Register_button_finish=(Button)findViewById(R.id.register_button_finish);
          Register_button_back=(Button)findViewById(R.id.register_button_back);
          
    }
    
    public void addlistener() {
        Register_edittext_email.setOnFocusChangeListener(this);
        Register_edittext_password.setOnFocusChangeListener(this);
        Register_edittext_password2.setOnFocusChangeListener(this);
        Register_button_finish.setOnClickListener(this);
        Register_button_back.setOnClickListener(this);
    }


    @SuppressLint("DefaultLocale") 
    
    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
			case R.id.register_button_finish: {
				 verifyInput();
				 break;
			}
			case R.id.register_button_back: {
				RegisterActivity.this.finish();
				break;
			}
    	}
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if ( hasFocus ) {

		} else {
            if(view.getId()==R.id.register_edittext_email) {}
            else if(view.getId()==R.id.register_edittext_password) {
            	 if((!Register_edittext_password.getText().toString().equals(Register_edittext_password2.getText().toString()))&& (!Register_edittext_password.getText().toString().equals("")) && (!Register_edittext_password2.getText().toString().equals(""))) {
            		 CustomToast.showCustomToast("Two passwords have to be consistent",this);
				 }
            } else if (view.getId()==R.id.register_edittext_password2){
				 if((!Register_edittext_password.getText().toString().equals(Register_edittext_password2.getText().toString()))&& (!Register_edittext_password.getText().toString().equals("")) && (!Register_edittext_password2.getText().toString().equals(""))) {
					 CustomToast.showCustomToast("Two passwords have to be consistent",this);
				 }
            }
        }
    }
    
    
    public void verifyInput()
    {
	   	 NetworkChecker networkerchecker=new NetworkChecker(RegisterActivity.this);
	   	 email=Register_edittext_email.getText().toString().toLowerCase();
	   	 password=Register_edittext_password.getText().toString();
	   	 password2=Register_edittext_password2.getText().toString();
	   	 if (networkerchecker.isNetworkAvailable()) {
	   		//check out if there is form that have not been fill out
	       	if((!email.equals(""))  &&(!password.equals("")) && (!password2.equals("")))  {
	       		//check out if the format of email is correct by regex
	       		if(Pattern.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", email)) {
	       				//check out if two password is consistent
		        			if(Register_edittext_password.getText().toString().equals(Register_edittext_password2.getText().toString())) {
		        		    	CustomToast.showCustomToast("Registering",this);
		        				register();	
		        			} else {
		        		    	CustomToast.showCustomToast("Two passwords have to be consistent",this);
		        			}   			
	       		} else {
	       			CustomToast.showCustomToast("Input account format wrong!",this);
	       		}      		
	       	} else {
	       		CustomToast.showCustomToast("form can not be empty",this);
	       	}
	   	} else {
	   		CustomToast.showCustomToast("No available Network",this);
	   	 }
    }
    
    public void register()
    {
    	mUser=new User(); 	
  		mUser.setUsername(email);
  		mUser.setEmail(email);
  		mUser.setPassword(password);
		mUser.signUp(new SaveListener<User>() {
			@Override
			public void done(User user, BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Success",RegisterActivity.this);
					Intent intent = new Intent();
					intent.setClass(RegisterActivity.this,LoginActivity.class);
					startActivity(intent);
					RegisterActivity.this.finish();

				} else {
					CustomToast.showCustomToast(e.toString(),RegisterActivity.this);
				}
			}
		});
//
//  		mUser.signUp(this, new SaveListener() {
//
//		    @Override
//		    public void onSuccess() {
//		        // TODO Auto-generated method stub
//		    	CustomToast.showCustomToast("注册成功!",RegisterActivity.this);
//
//					Intent intent = new Intent();
//					intent.setClass(RegisterActivity.this,LoginActivity.class);
//					startActivity(intent);
//                 	RegisterActivity.this.finish();
//    		    }
//
//    		    @Override
//    		    public void onFailure(int code, String arg0) {
//    		        // TODO Auto-generated method stub
//		    	CustomToast.showCustomToast("邮箱已被注册!",RegisterActivity.this);
//		        // 添加失败
//		    }
//		});
  
    }
    
}
