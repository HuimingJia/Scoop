package com.coop.android.activity;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.coop.android.activity.toast.CustomToast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorerActivity extends FragmentActivity implements OnItemClickListener,OnClickListener{
	private CustomToast CustomToast=new CustomToast();
	private ListView listView;
	private TextView textView;
	private Button mBackBtn;
	private Button mCloseBtn;
	private File root;

	//parent folder path
	private File ParentFolder;
	//List to record all the file under current folder
	//private File[] CurrentFiles;
	private Intent intent;
	private LinkedList<File> mFileList = new LinkedList<File>();
	private FileAdapter listAdapter; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intent = this.getIntent();
		setContentView(R.layout.activity_fileexplorer);
		initiateViews();
		initiateevents();
	}
	
	private void queryfiles() {
		//CurrentFiles=root;
		for(int i = 0;i<ParentFolder.listFiles().length;i++) {
			mFileList.add(ParentFolder.listFiles()[i]);
		}		
	}

	private void initiateViews() {
		mBackBtn =(Button)findViewById(R.id.file_back);
		mCloseBtn =(Button)findViewById(R.id.file_close);
		//get the ListView hold all the list
		listView=(ListView)findViewById(R.id.list);
		textView=(TextView)findViewById(R.id.path);
	
		//get root path for SD card
	     root=new File("/");
	     ParentFolder=root;	
		//If SD card exist
		if(root.exists()) {
			queryfiles();
		}	
		
		listAdapter = new FileAdapter(FileExplorerActivity.this, mFileList);	
		listView.setAdapter(listAdapter);
	}
	
	private void initiateevents() {
		listView.setOnItemClickListener(this);
		mBackBtn.setOnClickListener(this);	
		mCloseBtn.setOnClickListener(this);	
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub
		if(mFileList.get(position).isFile()) {
			Bundle bundle =new Bundle();
			try {
				bundle.putString("path",ParentFolder.getCanonicalPath() + "/" + mFileList.get(position).getName());
				bundle.putString("name", mFileList.get(position).getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			intent.putExtras(bundle);			
			setResult(RESULT_OK, intent);
			finish();		
			return;
		}
		
		File[] tmp=mFileList.get(position).listFiles();
		if(tmp==null||tmp.length==0) {
			CustomToast.showCustomToast("No File In Current Folder", FileExplorerActivity.this);
		} else {
			ParentFolder=mFileList.get(position);
			mFileList.clear();
			queryfiles();
			listAdapter.notifyDataSetChanged();
			textView.setText(ParentFolder.getPath());
		}		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mBackBtn) {
			if (!ParentFolder.getPath().equals("/")) {
				ParentFolder = ParentFolder.getParentFile();
			    //CurrentFiles = ParentFolder.listFiles();
				mFileList.clear();
				queryfiles();
				listAdapter.notifyDataSetChanged();
				textView.setText(ParentFolder.getPath());
			} else {
//					bundle.putString("path",currentParent.getCanonicalPath()+"/"+currentFiles[position].getName());
//					setResult(RESULT_OK, intent);
				finish();
			}
		} else if(v==mCloseBtn) {
			finish();
		}
	}
	
	private class FileAdapter extends ArrayAdapter<File> {
		public FileAdapter(Context context,List<File> objects)
		{
			super(context,0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if (convertView == null) {
				convertView = FileExplorerActivity.this.getLayoutInflater().inflate(R.layout.item_fileexplorer, parent, false);
			}
			ImageView imageview=(ImageView)convertView.findViewById(R.id.file_icon);
			TextView textview=(TextView)convertView.findViewById(R.id.file_name);
						
			if(getItem(position).isDirectory()) {
				imageview.setBackgroundResource(R.drawable.icon_directory);
			} else {
				imageview.setBackgroundResource(R.drawable.icon_file);
			}
			
			textview.setText(getItem(position).getName());			
			return convertView;
		}
	}
}
