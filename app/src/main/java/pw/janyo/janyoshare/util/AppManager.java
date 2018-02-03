package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.drawable.DrawableFactory;
import vip.mystery0.tools.logs.Logs;

public class AppManager {
    private static final String TAG = "AppManager";
    public final static int USER = 1;
    public final static int SYSTEM = 2;

    public final static int SORT_TYPE_NONE = 0;
    public final static int SORT_TYPE_NAME_UP = 1;
    public final static int SORT_TYPE_NAME_DOWN = 2;
    public final static int SORT_TYPE_SIZE_UP = 3;
    public final static int SORT_TYPE_SIZE_DOWN = 4;
    public final static int SORT_TYPE_PACKAGE_UP = 5;
    public final static int SORT_TYPE_PACKAGE_DOWN = 6;

    public static List<InstallAPP> getInstallAPPList(Context context, int appType) {
        DrawableFactory drawableFactory = new DrawableFactory();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        List<InstallAPP> tempList = new ArrayList<>();
        List<InstallAPP> installAPPList = new ArrayList<>();
        switch (appType) {
            case USER:
                for (PackageInfo packageInfo : packageInfoList) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
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
                        tempList.add(installAPP);
                    }
                }
                installAPPList.addAll(sort(tempList));
                boolean saveUserResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.USER_LIST_FILE);
                Logs.i(TAG, "getInstallAPPList: 存储APP列表结果: " + saveUserResult);
                break;
            case SYSTEM:
                for (PackageInfo packageInfo : packageInfoList) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
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
                        tempList.add(installAPP);
                    }
                }
                installAPPList.addAll(sort(tempList));
                boolean saveSystemResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.SYSTEM_LIST_FILE);
                Logs.i(TAG, "getInstallAPPList: 存储APP列表结果: " + saveSystemResult);
                break;
        }
        return installAPPList;
    }

    private static List<InstallAPP> sort(List<InstallAPP> originList) {
        int sortType = Settings.getSortType();
        if (sortType == 0)
            return originList;
        InstallAPP array[] = new InstallAPP[originList.size()];
        for (int i = 0; i < originList.size(); i++)
            array[i] = originList.get(i);
        quickSort(array, 0, array.length - 1, sortType);
        List<InstallAPP> sortList = new ArrayList<>();
        sortList.addAll(Arrays.asList(array));
        return sortList;
    }

    private static void quickSort(InstallAPP n[], int left, int right, int sortType) {
        int dp;
        if (left < right) {
            dp = partition(n, left, right, sortType);
            quickSort(n, left, dp - 1, sortType);
            quickSort(n, dp + 1, right, sortType);
        }
    }

    private static int partition(InstallAPP n[], int left, int right, int sortType) {
        InstallAPP pivot = n[left];
        while (left < right) {
            switch (sortType) {
                case SORT_TYPE_NAME_UP:
                    while (left < right && n[right].getName().compareToIgnoreCase(pivot.getName()) >= 0)
                        right--;
                    break;
                case SORT_TYPE_NAME_DOWN:
                    while (left < right && n[right].getName().compareToIgnoreCase(pivot.getName()) <= 0)
                        right--;
                    break;
                case SORT_TYPE_SIZE_UP:
                    while (left < right && n[right].getSize() >= pivot.getSize())
                        right--;
                    break;
                case SORT_TYPE_SIZE_DOWN:
                    while (left < right && n[right].getSize() <= pivot.getSize())
                        right--;
                    break;
                case SORT_TYPE_PACKAGE_UP:
                    while (left < right && n[right].getPackageName().compareToIgnoreCase(pivot.getPackageName()) >= 0)
                        right--;
                    break;
                case SORT_TYPE_PACKAGE_DOWN:
                    while (left < right && n[right].getPackageName().compareToIgnoreCase(pivot.getPackageName()) <= 0)
                        right--;
                    break;
            }
            if (left < right)
                n[left++] = n[right];
            switch (sortType) {
                case SORT_TYPE_NAME_UP:
                    while (left < right && n[left].getName().compareToIgnoreCase(pivot.getName()) <= 0)
                        left++;
                    break;
                case SORT_TYPE_NAME_DOWN:
                    while (left < right && n[left].getName().compareToIgnoreCase(pivot.getName()) >= 0)
                        left++;
                    break;
                case SORT_TYPE_SIZE_UP:
                    while (left < right && n[left].getSize() <= pivot.getSize())
                        left++;
                    break;
                case SORT_TYPE_SIZE_DOWN:
                    while (left < right && n[left].getSize() >= pivot.getSize())
                        left++;
                    break;
                case SORT_TYPE_PACKAGE_UP:
                    while (left < right && n[left].getPackageName().compareToIgnoreCase(pivot.getPackageName()) <= 0)
                        left++;
                    break;
                case SORT_TYPE_PACKAGE_DOWN:
                    while (left < right && n[left].getPackageName().compareToIgnoreCase(pivot.getPackageName()) >= 0)
                        left++;
                    break;
            }
            if (left < right)
                n[right--] = n[left];
        }
        n[left] = pivot;
        return left;
    }
}
