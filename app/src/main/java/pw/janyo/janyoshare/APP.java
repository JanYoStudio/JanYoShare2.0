package pw.janyo.janyoshare;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

import vip.mystery0.tools.crashHandler.CrashHandler;
import vip.mystery0.tools.logs.Logs;

public class APP extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);
        Logs.setLevel(Logs.INSTANCE.getRelease());
        CrashHandler.getInstance(this)
                .init();
    }
}
