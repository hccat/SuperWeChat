package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by sks on 2016/5/23.
 */
public class DownloadPublicGroupTask extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    String public_group;
    String path;
    Context mContext;

    public DownloadPublicGroupTask(String public_group, Context mContext) {
        this.public_group = public_group;
        this.mContext = mContext;
        initPath();
    }

    private void initPath() {
        try {
            path= new ApiParams()
                    .with(I.Group.NAME,public_group)
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,responseDownloadPublicGroupTaskListener(),errorListener()) {
        });
    }

    private Response.Listener<Group[]> responseDownloadPublicGroupTaskListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups!=null&&groups.length>0) {
                    ArrayList<Group> groupList =
                            SuperWeChatApplication.getInstance().getGroupList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    groupList.clear();
                    groupList.addAll(list);
                    mContext.sendStickyBroadcast(new Intent("update_public_group"));
                }
            }
        };
    }
}
