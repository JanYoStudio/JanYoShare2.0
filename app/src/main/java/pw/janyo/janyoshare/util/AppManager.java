package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.drawable.DrawableFactory;
import vip.mystery0.tools.logs.Logs;

public class AppManager {
    private static final String TAG = "AppManager";
    public final static int USER = 1;
    public final static int SYSTEM = 2;

    public static List<InstallAPP> getInstallAPPList(Context context, int appType) {
        DrawableFactory drawableFactory = new DrawableFactory();
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
                        String sourceIconPath = context.getCacheDir().getAbsolutePath() + File.separator + "icon" + File.separator + packageInfo.applicationInfo.packageName;
                        if (drawableFactory.save(packageInfo.applicationInfo.loadIcon(packageManager), sourceIconPath))
                            installAPP.setIconPath(sourceIconPath);
                        else
                            installAPP.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                        installAPP.setSize((new File(packageInfo.applicationInfo.publicSourceDir)).length());
                        installAPP.setInstallTime(packageInfo.firstInstallTime);
                        installAPP.setUpdateTime(packageInfo.lastUpdateTime);
                        installAPPList.add(installAPP);
                    }
                }
                boolean saveUserResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.USER_LIST_FILE);
                Logs.i(TAG, "getInstallAPPList: 存储APP列表结果: " + saveUserResult);
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
                        String sourceIconPath = context.getCacheDir().getAbsolutePath() + File.separator + "icon" + File.separator + packageInfo.applicationInfo.packageName;
                        if (drawableFactory.save(packageInfo.applicationInfo.loadIcon(packageManager), sourceIconPath))
                            installAPP.setIconPath(sourceIconPath);
                        else
                            installAPP.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                        installAPP.setSize((new File(packageInfo.applicationInfo.publicSourceDir)).length());
                        installAPP.setInstallTime(packageInfo.firstInstallTime);
                        installAPP.setUpdateTime(packageInfo.lastUpdateTime);
                        installAPPList.add(installAPP);
                    }
                }
                boolean saveSystemResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.SYSTEM_LIST_FILE);
                Logs.i(TAG, "getInstallAPPList: 存储APP列表结果: " + saveSystemResult);
                break;
        }
        return installAPPList;
    }
}
