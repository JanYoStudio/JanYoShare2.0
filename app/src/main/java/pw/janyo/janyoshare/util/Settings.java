/*
 * Created by Mystery0 on 18-2-10 下午4:00.
 * Copyright (c) 2018. All Rights reserved.
 *
 *                    =====================================================
 *                    =                                                   =
 *                    =                       _oo0oo_                     =
 *                    =                      o8888888o                    =
 *                    =                      88" . "88                    =
 *                    =                      (| -_- |)                    =
 *                    =                      0\  =  /0                    =
 *                    =                    ___/`---'\___                  =
 *                    =                  .' \\|     |# '.                 =
 *                    =                 / \\|||  :  |||# \                =
 *                    =                / _||||| -:- |||||- \              =
 *                    =               |   | \\\  -  #/ |   |              =
 *                    =               | \_|  ''\---/''  |_/ |             =
 *                    =               \  .-\__  '-'  ___/-. /             =
 *                    =             ___'. .'  /--.--\  `. .'___           =
 *                    =          ."" '<  `.___\_<|>_/___.' >' "".         =
 *                    =         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       =
 *                    =         \  \ `_.   \_ __\ /__ _/   .-` /  /       =
 *                    =     =====`-.____`.___ \_____/___.-`___.-'=====    =
 *                    =                       `=---='                     =
 *                    =     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   =
 *                    =                                                   =
 *                    =               佛祖保佑         永无BUG              =
 *                    =                                                   =
 *                    =====================================================
 *
 * Last modified 18-2-10 下午4:00
 */

package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.util.Locale;

import pw.janyo.janyoshare.APP;

public class Settings {
    private static final SharedPreferences SHARED_PREFERENCES = APP.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

    private Settings() {
    }

    public static boolean isAutoClean() {
        return SHARED_PREFERENCES.getBoolean(Constant.IS_AUTO_CLEAN, true);
    }

    public static void setAutoClean(boolean autoClean) {
        SHARED_PREFERENCES.edit().putBoolean(Constant.IS_AUTO_CLEAN, autoClean).apply();
    }

    public static int getExportDir() {
        return SHARED_PREFERENCES.getInt(Constant.TEMP_DIR, 0);
    }

    public static void setExportDir(int tempDir) {
        SHARED_PREFERENCES.edit().putInt(Constant.TEMP_DIR, tempDir).apply();
    }

    public static String getCustomExportDir() {
        return SHARED_PREFERENCES.getString(Constant.CUSTOM_TEMP_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static void setCustomExportDir(String customExportDir) {
        SHARED_PREFERENCES.edit().putString(Constant.CUSTOM_TEMP_DIR, customExportDir).apply();
    }

    public static boolean isCustomFormat() {
        return SHARED_PREFERENCES.getBoolean(Constant.IS_CUSTOM_FORMAT, false);
    }

    public static void setCustomFormat(boolean isCustomFormat) {
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        if (!isCustomFormat)
            editor.remove(Constant.RENAME_FORMAT);
        editor.putBoolean(Constant.IS_CUSTOM_FORMAT, isCustomFormat);
        editor.apply();
    }

    public static String getRenameFormat() {//默认格式：名称-版本名称
        return SHARED_PREFERENCES.getString(Constant.RENAME_FORMAT, "%N-%V");
    }

    public static void setRenameFormat(String renameFormat) {
        SHARED_PREFERENCES.edit().putString(Constant.RENAME_FORMAT, renameFormat).apply();
    }

    public static String getNickName() {
        return SHARED_PREFERENCES.getString(Constant.NICK_NAME, "janyo");
    }

    public static void setNickName(String nickName) {
        SHARED_PREFERENCES.edit().putString(Constant.NICK_NAME, nickName).apply();
    }

    public static int getSortType() {
        return SHARED_PREFERENCES.getInt(Constant.SORT_TYPE, AppManager.SORT_TYPE_NONE);
    }

    public static void setSortType(int sortType) {
        SHARED_PREFERENCES.edit().putInt(Constant.SORT_TYPE, sortType).apply();
    }

    public static int getCurrentListSize(int appType) {
        return SHARED_PREFERENCES.getInt(String.format(Locale.CHINESE, Constant.CURRENT_LIST_SIZE, appType), 0);
    }

    public static void setCurrentListSize(int appType, int size) {
        SHARED_PREFERENCES.edit().putInt(String.format(Locale.CHINESE, Constant.CURRENT_LIST_SIZE, appType), size).apply();
    }

    public static long getCacheExpirationTime() {
        return SHARED_PREFERENCES.getLong(Constant.CACHE_EXPIRATION_TIME, 0);
    }

    public static void setCacheExpirationTime(long cacheExpirationTime) {
        SHARED_PREFERENCES.edit().putLong(Constant.CACHE_EXPIRATION_TIME, cacheExpirationTime).apply();
    }
}
