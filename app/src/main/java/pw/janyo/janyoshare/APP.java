package pw.janyo.janyoshare;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

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
        context=getApplicationContext();
        Logs.setLevel(Logs.INSTANCE.getDebug());
        CrashHandler.getInstance(this)
                .init();
    }
}
