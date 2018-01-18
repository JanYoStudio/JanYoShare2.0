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

    public static int getTempDir() {
        return SHARED_PREFERENCES.getInt("tempDir", 0);
    }

    public static void setTempDir(int tempDir) {
        SHARED_PREFERENCES.edit().putInt("tempDir", tempDir).apply();
    }
}
