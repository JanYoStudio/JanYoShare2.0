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

package pw.janyo.janyoshare.classes

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.drawable.DrawableFactory
import java.io.File

class InstallAPP {
	lateinit var name: String
	lateinit var versionName: String
	var versionCode: Int = 0
	lateinit var sourceDir: String
	lateinit var packageName: String
	var size: Long = 0
	var installTime: Long = 0
	var updateTime: Long = 0
	var iconPath: String? = null
	@Transient
	var icon: Drawable? = null
	var isDisable = false

	fun convertPackageInfo(packageInfo: PackageInfo, packageManager: PackageManager): InstallAPP {
		name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
		versionName = packageInfo.versionName
		versionCode = packageInfo.versionCode
		sourceDir = packageInfo.applicationInfo.sourceDir
		packageName = packageInfo.applicationInfo.packageName
		val sourceIconPath = JanYoFileUtil.getIconPath(packageName)
		size = File(packageInfo.applicationInfo.publicSourceDir).length()
		installTime = packageInfo.firstInstallTime
		updateTime = packageInfo.lastUpdateTime
		val drawable = packageInfo.applicationInfo.loadIcon(packageManager)
		if (DrawableFactory().save(drawable, sourceIconPath))
			iconPath = sourceIconPath
		else
			icon = drawable
		isDisable = !packageInfo.applicationInfo.enabled
		return this
	}
}
