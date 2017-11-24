package com.coop.android.activity;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.volley.toolbox.ImageLoader;

import com.coop.android.activity.bean.Comment;
import com.coop.android.activity.bean.Discussion;
import com.coop.android.activity.bean.Project;
import com.coop.android.activity.bean.Student;
import com.coop.android.activity.bean.Task;
import com.coop.android.activity.bean.User;
import com.coop.android.activity.dialog.BaseDialog;
import com.coop.android.activity.dialog.LongMessageDialog;
import com.coop.android.activity.dialog.ShortMessageDialog;
import com.coop.android.activity.dialog.LoadingDialog;
import com.coop.android.activity.fragment.AnsDiscussionFragment;
import com.coop.android.activity.fragment.UnAnsDiscussionFragment;
import com.coop.android.activity.toast.CustomToast;
import com.coop.android.activity.view.CircleImageView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressWarnings("unused")
public class DiscussionDetailActivity extends Activity implements
		View.OnClickListener, OnRefreshListener<ListView>,
		DialogInterface.OnClickListener, AdapterView.OnItemLongClickListener, OnLongClickListener {
	private CustomToast CustomToast = new CustomToast();

	private Discussion mDiscussion;
	private CircleImageView mProfile;
	private TextView mName;
	private TextView mContent;
	private TextView mTheme;
	

	private int operation_position;
	private PullToRefreshListView mPullRefreshListView;
	private LinkedList<Comment> mCommentsList = new LinkedList<Comment>();
	private CommentsAdapter listAdapter;

	private Button mBackBtn;
	private Button mAddComBtn;

	private Comment mComment;
	private Project mProject;
	private Task mTask;
	private User mUser;

	private BaseDialog mDelDialog;
	private LongMessageDialog mAddDialog;
	private LongMessageDialog mEditContentDialog;
	private LoadingDialog mWaitingAddTaskDialog;

	/*
	 * the needed operation when create the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discussiondetail);
		mProject = (Project) getIntent().getSerializableExtra("Project");
		mTask = (Task) getIntent().getSerializableExtra("Task");
		mDiscussion = (Discussion) getIntent().getSerializableExtra(
				"Discussion");
		
		initiateViews();
		initiateEvents();
		queryComments();
	}

	private void queryComments() {
		mCommentsList.clear();
		BmobQuery<Comment> query = new BmobQuery<Comment>();
		query.addWhereRelatedTo("mComments", new BmobPointer(mDiscussion));
		query.order("createdAt");
		query.findObjects(new FindListener<Comment>() {
			@Override
			public void done(List<Comment> list, BmobException e) {
				if (e == null) {
					for (Comment t : list)
					{
						mCommentsList.add(t);
						listAdapter.notifyDataSetChanged();}
				} else {
					Log.i("Comment","Failed：" + e.getMessage() + "," + e.getErrorCode());
				}
			}
		});

		query.findObjects(new FindListener<Comment>() {
			@Override
			public void done(List<Comment> list, BmobException e) {
				if (e == null) {
					for (Comment t : list) {
						mCommentsList.add(t);
						listAdapter.notifyDataSetChanged();
					}
				}
			}
		});
	}

	private void initiateViews() {
		mWaitingAddTaskDialog = new LoadingDialog(DiscussionDetailActivity.this, "Comment Send，Please Waiting...");
		mPullRefreshListView = (PullToRefreshListView) DiscussionDetailActivity.this.findViewById(R.id.contents_listview);
		mPullRefreshListView.setMode(Mode.PULL_FROM_START);

		mDelDialog = new BaseDialog(DiscussionDetailActivity.this);
		mDelDialog.setTitle("Comment Delete");
		mDelDialog.setMessage("Confirm Delete？");

		mAddDialog = new LongMessageDialog(DiscussionDetailActivity.this);
		mAddDialog.setTitle("Add Comment");
		mAddDialog.setHint("Type Comment");

		mEditContentDialog = new LongMessageDialog(DiscussionDetailActivity.this);
		mEditContentDialog.setTitle("Edit Discussion Content");
		mEditContentDialog.setHint("Type Content");

		mPullRefreshListView = (PullToRefreshListView) DiscussionDetailActivity.this.findViewById(R.id.contents_listview);
		listAdapter = new CommentsAdapter(DiscussionDetailActivity.this, mCommentsList);
		mPullRefreshListView.setAdapter(listAdapter);

		mName = (TextView) findViewById(R.id.comment_textview_owername);
		mTheme = (TextView) findViewById(R.id.comment_textview_theme);
		mContent = (TextView) findViewById(R.id.comment_textview_description);

		if(mDiscussion.getName() == null || mDiscussion.getName().equals(""))
			mName.setText("UNSET");
		else
			mName.setText(mDiscussion.getUser().getName());
		
		mTheme.setText(mDiscussion.getTitle());
		mContent.setText(mDiscussion.getDescription());
		mBackBtn = (Button) findViewById(R.id.discussiondetail_button_back);
		mBackBtn.setBackgroundResource(R.drawable.button_back);
		mAddComBtn = (Button) findViewById(R.id.discussiondetail_image_editdiscussion);
		mAddComBtn.setBackgroundResource(R.drawable.button_add);
	}

	private void initiateEvents() {
		mPullRefreshListView.setOnRefreshListener(this);
		mBackBtn.setOnClickListener(this);
		mAddComBtn.setOnClickListener(this);

		mPullRefreshListView.setOnItemLongClickListener(this);
		mAddDialog.setButton1("Sure", this);
		mAddDialog.setButton2("Cancel", this);
		mDelDialog.setButton1("Sure", this);
		mDelDialog.setButton2("Cancel", this);
		mEditContentDialog.setButton1("Sure", this);
		mEditContentDialog.setButton2("Cancel", this);
		mContent.setOnLongClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.discussiondetail_image_editdiscussion:
			mAddDialog.show();
			break;
		case R.id.discussiondetail_button_back:
			Intent intent_back = new Intent(DiscussionDetailActivity.this, DiscussionsActivity.class);
			intent_back.putExtra("Project", mProject);
			intent_back.putExtra("Task", mTask);
			startActivity(intent_back);
			finish();
			break;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == 1) {
			dialog.cancel();
		} else {
			if (dialog == mDelDialog) {

				// 删除任务
				mDelDialog.dismiss();
				BmobRelation DiscussionComment = new BmobRelation();
				DiscussionComment.remove(mComment);
				mDiscussion.setComments(DiscussionComment);
				mDiscussion.setState(mDiscussion.getState()-1);
				mDiscussion.update(new UpdateListener() {
					@Override
					public void done(BmobException e) {
						if (e == null) {
							mComment.delete(new UpdateListener() {
								@Override
								public void done(BmobException e) {
									if (e == null) {
										CustomToast.showCustomToast("Comment Delete Finished", DiscussionDetailActivity.this);
										mCommentsList.remove(operation_position);
										listAdapter.notifyDataSetChanged();
									} else{
										CustomToast.showCustomToast("Comment Delete Failed", DiscussionDetailActivity.this);
									}
								}
							});
						} else {
							CustomToast.showCustomToast("Comment Delivery Failed", DiscussionDetailActivity.this);
						}
					}
				});
			} 
			else if (dialog == mAddDialog) {
				String content = mAddDialog.getText();
				if (content == null) {
					mAddDialog.requestFocus();
					CustomToast.showCustomToast("Content Can Not Be Empty", DiscussionDetailActivity.this);
				} else {
					mAddDialog.dismiss();
					mAddDialog.setTextNull();
					new addCommentThread().execute(content);
				}
			}
			else if(dialog==mEditContentDialog) {
				dialog.cancel();
				SaveContent();
				mContent.setText(mDiscussion.getDescription().toString());
			}
		}
	}
	
	private void SaveContent() {
		if(mEditContentDialog.getText().toString()!=null && mEditContentDialog.getText().toString()!="") {
			mDiscussion.setDescription(mEditContentDialog.getText().toString());
			mDiscussion.update(new UpdateListener() {
				@Override
				public void done(BmobException e) {
					if (e == null) {
						CustomToast.showCustomToast("Save Finished", DiscussionDetailActivity.this);
					}
				}
			});
		}
	}

	private class addCommentThread extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitingAddTaskDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				addComment(params[0]);
				queryComments();
				Thread.sleep(1100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitingAddTaskDialog.dismiss();
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		queryComments();

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		// mDelDialog.show();
		deleteComment(position);
		return true;
	}

	private class CommentsAdapter extends ArrayAdapter<Comment> {
		public CommentsAdapter(Context context, List<Comment> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (getItem(position).getCreator().getObjectId().equals(mDiscussion.getUser().getObjectId())) {
					convertView = DiscussionDetailActivity.this.getLayoutInflater().inflate(R.layout.item_comment_1, parent, false);
			} else {
				convertView = DiscussionDetailActivity.this.getLayoutInflater().inflate(R.layout.item_comment_2, parent, false);
			}

			final CircleImageView profile = (CircleImageView) convertView.findViewById(R.id.comment_imageview_profile);
			final TextView name = (TextView) convertView.findViewById(R.id.comment_textview_name);
			final TextView content = (TextView) convertView.findViewById(R.id.comment_textview_content);
			
			if(getItem(position).getCreatorPhoto()!=null) {
				profile.setBackgroundResource(R.drawable.background_profile);
//				 String url = getItem(position).getCreatorPhoto().loadi
//				ImageLoader imageLoader = ImageLoader.getInstance()
//				profile.setImageURI((URI)uri);
			} else {
				profile.setBackgroundResource(R.drawable.background_profile);
			}

			if(getItem(position).getCreatorName()!=null) {
				name.setText(getItem(position).getCreatorName());
			} else {
				name.setText("UNSET");
			}
			content.setText(getItem(position).getContent());
			return convertView;
		}
	}

	private void deleteComment(int position) {
		operation_position = position - 1;
		mComment = mCommentsList.get(operation_position);
		if (BmobUser.getCurrentUser(User.class).getObjectId().equals(mProject.getLeader().getObjectId())
				|| BmobUser.getCurrentUser(User.class).getObjectId().equals(mComment.getCreator().getObjectId())) {
			mDelDialog.show();
		} else {
			CustomToast.showCustomToast("You Do Not Have Authority To Delete", DiscussionDetailActivity.this);
		}
	}

	private void addComment(String content) {
		mComment = new Comment();
		User user = (User) BmobUser.getCurrentUser(User.class);
		mComment.setCreator(user);
		mComment.setCreatorName(user.getName());
		mComment.setCreatorPhoto(user.getPhoto());
		mComment.setDisscusion(mDiscussion);
		mComment.setContent(content);
		mComment.save(new SaveListener<String>() {
			@Override
			public void done(String s, BmobException e) {
				if (e == null) {
					BmobRelation mComments = new BmobRelation();
					mComments.add(mComment);
					mDiscussion.setComments(mComments);
					mDiscussion.setState(mDiscussion.getState() + 1);
					mDiscussion.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if (e == null) {
								mCommentsList.addLast(mComment);
								listAdapter.notifyDataSetChanged();
								CustomToast.showCustomToast("Comment Delivery Finished", DiscussionDetailActivity.this);
							} else {
								CustomToast.showCustomToast("Comment Delivery Failed", DiscussionDetailActivity.this);
							}
						}
					});
				} else {
					CustomToast.showCustomToast("Comment Delivery Failed", DiscussionDetailActivity.this);
				}
			}
		});
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(v==mContent) {
			if(BmobUser.getCurrentUser(User.class).getObjectId().equals(mDiscussion.getUser().getObjectId())) {
				mEditContentDialog.show();
			}
		}
		return true;
	}

}
