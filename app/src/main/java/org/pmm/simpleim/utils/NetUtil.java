package org.pmm.simpleim.utils;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;

import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class NetUtil {
    public static String IMAGE_URL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1607879910986&di=8eb71c943b35cec1ea54a7883b53b18b&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201901%2F19%2F20190119231949_buetu.jpg";

    public interface DataListener {
        void showDialogLoading();

        void onSubScribe(Disposable d);

        void onError(Throwable e);

        void onFaild();

        void dissDialogmissLoad();

        void onSuccess(String s);
    }


    /**
     * 网络数据
     *
     * @param dataListener
     */
    public void postNetData(String url, HttpParams httpParams, final DataListener dataListener) {
        OkGo.<String>post(url)
                .params(httpParams)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<String>())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (dataListener != null) {
                            dataListener.showDialogLoading();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<String>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (dataListener != null) {
                            dataListener.onSubScribe(d);
                        }
                    }

                    @Override
                    public void onNext(Response<String> stringResponse) {
                        try {
                            if (stringResponse.isSuccessful()) {
                                String s = stringResponse.body();
                                if (!TextUtils.isEmpty(s)) {
                                    JSONObject jsonObject = new JSONObject(s);
                                    int code = jsonObject.optInt("code");
                                    if (code == 1) {
                                        dataListener.onSuccess(s);
                                    } else {
                                        ToastUtils.showShort(jsonObject.optString("message"));
                                    }
                                } else {
                                    if (dataListener != null) {
                                        dataListener.onFaild();
                                    }
                                    ToastUtils.showShort("解析服务器数据错误");
                                }
                            } else {
                                if (dataListener != null) {
                                    dataListener.onFaild();
                                }
                                ToastUtils.showShort("请求服务器数据失败");
                            }
                        } catch (Exception e) {
                            if (dataListener != null) {
                                dataListener.onFaild();
                            }
                            ToastUtils.showShort("解析服务器数据错误");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShort("连接服务器失败，请稍候再试。");
                        if (dataListener != null) {
                            dataListener.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (dataListener != null) {
                            dataListener.dissDialogmissLoad();
                        }
                    }
                });
    }
}
