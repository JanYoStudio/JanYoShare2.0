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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.drawable.DrawableFactory;
import vip.mystery0.logs.Logs;

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
	public final static int SORT_TYPE_INSTALL_TIME_UP = 7;
	public final static int SORT_TYPE_INSTALL_TIME_DOWN = 8;
	public final static int SORT_TYPE_UPDATE_TIME_UP = 9;
	public final static int SORT_TYPE_UPDATE_TIME_DOWN = 10;

	/**
	 * 获取安装的APP列表
	 *
	 * @param context 上下文
	 * @param appType 获取列表的类型
	 * @return 列表
	 */
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
				boolean saveUserResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.USER_LIST_FILE + String.valueOf(Settings.getSortType()));
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
				boolean saveSystemResult = JanYoFileUtil.saveAppList(context, installAPPList, JanYoFileUtil.SYSTEM_LIST_FILE + String.valueOf(Settings.getSortType()));
				Logs.i(TAG, "getInstallAPPList: 存储APP列表结果: " + saveSystemResult);
				break;
		}
		return installAPPList;
	}

	/**
	 * 从指定列表中搜索APP
	 *
	 * @param originList 指定列表
	 * @param query      查询关键词
	 */
	public static void search(List<InstallAPP> originList, String query) {
		Iterator<InstallAPP> iterator = originList.iterator();
		while (iterator.hasNext()) {
			InstallAPP installAPP = iterator.next();
			if (!installAPP.getName().toLowerCase().contains(query.toLowerCase()) && !installAPP.getPackageName().toLowerCase().contains(query.toLowerCase()))
				iterator.remove();
		}
	}

	/**
	 * 排序指定列表
	 *
	 * @param originList 指定列表
	 * @return 排序之后的列表
	 */
	private static List<InstallAPP> sort(List<InstallAPP> originList) {
		final int sortType = Settings.getSortType();
		if (sortType == 0)
			return originList;
		InstallAPP array[] = (InstallAPP[]) originList.toArray();
		Arrays.sort(array, new Comparator<InstallAPP>() {
			@Override
			public int compare(InstallAPP app1, InstallAPP app2) {
				return compareInstallAPP(app1, app2, sortType);
			}
		});
		return new ArrayList<>(Arrays.asList(array));
	}

	/**
	 * 比对两个 {@link InstallAPP} 的指定类型大小
	 *
	 * @param app1     第一个
	 * @param app2     第二个
	 * @param sortType 排序的类型
	 * @return 比对结果
	 */
	private static int compareInstallAPP(InstallAPP app1, InstallAPP app2, int sortType) {
		switch (sortType) {
			case SORT_TYPE_NAME_UP:
				return app1.getName().compareTo(app2.getName());
			case SORT_TYPE_NAME_DOWN:
				return -app1.getName().compareTo(app2.getName());
			case SORT_TYPE_SIZE_UP:
				if (app1.getSize() > app2.getSize())
					return 1;
				else if (app1.getSize() < app2.getSize())
					return -1;
				return 0;
			case SORT_TYPE_SIZE_DOWN:
				if (app1.getSize() > app2.getSize())
					return -1;
				else if (app1.getSize() < app2.getSize())
					return 1;
				return 0;
			case SORT_TYPE_PACKAGE_UP:
				return app1.getPackageName().compareTo(app2.getPackageName());
			case SORT_TYPE_PACKAGE_DOWN:
				return -app1.getPackageName().compareTo(app2.getPackageName());
			case SORT_TYPE_INSTALL_TIME_UP:
				if (app1.getInstallTime() > app2.getInstallTime())
					return 1;
				else if (app1.getInstallTime() < app2.getInstallTime())
					return -1;
				return 0;
			case SORT_TYPE_INSTALL_TIME_DOWN:
				if (app1.getInstallTime() > app2.getInstallTime())
					return -1;
				else if (app1.getInstallTime() < app2.getInstallTime())
					return 1;
				return 0;
			case SORT_TYPE_UPDATE_TIME_UP:
				if (app1.getUpdateTime() > app2.getUpdateTime())
					return 1;
				else if (app1.getUpdateTime() < app2.getUpdateTime())
					return -1;
				return 0;
			case SORT_TYPE_UPDATE_TIME_DOWN:
				if (app1.getUpdateTime() > app2.getUpdateTime())
					return -1;
				else if (app1.getUpdateTime() < app2.getUpdateTime())
					return 1;
				return 0;
		}
		return 0;
	}

	public static void uninstallAPP(Context context, String packageName) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(intent);
	}

	public static boolean uninstallAPPByRoot(){

	}
}
