package org.pmm.simpleim.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;
import org.pmm.simpleim.R;
import org.pmm.simpleim.db.IMDbDataBean;
import org.pmm.simpleim.service.IMService;
import org.pmm.simpleim.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IMDetailsAdapter.ItemOnLongClickListener {
    private RecyclerView recyclerview;
    private ProgressBar mProgressBar;
    private TextView mTvData;

    private IMDetailsAdapter chatMessageDetailsAdapter;//内容适配器
    private List<IMDbDataBean> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarUtils.setStatusBarLightMode(this, true);
        EventBus.getDefault().register(this);//注册事件总线

        recyclerview = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mTvData = findViewById(R.id.tv_no_data);

        mProgressBar.setVisibility(View.VISIBLE);
        mTvData.setVisibility(View.INVISIBLE);
        recyclerview.setVisibility(View.INVISIBLE);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initList();
                StartWebSocketService();
            }
        }, 1700);//延迟N秒执行登陆操作
    }

    @Override
    protected void onStart() {
        super.onStart();
        SPUtils.getInstance().putString(SPUtils.CHAT_MESSAGE_A, "true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        SPUtils.getInstance().putString(SPUtils.CHAT_MESSAGE_A, "false");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//销毁
    }

    /**
     * 启动websocket聊天服务
     */
    private Intent ServiceIntent = null;

    private void StartWebSocketService() {
        if (null == ServiceIntent) {
            ServiceIntent = new Intent(this, IMService.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(ServiceIntent);
        } else {
            startService(ServiceIntent);
        }
    }

    private void initList() {
        //1.设置内容列表
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        recyclerview.setLayoutManager(mLinearLayoutManager);
        chatMessageDetailsAdapter = new IMDetailsAdapter(this, mList);
        chatMessageDetailsAdapter.setItemOnLongClickListener(this);
        recyclerview.setAdapter(chatMessageDetailsAdapter);
        initDBDataToView();
    }

    /**
     * 加载本地数据库的聊天记录
     */
    private void initDBDataToView() {
        try {
            mList.addAll(LitePal.findAll(IMDbDataBean.class));
            chatMessageDetailsAdapter.notifyDataSetChanged();
            recyclerview.scrollToPosition(chatMessageDetailsAdapter.getItemCount() - 1);
            if (mList.size() > 0) {
                mProgressBar.setVisibility(View.GONE);
                mTvData.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mTvData.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将消息添加进入内容列表   已经在Wevsocket里面保存了
     *
     * @param dataBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void acceptMsg(IMDbDataBean dataBean) {
        try {
            boolean tf = false;
            for (IMDbDataBean bean : mList) {
                if (bean.getMsgid() == dataBean.getMsgid()) {
                    tf = true;
                    break;
                }
            }
            if (!tf) {
                mList.add(dataBean);
                chatMessageDetailsAdapter.notifyDataSetChanged();
                recyclerview.scrollToPosition(chatMessageDetailsAdapter.getItemCount() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemOnLongClick(int position) {

    }
}