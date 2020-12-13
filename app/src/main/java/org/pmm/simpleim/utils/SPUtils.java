package org.pmm.simpleim.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.pmm.simpleim.ui.app.MyApp;


/**
 * Created by 蒋 on 2018/4/23.
 * 单例
 * SP工具类
 */

public class SPUtils {
    private SharedPreferences share;
    private SharedPreferences.Editor editor;
    private String SHARED_NAME = "sp_simp_im";//sp的文件名

    private SPUtils() {
        share = MyApp.getContext().getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        editor = share.edit();
    }

    public static SPUtils getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final SPUtils sInstance = new SPUtils();
    }

    /**
     * ------- Int ---------
     */
    public void putInt(String spName, int value) {
        editor.putInt(spName, value);
        editor.commit();
    }

    public int getInt(String spName, int defaultvalue) {
        return share.getInt(spName, defaultvalue);
    }


    /**
     * ------- String ---------
     */
    public void putString(String spName, String value) {
        editor.putString(spName, value);
        editor.commit();
    }

    public String getString(String spName, String defaultvalue) {
        return share.getString(spName, defaultvalue);
    }

    public String getString(String spName) {
        return share.getString(spName, "");
    }


    /**
     * ------- boolean ---------
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return share.getBoolean(key, defValue);
    }

    /**
     * ------- float ---------
     */
    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloat(String key, float defValue) {
        return share.getFloat(key, defValue);
    }


    /**
     * ------- long---------
     */
    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, long defValue) {
        return share.getLong(key, defValue);
    }


    /**
     * 清空SP里所有数据
     */
    public void clear() {
        editor.clear();//清空
        editor.commit();//提交
    }

    /**
     * 删除SP里指定key对应的数据项
     *
     * @param key
     */
    public void remove(String key) {
        editor.remove(key);//删除掉指定的值
        editor.commit();//提交
    }

    /**
     * 查看sp文件里面是存在 key
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return share.contains(key);
    }

    //---------------------Key---------------------------//
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_PWD = "USER_PWD";
    public static final String USER_UID = "USER_UID";
    public static final String IS_ADD_WELCMOE = "is_add_welcome";//messageA 聊天列表

    public static final String CHAT_MESSAGE_A = "messagesA";//messageA 聊天列表
    public static final String CHAT_MESSAGE_B = "messagesB";//messageB 聊天详情
    //---------------------Key---------------------------//
}
