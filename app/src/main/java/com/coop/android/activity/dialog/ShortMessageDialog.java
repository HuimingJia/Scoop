package com.coop.android.activity.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import com.coop.android.activity.R;

public class ShortMessageDialog extends BaseDialog {

	private EditText mEtEnter;

	public ShortMessageDialog(Context context) {
		super(context);
		setDialogContentView(R.layout.include_dialogshortedit);
		mEtEnter = (EditText) findViewById(R.id.dialog_edittext_enter);
		mEtEnter.setMaxEms(80);
		mEtEnter.setMaxLines(1);
	}
	
	public void setmax(int max)
	{
		mEtEnter.setMaxEms(max);
		mEtEnter.setMaxLines(max);
	}

	@Override
	public void setTitle(CharSequence text) {
		super.setTitle(text);
	}

	public void setButton(CharSequence text,
			DialogInterface.OnClickListener listener) {
		super.setButton1(text, listener);
	}

	public void setButton(CharSequence text1,
			DialogInterface.OnClickListener listener1, CharSequence text2,
			DialogInterface.OnClickListener listener2) {
		super.setButton1(text1, listener1);
		super.setButton2(text2, listener2);
	}

	public String getText() {
		if (isNull(mEtEnter)) {
			return null;
		}
		return mEtEnter.getText().toString().trim();
	}
	
	public void setTextNull() {
		mEtEnter.setText("");
	}
	
	public void setText(CharSequence text) {
		mEtEnter.setText(text);
	}
	
	public void setHint(CharSequence text) {
		mEtEnter.setHint(text);
	}

	private boolean isNull(EditText editText) {
		String text = editText.getText().toString().trim();
		if (text != null && text.length() > 0) {
			return false;
		}
		return true;
	}

	public void requestFocus() {
		mEtEnter.requestFocus();
	}
}
