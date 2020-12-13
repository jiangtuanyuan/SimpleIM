package org.pmm.simpleim.ui.welcome;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.pmm.simpleim.R;
import org.pmm.simpleim.ui.login.LoginActivity;
import org.pmm.simpleim.ui.main.MainActivity;
import org.pmm.simpleim.utils.SPUtils;

import io.reactivex.functions.Consumer;

/**
 * 引导页
 */
public class WelcomeActivity extends AppCompatActivity {
    private int TIME = 1700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        HiddenWindosStatus();
        checkPermissions(this);
    }

    private void intHander() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(SPUtils.getInstance().getString(SPUtils.USER_NAME)) && !TextUtils.isEmpty(SPUtils.getInstance().getString(SPUtils.USER_PWD))) {
                    startMain();
                } else {
                    startLogin();
                }
            }
        }, TIME);//延迟N秒执行登陆操作
    }

    /**
     * 隐藏状栏 全屏
     */
    public void HiddenWindosStatus() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @SuppressLint("CheckResult")
    public void checkPermissions(final Activity activity) {
        RxPermissions mPermissions = new RxPermissions(this);
        mPermissions.request(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (!aBoolean) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("提示");
                            builder.setMessage("必须同意所有权限才能使用[" + getResources().getString(R.string.app_name) + "]");
                            builder.setCancelable(false);
                            builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    activity.finish();
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton("再次请求", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermissions(activity);
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            intHander();
                        }
                    }
                });
    }
}