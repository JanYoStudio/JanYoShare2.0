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

import java.util.ArrayList

import pw.janyo.janyoshare.classes.InstallAPP
import vip.mystery0.logs.Logs
import vip.mystery0.tools.utils.CommandTools

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
	 *
	 * @return 列表
	 */
	fun getInstallAPPList(context: Context, appType: Int): List<InstallAPP> {
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
	 *
	 * @return 排序之后的列表
	 */
	private fun sort(originList: List<InstallAPP>): List<InstallAPP> {
		return when (Settings.sortType) {
			SortType.SORT_TYPE_NAME_UP -> originList.sortedBy { it.name }
			SortType.SORT_TYPE_NAME_DOWN -> originList.sortedByDescending { it.name }
			SortType.SORT_TYPE_SIZE_UP -> originList.sortedBy { it.size }
			SortType.SORT_TYPE_SIZE_DOWN -> originList.sortedByDescending { it.size }
			SortType.SORT_TYPE_PACKAGE_UP -> originList.sortedBy { it.packageName }
			SortType.SORT_TYPE_PACKAGE_DOWN -> originList.sortedByDescending { it.packageName }
			SortType.SORT_TYPE_INSTALL_TIME_UP -> originList.sortedBy { it.installTime }
			SortType.SORT_TYPE_INSTALL_TIME_DOWN -> originList.sortedByDescending { it.installTime }
			SortType.SORT_TYPE_UPDATE_TIME_UP -> originList.sortedBy { it.updateTime }
			SortType.SORT_TYPE_UPDATE_TIME_DOWN -> originList.sortedByDescending { it.updateTime }
			else -> originList
		}
	}

	/**
	 * 冻结指定APP
	 *
	 * @param packageName 要冻结的APP的包名
	 *
	 * @return 命令执行结果
	 */
	fun disableAPP(packageName: String): Boolean {
		val result = CommandTools.execRootCommand("pm disable $packageName")
		return result.isSuccess()
	}

	/**
	 * 通过Intent请求卸载APP
	 *
	 * @param context 上下文
	 * @param packageName 卸载APP的包名
	 */
	fun uninstallAPP(context: Context, packageName: String) {
		val packageURI = Uri.parse("package:$packageName")
		val intent = Intent(Intent.ACTION_DELETE, packageURI)
		context.startActivity(intent)
	}

	/**
	 * 通过Root的方式卸载应用（静默卸载）
	 *
	 * @param packageName 要卸载的APP的包名
	 *
	 * @return 执行结果
	 */
	fun uninstallAPPByRoot(packageName: String): Boolean {
		val result = CommandTools.execRootCommand("pm uninstall $packageName")
		return result.isSuccess()
	}
}
