/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.ImageUtils;
import cn.ucai.superwechat.utils.Utils;

public class NewGroupActivity extends BaseActivity {
	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer;
	private ImageView mGroupAvatar;

	OnSetAvatarListener mOnSetAvatarListener;
	NewGroupActivity mContext;

	private static final int CREAT_NEW_GROUP =100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);
		mContext = this;
		initView();
		setListener();
	}

	private void initView() {
		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);

		mGroupAvatar = (ImageView) findViewById(R.id.iv_avatar);
	}

	private void setListener() {
		setOnCheckchangedListener();
		setSaveGroupClickListener();
		setGroupIconClickListener();
	}

	private void setGroupIconClickListener() {
		findViewById(R.id.layout_group_avatar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOnSetAvatarListener=new OnSetAvatarListener(mContext,R.id.layout_new_group,getAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
			}
		});
	}

	String avatarName;
	private String getAvatarName() {
		avatarName = System.currentTimeMillis() + "";
		return avatarName;
	}

	private void setSaveGroupClickListener() {
		findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(mContext, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
					startActivityForResult(new Intent(mContext, GroupPickContactsActivity.class).putExtra("groupName", name), CREAT_NEW_GROUP);
				}
			}
		});
	}

	private void setOnCheckchangedListener() {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					openInviteContainer.setVisibility(View.INVISIBLE);
				}else{
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}


	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == CREAT_NEW_GROUP) {
			setProgressDialog();
			//新建群组
			createNewGroup(data);
		} else {
			mOnSetAvatarListener.setAvatar(requestCode,data,mGroupAvatar);
		}
	}

	private void createNewGroup( final Intent data) {
		final String st2 = getResources().getString(R.string.Failed_to_create_groups);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 调用sdk创建群组方法
				String groupName = groupNameEditText.getText().toString().trim();
				String desc = introductionEditText.getText().toString();
				Contact[] contacts= (Contact[]) data.getSerializableExtra("newmembers");
				String[] members = null;
//				String[] memberIds = null;
				if (contacts!=null) {
					members = new String[contacts.length];
					for (int i =0 ;i<contacts.length;i++) {
						members[i] = contacts[i].getMContactCname() + ",";
//						memberIds[i] = contacts[i].getMContactId() + ",";
					}
				}
				EMGroup emGroup;
				try {
					if(checkBox.isChecked()){
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						emGroup=EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
					}else{
						//创建不公开群
						emGroup=EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
					}
					String hxid = emGroup.getGroupId();
					createNewGroupAppServer(hxid, groupName, desc, contacts);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

			}
		});
	}

	private void createNewGroupAppServer(String hxid, String groupName, String desc, final Contact[] contacts) {
		User user = SuperWeChatApplication.getInstance().getUser();
		boolean isPublic = checkBox.isChecked();
		boolean isAllow = memberCheckbox.isChecked();
		//注册环信的群组
		//首先注册远端服务器账号，并上传头像----okhttp
		//如果环信注册失败，调用取消注册的方法，删除远端账号和图片

		File file = new File(ImageUtils.getAvatarPath(mContext, I.AVATAR_TYPE_USER_PATH),
				avatarName + I.AVATAR_SUFFIX_JPG);
		OkHttpUtils<Group> utils = new OkHttpUtils<Group>();
		utils.url(SuperWeChatApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST, I.REQUEST_CREATE_GROUP)
				.addParam(I.Group.HX_ID, hxid + "")
				.addParam(I.Group.NAME, groupName)
				.addParam(I.Group.DESCRIPTION, desc)
				.addParam(I.Group.OWNER, SuperWeChatApplication.getInstance().getUserName())
				.addParam(I.Group.IS_PUBLIC, isPublic + "")
				.addParam(I.Group.ALLOW_INVITES, isAllow + "")
				.addParam(I.User.USER_ID, user.getMUserId() + "")
				.targetClass(Group.class)
				.addFile(file)

				.execute(new OkHttpUtils.OnCompleteListener<Group>() {
					@Override
					public void onSuccess(Group group) {
						if (group.isResult()) {
							if (contacts != null) {
								addGroupMembers(group, contacts);
							} else {
								SuperWeChatApplication.getInstance().getGroupList().add(group);
								progressDialog.dismiss();
								setResult(RESULT_OK);
								mContext.sendBroadcast(new Intent("update_group_list"));
								finish();
							}
						} else {
							progressDialog.dismiss();
							Utils.showToast(mContext,Utils.getResourceString(mContext,group.getMsg()),Toast.LENGTH_SHORT);
						}
					}

					@Override
					public void onError(String error) {

					}
				});
	}

	private void addGroupMembers(Group group,Contact[] contacts) {
		String userIds = "";
		String userNames= "";
		for (int i=0;i<contacts.length;i++) {
			userIds += contacts[i].getMContactId()+"";
			userNames += contacts[i].getMContactCname() + ",";
		}
		try {
			String path =new ApiParams()
					.with(I.Member.USER_ID,userIds)
					.with(I.Member.USER_NAME,userNames)
					.with(I.Member.GROUP_HX_ID,group.getMGroupHxid())
					.getRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS);
			executeRequest(new GsonRequest<Message>(path,Message.class,responseAddGroupMembersListener(group),errorListener()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Response.Listener<Message> responseAddGroupMembersListener(final Group group) {
		return new Response.Listener<Message>() {
			@Override
			public void onResponse(Message message) {
				if (message.isResult()) {
					progressDialog.dismiss();
					SuperWeChatApplication.getInstance().getGroupList().add(group);
					progressDialog.dismiss();
					setResult(RESULT_OK);
					mContext.sendBroadcast(new Intent("update_group_list"));
					finish();
				} else {
					progressDialog.dismiss();
					Utils.showToast(mContext,R.string.Failed_to_create_groups,Toast.LENGTH_SHORT);
				}
				finish();
			}
		};
	}

	private void setProgressDialog() {
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(st1);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	public void back(View view) {
		finish();
	}
}
