package org.pmm.simpleim.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.model.HttpParams;

import org.json.JSONObject;
import org.pmm.simpleim.R;
import org.pmm.simpleim.db.IMDbDataBean;
import org.pmm.simpleim.ui.main.MainActivity;
import org.pmm.simpleim.utils.ETChangedUtlis;
import org.pmm.simpleim.utils.NetUtil;
import org.pmm.simpleim.utils.NoMoreClickListener;
import org.pmm.simpleim.utils.SPUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Views
     */
    private EditText etUserName;
    private ImageView ivUserNameCler;
    private EditText etUserPwd;
    private CheckBox cbUserPwdSh;
    private Button btLogin;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        BarUtils.setStatusBarLightMode(this, true);
        initView();
        initListener();
        etUserName.setText(SPUtils.getInstance().getString(SPUtils.USER_NAME));
    }

    private void initView() {
        etUserName = findViewById(R.id.et_user_name);
        ivUserNameCler = findViewById(R.id.iv_user_name_cler);
        etUserPwd = findViewById(R.id.et_user_pwd);
        cbUserPwdSh = findViewById(R.id.cb_user_pwd_sh);
        btLogin = findViewById(R.id.bt_login);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        ETChangedUtlis.EditTextChangedListener(etUserName, ivUserNameCler);
        ETChangedUtlis.EditTextChangedListener(etUserPwd, cbUserPwdSh);

        if (SPUtils.getInstance().getInt(SPUtils.IS_ADD_WELCMOE, 0) == 0) {
            IMDbDataBean bean = new IMDbDataBean();
            bean.setMsgid(1);
            bean.setAddtime("2020-12-13 23:18:56");
            bean.setCardname(getResources().getString(R.string.app_name));
            bean.setMessage("欢迎使用" + getResources().getString(R.string.app_name) + "APP,祝您生活愉快!");
            bean.save();
            SPUtils.getInstance().putInt(SPUtils.IS_ADD_WELCMOE, 1);
        }

    }

    private void initListener() {
        btLogin.setOnClickListener(new NoMoreClickListener(2500) {
            @Override
            public void OnMoreClick(View view) {
                super.OnMoreClick(view);

                if (checkUserNameAndPwd()) {
                    login(etUserName.getText().toString(), etUserPwd.getText().toString());
                }
            }

            @Override
            public void OnMoreErrorClick() {
                ToastUtils.showShort("操作过于频繁,请2s后重试!");
            }
        });
        //长按直接进入
        btLogin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startMain();
                return true;
            }
        });

    }

    /**
     * 验证用户名和密码
     */
    private boolean checkUserNameAndPwd() {
        if (TextUtils.isEmpty(etUserName.getText().toString()) || TextUtils.isEmpty(etUserPwd.getText().toString())) {
            ToastUtils.showShort("用户名和密码不能为空!");
            return false;
        }
        if (etUserPwd.getText().toString().length() < 4) {
            ToastUtils.showShort("密码不能小于4位!");
            return false;
        }
        return true;
    }

    /**
     * 登录
     *
     * @param name 用户名
     * @param pwd  密码
     */
    private void login(String name, String pwd) {
        mProgressBar.setVisibility(View.VISIBLE);
        btLogin.setEnabled(false);
        NetUtil mNetUtil = new NetUtil();
        HttpParams httpParams = new HttpParams();
        httpParams.put("username", name);
        httpParams.put("password", pwd);
        mNetUtil.postNetData("http://p2pmm.cn/api/index/login", httpParams, new NetUtil.DataListener() {
            @Override
            public void showDialogLoading() {
                mProgressBar.setVisibility(View.VISIBLE);
                btLogin.setEnabled(false);
            }

            @Override
            public void onSubScribe(Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onError(Throwable e) {
                mProgressBar.setVisibility(View.INVISIBLE);
                btLogin.setEnabled(true);
            }

            @Override
            public void onFaild() {
                mProgressBar.setVisibility(View.INVISIBLE);
                btLogin.setEnabled(true);
            }

            @Override
            public void dissDialogmissLoad() {
                mProgressBar.setVisibility(View.INVISIBLE);
                btLogin.setEnabled(true);
            }

            @Override
            public void onSuccess(String s) {
                btLogin.setEnabled(true);
                mProgressBar.setVisibility(View.INVISIBLE);
                LogUtils.d("请求成功:" + s);

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    int code = jsonObject.optInt("code");
                    if (code == 1) {
                        SPUtils.getInstance().putString(SPUtils.USER_NAME, name);
                        SPUtils.getInstance().putString(SPUtils.USER_UID, jsonObject.optString("userid"));
                        SPUtils.getInstance().putString(SPUtils.USER_PWD, pwd);
                        startMain();
                        ToastUtils.showShort("登录成功!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showShort("JSON数据解析异常!");
                }
            }
        });
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * 添加订阅
     *
     * @param disposable
     */
    private CompositeDisposable mCompositeDisposable;//Rxjava订阅

    public void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }
}
