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

package pw.janyo.janyoshare.model

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import pw.janyo.janyoshare.utils.JanYoFileUtil
import pw.janyo.janyoshare.utils.drawable.DrawableFactory
import java.io.File

@Entity(tableName = "tb_install_app")
class InstallAPP {
	@PrimaryKey(autoGenerate = true)
	var id = 0

	@ColumnInfo(name = "ia_name")
	lateinit var name: String
	@ColumnInfo(name = "ia_version_name")
	lateinit var versionName: String
	@ColumnInfo(name = "ia_version_code")
	var versionCode: Int = 0
	@ColumnInfo(name = "ia_source_dir")
	lateinit var sourceDir: String
	@ColumnInfo(name = "ia_package_name")
	lateinit var packageName: String
	@ColumnInfo(name = "ia_size")
	var size: Long = 0
	@ColumnInfo(name = "ia_install_time")
	var installTime: Long = 0
	@ColumnInfo(name = "ia_update_time")
	var updateTime: Long = 0
	@ColumnInfo(name = "ia_icon_path")
	var iconPath: String? = null
	@Transient
	@Ignore
	var icon: Drawable? = null
	@ColumnInfo(name = "ia_disable")
	var isDisable = false

	@ColumnInfo(name = "ia_type")
	var type = 0

	fun convertPackageInfo(packageInfo: PackageInfo, packageManager: PackageManager, type: Int): InstallAPP {
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
		this.type = type
		return this
	}

	override fun equals(other: Any?): Boolean =other is InstallAPP && packageName == other.packageName
}
