package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pw.janyo.janyoshare.classes.InstallAPP;
import vip.mystery0.tools.logs.Logs;

public class AppManager {
    private static final String TAG = "AppManager";
    public final static int USER = 1;
    public final static int SYSTEM = 2;

    public static List<InstallAPP> getInstallAPPList(Context context, int appType) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        List<InstallAPP> installAPPList = new ArrayList<>();
        switch (appType) {
            case USER:
                for (PackageInfo packageInfo : packageInfoList) {
                    if ((packageInfo.applicationInfo.flags | ApplicationInfo.FLAG_SYSTEM) > 0) {
                        InstallAPP installAPP = new InstallAPP();
                        installAPP.setName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
                        installAPP.setVersionName(packageInfo.versionName);
                        installAPP.setVersionCode(packageInfo.versionCode);
                        installAPP.setSourceDir(packageInfo.applicationInfo.sourceDir);
                        installAPP.setPackageName(packageInfo.applicationInfo.packageName);
//                        String sourceIconPath=context.getCacheDir()
                        installAPP.setSize((new File(packageInfo.applicationInfo.publicSourceDir)).length());
                        installAPP.setInstallTime(packageInfo.firstInstallTime);
                        installAPP.setUpdateTime(packageInfo.lastUpdateTime);
                        installAPPList.add(installAPP);
                    }
                }
                break;
            case SYSTEM:
                for (PackageInfo packageInfo : packageInfoList) {
                    if ((packageInfo.applicationInfo.flags | ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        InstallAPP installAPP = new InstallAPP();
                        installAPP.setName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
                        installAPP.setVersionName(packageInfo.versionName);
                        installAPP.setVersionCode(packageInfo.versionCode);
                        installAPP.setSourceDir(packageInfo.applicationInfo.sourceDir);
                        installAPP.setPackageName(packageInfo.applicationInfo.packageName);
//                        String sourceIconPath=context.getCacheDir()
                        installAPP.setSize((new File(packageInfo.applicationInfo.publicSourceDir)).length());
                        installAPP.setInstallTime(packageInfo.firstInstallTime);
                        installAPP.setUpdateTime(packageInfo.lastUpdateTime);
                        installAPPList.add(installAPP);
                    }
                }
                break;
        }
        return installAPPList;
    }
}
