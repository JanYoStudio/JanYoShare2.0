package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.SharedPreferences;

import pw.janyo.janyoshare.APP;

public class Settings {
    private static final SharedPreferences SHARED_PREFERENCES = APP.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

    private Settings() {
    }

    public static boolean isAutoClean() {
        return SHARED_PREFERENCES.getBoolean("isAutoClean", true);
    }

    public static void setAutoClean(boolean autoClean) {
        SHARED_PREFERENCES.edit().putBoolean("isAutoClean", autoClean).apply();
    }

    public static int getExportDir() {
        return SHARED_PREFERENCES.getInt("tempDir", 0);
    }

    public static void setExportDir(int tempDir) {
        SHARED_PREFERENCES.edit().putInt("tempDir", tempDir).apply();
    }

    public static boolean isCustomFormat() {
        return SHARED_PREFERENCES.getBoolean("isCustomFormat", false);
    }

    public static void setCustomFormat(boolean isCustomFormat) {
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        if (!isCustomFormat)
            editor.remove("renameFormat");
        editor.putBoolean("isCustomFormat", isCustomFormat);
        editor.apply();
    }

    public static String getRenameFormat() {//默认格式：名称-版本名称
        return SHARED_PREFERENCES.getString("renameFormat", "%N-%V");
    }

    public static void setRenameFormat(String renameFormat) {
        SHARED_PREFERENCES.edit().putString("renameFormat", renameFormat).apply();
    }
}
