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

package pw.janyo.janyoshare.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri

import java.io.File
import java.util.ArrayList
import java.util.Arrays

import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.util.drawable.DrawableFactory
import vip.mystery0.logs.Logs

object AppManager {
	object AppType {
		const val USER = 1
		const val SYSTEM = 2
	}

	object SortType {
		const val SORT_TYPE_NONE = 0
		const val SORT_TYPE_NAME_UP = 1
		const val SORT_TYPE_NAME_DOWN = 2
		const val SORT_TYPE_SIZE_UP = 3
		const val SORT_TYPE_SIZE_DOWN = 4
		const val SORT_TYPE_PACKAGE_UP = 5
		const val SORT_TYPE_PACKAGE_DOWN = 6
		const val SORT_TYPE_INSTALL_TIME_UP = 7
		const val SORT_TYPE_INSTALL_TIME_DOWN = 8
		const val SORT_TYPE_UPDATE_TIME_UP = 9
		const val SORT_TYPE_UPDATE_TIME_DOWN = 10
	}

	/**
	 * 获取安装的APP列表
	 *
	 * @param context 上下文
	 * @param appType 获取列表的类型
	 * @return 列表
	 */
	fun getInstallAPPList(context: Context, appType: Int): List<InstallAPP> {
		val drawableFactory = DrawableFactory()
		val packageManager = context.packageManager
		val packageInfoList = packageManager.getInstalledPackages(0)
		val tempList = ArrayList<InstallAPP>()
		val installAPPList = ArrayList<InstallAPP>()
		when (appType) {
			AppType.USER -> {
				for (packageInfo in packageInfoList)
					if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0)
						tempList.add(InstallAPP().convertPackageInfo(packageInfo, packageManager))
				installAPPList.addAll(sort(tempList))
				val saveUserResult = JanYoFileUtil.saveAppList(context, installAPPList, "${JanYoFileUtil.USER_LIST_FILE}${Settings.sortType}")
				Logs.i("getInstallAPPList: 存储APP列表结果: $saveUserResult")
			}
			AppType.SYSTEM -> {
				for (packageInfo in packageInfoList)
					if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0)
						tempList.add(InstallAPP().convertPackageInfo(packageInfo, packageManager))
				installAPPList.addAll(sort(tempList))
				val saveSystemResult = JanYoFileUtil.saveAppList(context, installAPPList, "${JanYoFileUtil.SYSTEM_LIST_FILE}${Settings.sortType}")
				Logs.i("getInstallAPPList: 存储APP列表结果: $saveSystemResult")
			}
		}
		return installAPPList
	}

	/**
	 * 从指定列表中搜索APP
	 *
	 * @param originList 指定列表
	 * @param query      查询关键词
	 */
	fun search(originList: MutableList<InstallAPP>, query: String) {
		val iterator = originList.iterator()
		while (iterator.hasNext()) {
			val installAPP = iterator.next()
			if (!installAPP.name.toLowerCase().contains(query.toLowerCase()) && !installAPP.packageName.toLowerCase().contains(query.toLowerCase()))
				iterator.remove()
		}
	}

	/**
	 * 排序指定列表
	 *
	 * @param originList 指定列表
	 * @return 排序之后的列表
	 */
	private fun sort(originList: List<InstallAPP>): List<InstallAPP> {
		val sortType = Settings.sortType
		if (sortType == 0)
			return originList
		val array = originList.toTypedArray()
		Arrays.sort(array) { app1, app2 -> compareInstallAPP(app1, app2, sortType) }
		return ArrayList(Arrays.asList(*array))
	}

	/**
	 * 比对两个 [InstallAPP] 的指定类型大小
	 *
	 * @param app1     第一个
	 * @param app2     第二个
	 * @param sortType 排序的类型
	 * @return 比对结果
	 */
	private fun compareInstallAPP(app1: InstallAPP, app2: InstallAPP, sortType: Int): Int {
		when (sortType) {
			SortType.SORT_TYPE_NAME_UP -> return app1.name.compareTo(app2.name)
			SortType.SORT_TYPE_NAME_DOWN -> return -app1.name.compareTo(app2.name)
			SortType.SORT_TYPE_SIZE_UP -> {
				if (app1.size > app2.size)
					return 1
				else if (app1.size < app2.size)
					return -1
				return 0
			}
			SortType.SORT_TYPE_SIZE_DOWN -> {
				if (app1.size > app2.size)
					return -1
				else if (app1.size < app2.size)
					return 1
				return 0
			}
			SortType.SORT_TYPE_PACKAGE_UP -> return app1.packageName.compareTo(app2.packageName)
			SortType.SORT_TYPE_PACKAGE_DOWN -> return -app1.packageName.compareTo(app2.packageName)
			SortType.SORT_TYPE_INSTALL_TIME_UP -> {
				if (app1.installTime > app2.installTime)
					return 1
				else if (app1.installTime < app2.installTime)
					return -1
				return 0
			}
			SortType.SORT_TYPE_INSTALL_TIME_DOWN -> {
				if (app1.installTime > app2.installTime)
					return -1
				else if (app1.installTime < app2.installTime)
					return 1
				return 0
			}
			SortType.SORT_TYPE_UPDATE_TIME_UP -> {
				if (app1.updateTime > app2.updateTime)
					return 1
				else if (app1.updateTime < app2.updateTime)
					return -1
				return 0
			}
			SortType.SORT_TYPE_UPDATE_TIME_DOWN -> {
				if (app1.updateTime > app2.updateTime)
					return -1
				else if (app1.updateTime < app2.updateTime)
					return 1
				return 0
			}
		}
		return 0
	}

	fun uninstallAPP(context: Context, packageName: String) {
		val packageURI = Uri.parse("package:$packageName")
		val intent = Intent(Intent.ACTION_DELETE, packageURI)
		context.startActivity(intent)
	}

//	fun uninstallAPPByRoot(): Boolean {
//
//	}
}
