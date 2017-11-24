package com.coop.android.activity.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.coop.android.activity.R;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.CircleImageView;

public class ProfileFragment extends Fragment implements View.OnClickListener,DialogInterface.OnClickListener {
	
	private int button_statue = 0;		
	private TextView mEmail;
	private EditText mMajor;
	private LinearLayout mBoard;
	
	private EditText mPhone;
	private EditText mUsername;
	
	private BaseDialog mBaseDialog;
	private BaseDialog mConfirmDialog;
	private Button mMenuRightBtn;
	private CircleImageView mPhoto;
	private CircleImageView item_Photo;
	
	private BmobFile bmobFile;
	private User currentUser;

	
	private String mName;
	private String mNumber;
	private CustomToast CustomToast=new CustomToast();
	
	public static final int SELECT_PHOTO = 0;
	public static final int SAVE_PROFILE = 1;

	private static final int SELECT_FROM_CAMERA = 0;
	private static final int SELECT_FROM_FILE = 1;

	public static boolean save = false;
	
	private byte[] mContent;
	private String imagePath;
	private Bitmap mBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentUser=BmobUser.getCurrentUser(User.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		View profilefragment = inflater.inflate(R.layout.fragment_menuitem_profile,container, false);
		initiateViews(profilefragment);
		initiateEvents();
		return profilefragment;
	}
	
	private void initiateViews(View profilefragment)
	{
		((TextView) getActivity().findViewById(R.id.activity_title)).setText("个人信息");
		
		mEmail = (TextView) profilefragment.findViewById(R.id.tv_profile_email);
		mMajor   = (EditText) profilefragment.findViewById(R.id.tv_profile_major);
		
		mPhone = (EditText) profilefragment.findViewById(R.id.et_profile_phone);	
		mUsername = (EditText) profilefragment.findViewById(R.id.et_profile_username);
		mPhoto = (CircleImageView) profilefragment.findViewById(R.id.iv_profile_selectphoto);
		item_Photo= (CircleImageView) getActivity().findViewById(R.id.iv_icon);
		
		mBoard=(LinearLayout)profilefragment.findViewById(R.id.profilefragment_layout_board);

		mEmail.setEnabled(false);
		mMajor.setEnabled(false);
		mPhone.setEnabled(false);
		mUsername.setEnabled(false);
			
		mBaseDialog=new BaseDialog(getActivity());
		mBaseDialog.setTitle("选择头像");
		mBaseDialog.setMessage("从下列方式选择一项上传头像");
		
		
		mConfirmDialog=new BaseDialog(getActivity());
		mConfirmDialog.setTitle("确认修改");
		mConfirmDialog.setMessage("是否确认修改个人信息?");
		
		mMenuRightBtn = ((Button) getActivity().findViewById(R.id.title_bar_right_menu));
		mMenuRightBtn.setBackgroundResource(R.drawable.button_edit);

		initProfileInfo();
	}
	
	private void initiateEvents()
	{
		mPhoto.setOnClickListener(this);
		mMenuRightBtn.setOnClickListener(this);
		mBaseDialog.setButton1("Camera",this);
		mBaseDialog.setButton2("Photo ALbum",this);
		mBaseDialog.setButton3("Cancel",this);
		
		mConfirmDialog.setButton1("Sure",this);
		mConfirmDialog.setButton2("Cancel",this);
	}
	
	//initiate the information about current user and send them to view
	//when the value of certain property is null,set the content of view be "未设置"
	private void initProfileInfo()
	{	
		if (currentUser.getPhoto() != null) {
			mPhoto.setImageResource(R.drawable.blade);
//			currentUser.getPhoto().loadImage(getActivity(),mPhoto);
		} else {
			mPhoto.setImageResource(R.drawable.blade);
		}

		if (currentUser.getName() == null) {
			mUsername.setText("UNSET");
		} else {
			mUsername.setText(currentUser.getName());
		}
		
		if (currentUser.getTel() == null) {
			mPhone.setText("UNSET");
		} else {
			mPhone.setText(currentUser.getTel());
		}
		
		if(currentUser.getMajor()==null) {
			mMajor.setText("UNSET");
		} else {
			mMajor.setText(currentUser.getMajor());
		}
		mEmail.setText(currentUser.getEmail());
	}
	
	@SuppressLint({ "SdCardPath", "SimpleDateFormat" })
	
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 * this funtion will be evolve when the new activity is cancalled,we can get the result value from "data"
	 * and from statue from requestCode
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		ContentResolver resolver = getActivity().getContentResolver();
		File file = new File("/sdcard/myImage/");
		file.mkdirs();
		if (requestCode == SELECT_FROM_FILE)
		{
			if(data.getData()!=null)
			{
				try {
					Uri originalUri = data.getData();
					imagePath = originalUri.toString();
					mContent = readStream(resolver.openInputStream(Uri.parse(originalUri.toString())));
					// convert Imageview to Bitmap that can be used by programe
					mBitmap = getPicFromBytes(mContent, null);
					// check the statue of sd card
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						imagePath = "/sdcard/myImage/tempImage.jpg";
						FileOutputStream writeImage = new FileOutputStream(imagePath);
						mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, writeImage);
						//write the Bitmap into filesystem
						uploadUserImage(imagePath);
						return;
					} else {
						CustomToast.showCustomToast("SD Card Not Readable Right Now",getActivity());
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		} else if (requestCode == SELECT_FROM_CAMERA) {
			if(data.getExtras()!=null) {
				try
				{
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						// to get the data return by camera and save it in the bitmap
						//we use the time of creating picture as the filename of the photo
						mBitmap = (Bitmap) data.getExtras().get("data");
						Date fileName= new Date();;
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
						imagePath = "/sdcard/myImage/" + format.format(fileName) + ".jpg";

						FileOutputStream writeImage = new FileOutputStream(imagePath);
						mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, writeImage);
						uploadUserImage(imagePath);
					}
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	public static Bitmap getPicFromBytes(byte[] bytes,BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) 
		{
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}

	@Override
	public void onClick(View view) {
		if (view == mMenuRightBtn) 
		{
			if(button_statue==0)
			{
				//保存信息到服务器
				mMenuRightBtn.setBackgroundResource(R.drawable.button_confirm);		
				mPhone.setEnabled(true);
				mUsername.setEnabled(true);
				mMajor.setEnabled(true);
				
				mPhone.setTextColor(Color.rgb(123,123,123));
				mUsername.setTextColor(Color.rgb(123,123,123));
				mMajor.setTextColor(Color.rgb(123,123,123));
				mBoard.setBackgroundResource(R.drawable.background_board2);
				button_statue=1;
				
				mName=mUsername.getText().toString();
				mNumber=mPhone.getText().toString();
			}
			else
			{
				if((!mName.equals(mUsername.getText().toString()))|| (!mNumber.equals(mPhone.getText().toString())))
				mConfirmDialog.show();
				else
				{
					mMenuRightBtn.setBackgroundResource(R.drawable.button_edit);
					
					mPhone.setEnabled(false);
					mUsername.setEnabled(false);
					mMajor.setEnabled(false);
					mPhone.setTextColor(Color.rgb(41,55,70));
					mUsername.setTextColor(Color.rgb(41,55,70));
					mMajor.setTextColor(Color.rgb(41,55,70));
					mBoard.setBackgroundResource(R.drawable.background_board3);
					button_statue=0;
				}
			
			}
		}
		else if(view == mPhoto)
		{
			mBaseDialog.show();
		}
	}
	

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(dialog==mBaseDialog)
		{
			 switch(which)
			 {
			 case 0:	 
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				startActivityForResult(intent, SELECT_FROM_CAMERA);
				 dialog.cancel();
				 break;
			 case 1:
				 Intent intent2 = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				 startActivityForResult(intent2, SELECT_FROM_FILE);
				 dialog.cancel();
				 break;
			 case 2:
				 dialog.cancel();
				 break;
			 
			 }	
		}
		else if(dialog==mConfirmDialog)
		{
			 switch(which)
			 {
			 case 0:	 
				 SaveModification();
				 dialog.cancel();
				 break;
			 case 1:
				 dialog.cancel();
				 break;
			 
			 }
		}
	}
	
	private void SaveModification() {
		/*
		 * use regex match to verify the correctness of format of input telephone number
		 * if the format of in put email address is not meet the standard of normal telephone numer,only save the name
		 * otherwise save both
		 */
		//验证手机是否符合格式要求
		if (Pattern.matches("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$",mPhone.getText().toString()) || mPhone.getText().toString().equals("Unset")) {
			currentUser.setName(mUsername.getText().toString());
			currentUser.setTel(mPhone.getText().toString());
		} else {
			currentUser.setName(mUsername.getText().toString());
			CustomToast.showCustomToast("Can not be save due to illegal format",getActivity());
		}

		currentUser.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					CustomToast.showCustomToast("Update Finished",getActivity());
				} else {
					CustomToast.showCustomToast("Update Failed",getActivity());
				}
			}
		});
		
		/*
		 * finish the modification and reset the statue of the button
		 */
		
		mMenuRightBtn.setBackgroundResource(R.drawable.button_edit);
		
		mPhone.setEnabled(false);
		mUsername.setEnabled(false);
		mMajor.setEnabled(false);
		
		mPhone.setTextColor(Color.rgb(41,55,70));
		mUsername.setTextColor(Color.rgb(41,55,70));
		mMajor.setTextColor(Color.rgb(41,55,70));
		
		mBoard.setBackgroundResource(R.drawable.background_board3);
		button_statue = 0;
		
	}

	private void uploadUserImage(String imagePath) 
	{
		bmobFile = new BmobFile(new File(imagePath));
		bmobFile.uploadblock(new UploadFileListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					currentUser.setPhoto(bmobFile);
					currentUser.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {

								mPhoto.setImageBitmap(mBitmap);
								item_Photo.setImageBitmap(mBitmap);
								CustomToast.showCustomToast("Update Finished",getActivity());
							} else {
								CustomToast.showCustomToast("Update Failed : " + e.toString(), getActivity());
							}
						}
					});
				}
			}
		});
	}
}
