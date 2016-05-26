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
public class DownloadPublicGroupTask  extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    Context mContext;
    String username;
    String path;
    int pageid;
    int pagesize;

    public DownloadPublicGroupTask(Context mContext, String username, int pageid, int pagesize) {
        this.mContext = mContext;
        this.username = username;
        this.pageid = pageid;
        this.pagesize = pagesize;
        initPath();
    }


    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME, username)
                    .with(I.PAGE_ID,pageid+"")
                    .with(I.PAGE_SIZE,pagesize+"")
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path, Group[].class,
                responseDownloadGroupListTaskListener(), errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadGroupListTaskListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] response) {
                if (response != null) {
                    ArrayList<Group> contactGroupList = SuperWeChatApplication.getInstance().getGroupList();
                    ArrayList<Group> list = Utils.array2List(response);
                    //contactGroupList.clear();
                    for (Group g : list) {
                        if (!contactGroupList.contains(g)) {
                            contactGroupList.add(g);
                        }
                    }
                    mContext.sendStickyBroadcast(new Intent("update_public_group"));
                }

            }
        };

    }
}
