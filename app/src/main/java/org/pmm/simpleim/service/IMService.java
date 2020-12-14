package org.pmm.simpleim.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.pmm.simpleim.BuildConfig;
import org.pmm.simpleim.R;
import org.pmm.simpleim.db.IMDbDataBean;
import org.pmm.simpleim.ui.app.MyApp;
import org.pmm.simpleim.ui.main.MainActivity;
import org.pmm.simpleim.utils.NotificationUtils;
import org.pmm.simpleim.utils.RxTimerUtil;
import org.pmm.simpleim.utils.SPUtils;

import java.util.List;
import java.util.Random;


public class IMService extends Service {
    private final String TAG = "IMService";
    //通知的渠道
    public static final String NOTIFI_CHAT = "chat";//聊天通知
    //广播监听 和CUP保持
    private NetScreenStateReceiver NetScreenReceiver;//网络变化和息屏的广播
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        showTAG("onBind:");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showTAG("onCreate:");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotifi();
        }
        startTimer();
    }

    /**
     * 多少N访问
     */
    private boolean startISok = false;//定时器是否启动
    private boolean ischeck = false;//锁

    private void startTimer() {
        startISok = true;
        //10s
        RxTimerUtil.interval(7000, new RxTimerUtil.IRxNext() {
            @SuppressLint("NewApi")
            @Override
            public void doNext(long number) {
                getSginText();
            }
        });
    }

    /**
     * 停止监听
     */
    private void stopTimer() {
        RxTimerUtil.cancel();
        ischeck = false;
        startISok = false;
    }

    /**
     * 获取一条语录
     */
    private String userID = SPUtils.getInstance().getString(SPUtils.USER_UID);

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getSginText() {
        String url = "http://p2pmm.cn/api/index/get_data";
        OkGo.<String>post(url)
                .tag(this)
                .params("userid", userID)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String str = response.body();
                            LogUtils.d(str);
                            if (!TextUtils.isEmpty(str)) {
                                JSONObject JSON = new JSONObject(str);
                                int code = JSON.optInt("code");
                                if (code == 1) {
                                    JSONArray jsonArray = JSON.optJSONArray("data");
                                    Gson gson = new Gson();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        IMDbDataBean dataBean = gson.fromJson(jsonArray.optString(i), IMDbDataBean.class);
                                        if (dataBean != null) {
                                            savaAndUpdateData(dataBean);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {

                    }
                });
    }

    /**
     * sava to data
     *
     * @param dataBean
     */
    private void savaAndUpdateData(IMDbDataBean dataBean) {
        List<IMDbDataBean> dataDBs = LitePal.where("msgid = ?", String.valueOf(dataBean.getMsgid())).find(IMDbDataBean.class);
        LogUtils.d(dataDBs);
        if (dataDBs.size() == 0) {
            dataBean.save();
        } else if (dataDBs.size() == 1) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("cardname", dataBean.getCardname());
            contentValues.put("message", dataBean.getMessage());
            contentValues.put("addtime", dataBean.getAddtime());
            LitePal.update(IMDbDataBean.class, contentValues, dataDBs.get(0).getId());
        }
        String messagesA = SPUtils.getInstance().getString(SPUtils.CHAT_MESSAGE_A, "false");
        EventBus.getDefault().post(dataBean);
        if (messagesA.equals("false")) {
            showNotification(dataBean);
        }
    }

    /**
     * 显示一条聊天信息的通知
     *
     * @param bean
     */
    private void showNotification(IMDbDataBean bean) {
        //跳转到聊天记录列表界面
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtils.showNotification(MyApp.getContext(), contentIntent,
                bean.getCardname(), bean.getMessage(), NOTIFI_CHAT, new Random().nextInt(8000 + 1));

    }

    /**
     * 创建渠道通知
     */
    private void initNotifi() {
        String channelId = NOTIFI_CHAT;
        String channelName = "易简讯APP-聊天消息";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        createNotificationChannel(channelId, channelName, importance);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        //显示消息角标
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 当前台服务被销毁的时候
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //解除广播
        if (NetScreenReceiver != null) {
            unregisterReceiver(NetScreenReceiver);
        }
        //释放
        if (wakeLock != null) {
            wakeLock.release();//关闭
        }
        OkGo.getInstance().cancelTag(this);
        stopTimer();
        //如果从聊天界面和列表界面关闭APP 会出现通知栏不显示 这里恢复
        SPUtils.getInstance().putString(SPUtils.CHAT_MESSAGE_A, "false");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //发送广播 进程KeepStartService
        showNotifiForeground();
        initNetReceiver();
        return Service.START_STICKY;
    }

    /**
     * 注册广播监听
     */
    private void initNetReceiver() {
        //注册网络监听广播 START
        if (NetScreenReceiver == null) {
            NetScreenReceiver = new NetScreenStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//网络变化
            filter.addAction(Intent.ACTION_SCREEN_OFF); // 屏幕灭屏广播
            filter.addAction(Intent.ACTION_SCREEN_ON); // 屏幕亮屏广播
            registerReceiver(NetScreenReceiver, filter);//注册广播
        }
    }

    /**
     * 启动通知到前台服务
     */
    private void showNotifiForeground() {
        try {
            String CHANNEL_ONE_ID = "org.pmm.simpleim";
            String CHANNEL_ONE_NAME = getString(R.string.app_name);
            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(notificationChannel);
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);

                Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                        .setChannelId(CHANNEL_ONE_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("为保持消息畅通,请保持此服务的开启!")
                        .setSmallIcon(R.drawable.app_logo)
                        .build();
                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                startForeground(7000, notification);
            } else {
                // 在API11之后构建Notification的方式
                Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
                builder.setSmallIcon(R.drawable.app_logo) // 设置状态栏内的小图标
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("为保持消息畅通,请保持此服务的开启!")
                        .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                        .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
                Notification notification = builder.build(); // 获取构建好的Notification
                startForeground(7000, notification);// 开始前台服务
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showShort("前台服务启动异常,请开启通知栏权限!");
        }
    }


    /**
     * 广播
     */
    public class NetScreenStateReceiver extends BroadcastReceiver {
        @SuppressLint("InvalidWakeLockTag")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:// 屏幕灭屏广播
                    showTAG("灭屏");
                    pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");
                    wakeLock.acquire();
                    //灭屏的话 就默认不接收
                    break;
                case Intent.ACTION_SCREEN_ON: // 屏幕亮屏广播
                    showTAG("亮屏");

                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION://网络变化的广播
                    showTAG("网络变化");
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                        if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {

                        } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {

                        } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {

                        }
                    } else {
                        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        Network[] networks = connMgr.getAllNetworks();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < networks.length; i++) {
                            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                            if (networkInfo != null) {
                                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                                if (networkInfo.isConnected()) {

                                    break;
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void showTAG(String msg) {
        if (BuildConfig.DEBUG) {
            int max_str_length = 2001 - TAG.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.e(TAG, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.e(TAG, msg);
        }
    }
}
