package org.pmm.simpleim.utils;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.os.Build;


import androidx.core.app.NotificationCompat;

import org.pmm.simpleim.R;
import org.pmm.simpleim.ui.app.MyApp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * 通知工具类
 * Created by caoyu on 2018/7/17/017.
 */

public class NotificationUtils {

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    /**
     * 显示通知
     *
     * @param context
     * @param contentTitle 通知标题
     * @param contentText  通知内容
     * @param channelId    通知渠道标签
     * @param id           通知的id
     */
    public static void showNotification(Context context, PendingIntent intent, String contentTitle, String contentText, String channelId, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(MyApp.getContext(), channelId)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                    .setAutoCancel(true)
                    .setContentIntent(intent)
                    .build();
            manager.notify(id, notification);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            //设置通知栏大图标，上图中右边的大图
            builder.setLargeIcon(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.app_logo))
                    // 设置状态栏和通知栏小图标
                    .setSmallIcon( R.drawable.app_logo)
                    // 设置通知栏应用名称
                    .setTicker(context.getString(R.string.app_name))
                    // 设置通知栏显示时间
                    .setWhen(System.currentTimeMillis())
                    // 设置通知栏标题
                    .setContentTitle(contentTitle)
                    // 设置通知栏内容
                    .setContentText(contentText)
                    // 设置通知栏点击后是否清除，设置为true，当点击此通知栏后，它会自动消失
                    .setAutoCancel(true)
                    // 将Ongoing设为true 那么左滑右滑将不能删除通知栏
                    //.setOngoing(true);
                    // 设置通知栏点击意图
                    .setContentIntent(intent)
                    .setChannelId("")
                    // 铃声、闪光、震动均系统默认
                    .setDefaults(Notification.DEFAULT_ALL)
                    // 设置为public后，通知栏将在锁屏界面显示
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    // 从Android4.1开始，可以通过以下方法，设置通知栏的优先级，优先级越高的通知排的越靠前，
                    // 优先级低的，不会在手机最顶部的状态栏显示图标
                    // 设置优先级为PRIORITY_MAX，将会在手机顶部显示通知栏
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            NotificationManager noti = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);
            noti.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @SuppressLint("NewApi")
    public static boolean isNotificationEnabled(Context context) {
        boolean isNotification = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isNotification = isEnableV26(context);
        } else {
            isNotification = haveNotification(context);
        }
        return isNotification;
    }

    @SuppressLint("NewApi")
    public static boolean haveNotification(Context context) {

        AppOpsManager mAppOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;

        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod =
                    appOpsClass.getMethod(CHECK_OP_NO_THROW,
                            Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);

            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) ==
                    AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 8.0以上获取t通知栏状态
     *
     * @param context
     * @return
     */
    public static boolean isEnableV26(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Method sServiceField = notificationManager.getClass().getDeclaredMethod("getService");
            sServiceField.setAccessible(true);
            Object sService = sServiceField.invoke(notificationManager);

            Method method = sService.getClass().getDeclaredMethod("areNotificationsEnabledForPackage"
                    , String.class, Integer.TYPE);
            method.setAccessible(true);
            return (boolean) method.invoke(sService, pkg, uid);
        } catch (Exception e) {
            return true;
        }
    }

}
