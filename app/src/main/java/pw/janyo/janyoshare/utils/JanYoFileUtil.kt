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

package pw.janyo.janyoshare.utils

import android.content.Context
import android.os.Environment
import android.text.TextUtils

import com.google.gson.Gson
import com.google.gson.JsonParser

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Calendar

import pw.janyo.janyoshare.config.APP
import pw.janyo.janyoshare.model.InstallAPP
import vip.mystery0.logs.Logs
import vip.mystery0.tools.utils.FileTools

object JanYoFileUtil {
	private const val JANYO_SHARE = "JanYo Share"//临时目录名
	const val USER_LIST_FILE = "user.list"//用户软件列表存储文件名
	const val SYSTEM_LIST_FILE = "system.list"//系统软件列表存储文件名

	object Export {
		const val EXPORT_DIR_SDCARD_DATA = 0//导出到sdcard的data
		const val EXPORT_DIR_SDCARD = 1//导出到sdcard根目录
		const val EXPORT_DIR_CUSTOM = 2//导出到自定义目录
	}

	object Code {
		const val DONE = 100//完成
		const val ERROR = 101//失败
		const val FILE_NOT_EXIST = 102//文件不存在
		const val FILE_EXIST = 103//文件已存在
		const val DIR_NOT_EXIST = 104//目录不存在
		const val MAKE_DIR_ERROR = 105//创建目录失败
	}

	private var EXPORT_APK_DIR: File? = null

	/**
	 * 获取导出目录绝对路径
	 *
	 * @return 路径
	 */
	val exportDirPath: String
		get() {
			initExportDir()
			return EXPORT_APK_DIR!!.absolutePath
		}

	/**
	 * 获取icon存储路径
	 *
	 * @return 路径
	 */
	fun getIconPath(packageName: String): String {
		return "${APP.context.cacheDir.absolutePath}${File.separator}icon${File.separator}$packageName"
	}

	/**
	 * 获取导出目录，返回File对象
	 *
	 * @return File对象
	 */
	val exportDirFile: File
		get() = File(exportDirPath)

	/**
	 * 判断导出目录是否存在
	 *
	 * @return boolean
	 */
	val isExportDirExist: Boolean
		get() {
			initExportDir()
			return EXPORT_APK_DIR!!.exists() || EXPORT_APK_DIR!!.mkdirs()
		}

	/**
	 * 初始化存储的临时目录
	 */
	private fun initExportDir() {
		EXPORT_APK_DIR = when (Settings.exportDir) {
			Export.EXPORT_DIR_SDCARD_DATA -> File(APP.context.getExternalFilesDir(null), JANYO_SHARE)
			Export.EXPORT_DIR_SDCARD -> File(Environment.getExternalStorageDirectory(), JANYO_SHARE)
			Export.EXPORT_DIR_CUSTOM -> File(Settings.customExportDir)
			else -> throw NullPointerException("存储位置错误")
		}
	}

	/**
	 * 清理临时目录下文件
	 */
	fun cleanFileDir(): Int {
		initExportDir()
		if (!EXPORT_APK_DIR!!.exists()) {
			val mkdirs = EXPORT_APK_DIR!!.mkdirs()
			Logs.i("cleanFileDir: 创建导出临时目录: $mkdirs")
			if (!mkdirs)
				return Code.MAKE_DIR_ERROR
		}
		if (EXPORT_APK_DIR!!.isDirectory) {
			for (file in EXPORT_APK_DIR!!.listFiles())
				Logs.i("cleanFileDir: fileName: " + file.name + " deleteResult: " + file.delete())
			return Code.DONE
		}
		return Code.ERROR
	}

	/**
	 * 通过路径获取文件扩展名
	 *
	 * @param filePath 文件路径
	 * @return 扩展名
	 */
	fun getExtensionFileName(filePath: String): String {
		val dot = filePath.lastIndexOf('.')
		return if (dot > -1 && dot < filePath.length - 1) filePath.substring(dot + 1) else ""
	}

	/**
	 * 获取文件扩展名，适用于拼接路径字符串
	 *
	 * @param filePath 文件路径
	 * @return 扩展名（带点或者空）
	 */
	fun appendExtensionFileName(filePath: String?): String {
		val extensionFileName = getExtensionFileName(filePath!!)
		return if (TextUtils.isEmpty(extensionFileName)) "" else ".$extensionFileName"
	}

	/**
	 * 获取指定软件默认导出绝对路径
	 *
	 * @param installAPP 指定的软件
	 * @return 绝对路径
	 */
	fun getExportFilePath(installAPP: InstallAPP): String {
		val extendFileName = getExtensionFileName(installAPP.sourceDir)
		return exportDirPath + File.separator + formatName(installAPP, Settings.renameFormat) + if (extendFileName.isEmpty()) "" else ".$extendFileName"
	}

	/**
	 * 同上，返回类型为File
	 *
	 * @param installAPP 指定的软件
	 * @return File对象
	 */
	fun getExportFile(installAPP: InstallAPP): File {
		return File(getExportFilePath(installAPP))
	}

	/**
	 * 获取导出目录下指定文件
	 *
	 * @param fileNameWithExtends 文件名（含扩展名）
	 * @return File对象
	 */
	fun getFile(fileNameWithExtends: String): File {
		return File(exportDirFile, fileNameWithExtends)
	}

	/**
	 * 导出指定软件到导出目录，未重命名
	 *
	 * @param installAPP 制定软件
	 * @return 返回码
	 */
	fun exportAPK(installAPP: InstallAPP): Int {
		if (!isExportDirExist)
			return Code.DIR_NOT_EXIST
		val oldName = installAPP.sourceDir.substring(installAPP.sourceDir.lastIndexOf(File.separator) + 1)
		val outputPath = EXPORT_APK_DIR!!.absolutePath + File.separator + formatName(installAPP, Settings.renameFormat) + appendExtensionFileName(oldName)
		return FileTools.copyFile(installAPP.sourceDir,outputPath)
	}

	/**
	 * 重命名文件
	 *
	 * @param installAPP 导出的软件
	 * @param fileName   新的文件名（不包含扩展名）
	 * @param isDelete   是否删除已存在的文件
	 * @return 返回码
	 */
	fun renameFile(installAPP: InstallAPP, fileName: String, isDelete: Boolean): Int {
		val exportFile = getExportFile(installAPP)
		if (!exportFile.exists())
			return Code.FILE_NOT_EXIST
		val newFile = File(exportDirFile, fileName + appendExtensionFileName(installAPP.sourceDir))
		if (newFile.exists())
			if (isDelete)

				newFile.delete()
			else
				return Code.FILE_EXIST
		return if (exportFile.renameTo(newFile)) Code.DONE else Code.ERROR
	}

	/**
	 * 重命名文件扩展名
	 *
	 * @param installAPP    导出的软件
	 * @param extensionName 新的扩展名
	 * @param isDelete      是否删除已存在的文件
	 * @return 返回码
	 */
	fun renameExtension(installAPP: InstallAPP, extensionName: String, isDelete: Boolean): Int {
		val exportFile = getExportFile(installAPP)
		if (!exportFile.exists())
			return Code.FILE_NOT_EXIST
		val newFile = File(exportDirFile, formatName(installAPP, Settings.renameFormat) + if (TextUtils.isEmpty(extensionName)) "" else ".$extensionName")
		if (newFile.exists())
			if (isDelete)

				newFile.delete()
			else
				return Code.FILE_EXIST
		return if (exportFile.renameTo(newFile)) Code.DONE else Code.ERROR
	}

	/**
	 * 存储app临时列表
	 *
	 * @param context  Context
	 * @param list     列表
	 * @param fileName 存储的文件名
	 * @return 存储结果
	 */
	fun saveAppList(context: Context, list: List<InstallAPP>, fileName: String): Boolean {
		val file = File(context.externalCacheDir, fileName)
		return saveObject(list, file)
	}

	/**
	 * 存储对象
	 *
	 * @param any 要存储的对象
	 * @param file   存储到的文件
	 * @return 结果
	 */
	fun saveObject(any: Any, file: File): Boolean {
		val gson = Gson()
		return saveMessage(gson.toJson(any), file)
	}

	/**
	 * 存储文本信息
	 *
	 * @param message 信息
	 * @param file    文件
	 * @return 结果
	 */
	fun saveMessage(message: String, file: File): Boolean {
		if (file.exists())
			file.delete()
		if (!file.parentFile.exists())
			file.parentFile.mkdirs()
		var fileOutputStream: FileOutputStream? = null
		try {
			fileOutputStream = FileOutputStream(file)
			fileOutputStream.write(message.toByteArray())
		} catch (e: Exception) {
			e.printStackTrace()
			return false
		} finally {
			fileOutputStream?.close()
		}
		return true
	}

	/**
	 * 从文件中获取缓存的列表
	 *
	 * @param file   文件
	 * @param tClass 列表中的类
	 * @param <T>    泛型
	 * @return 列表
	</T> */
	fun <T> getListFromFile(file: File, tClass: Class<T>): List<T> {
		if (!file.exists())
			return ArrayList()
		var fileInputStream: FileInputStream? = null
		try {
			val parser = JsonParser()
			val gson = Gson()
			fileInputStream = FileInputStream(file)
			val jsonArray = parser.parse(InputStreamReader(fileInputStream)).asJsonArray
			val list = ArrayList<T>()
			for (jsonElement in jsonArray) {
				list.add(gson.fromJson(jsonElement, tClass))
			}
			return list
		} catch (e: Exception) {
			e.printStackTrace()
			return ArrayList()
		} finally {
			fileInputStream?.close()
		}
	}

	/**
	 * 判断缓存是否可用
	 *
	 * @param context Context
	 * @return 结果
	 */
	fun isCacheAvailable(context: Context, fileName: String): Boolean {
		if (Settings.cacheExpirationTime == 0f)
			return true
		val dir = context.externalCacheDir ?: return false
		val cacheFile = File(dir, fileName)
		if (!cacheFile.exists())
			return false
		val now = Calendar.getInstance().timeInMillis
		val modified = cacheFile.lastModified()
		return now - modified <= Settings.cacheExpirationTime
	}

	/**
	 * 检查数据包是否存在
	 *
	 * @param packageName 包名
	 * @return 列表，不存在则为空
	 */
	fun checkObb(packageName: String): List<File> {
		val list = ArrayList<File>()
		val dir = File(Environment.getExternalStoragePublicDirectory("Android").toString() + File.separator + "obb" + File.separator + packageName + File.separator)
		if (!dir.exists() || !dir.isDirectory)
			return list
		for (temp in dir.listFiles())
			if (temp.absolutePath.toLowerCase().endsWith("obb"))
				list.add(temp)
		return list
	}

	/**
	 * 格式化文件名
	 *
	 * @param installAPP 提取的软件
	 * @param format     格式
	 * @return 文件名，不包含扩展名
	 */
	fun formatName(installAPP: InstallAPP, format: String): String {
		val fileName = StringBuilder()
		var index = 0
		while (index < format.length)
			if (format[index] == '%' && index + 1 < format.length)
				when (format[index + 1]) {
					'N' -> {
						fileName.append(installAPP.name)
						index += 2
					}
					'V' -> {
						fileName.append(installAPP.versionName)
						index += 2
					}
					'W' -> {
						fileName.append(installAPP.versionCode)
						index += 2
					}
					'P' -> {
						fileName.append(installAPP.packageName)
						index += 2
					}
					else -> {
						fileName.append('%')
						index++
					}
				}
			else {
				fileName.append(format[index])
				index++
			}
		return fileName.toString()
	}
}