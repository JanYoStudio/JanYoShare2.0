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

import pw.janyo.janyoshare.config.APP
import java.text.SimpleDateFormat
import java.util.*

object Settings {
	private val SHARED_PREFERENCES = APP.context.getSharedPreferences("settings", Context.MODE_PRIVATE)

	var isAutoClean: Boolean
		get() = SHARED_PREFERENCES.getBoolean(Constant.IS_AUTO_CLEAN, true)
		set(autoClean) {
			SHARED_PREFERENCES.edit()
					.putBoolean(Constant.IS_AUTO_CLEAN, autoClean)
					.apply()
		}

	var exportDir: Int
		get() = SHARED_PREFERENCES.getInt(Constant.TEMP_DIR, JanYoFileUtil.Export.EXPORT_DIR_SDCARD_DATA)
		set(tempDir) {
			SHARED_PREFERENCES.edit()
					.putInt(Constant.TEMP_DIR, tempDir)
					.apply()
		}

	var customExportDir: String
		get() = SHARED_PREFERENCES.getString(Constant.CUSTOM_TEMP_DIR, Environment.getExternalStorageDirectory().absolutePath)!!
		set(customExportDir) {
			SHARED_PREFERENCES.edit()
					.putString(Constant.CUSTOM_TEMP_DIR, customExportDir)
					.apply()
		}

	var isCustomFormat: Boolean
		get() = SHARED_PREFERENCES.getBoolean(Constant.IS_CUSTOM_FORMAT, false)
		set(isCustomFormat) {
			val editor = SHARED_PREFERENCES.edit()
			if (!isCustomFormat)
				editor.remove(Constant.RENAME_FORMAT)
			editor.putBoolean(Constant.IS_CUSTOM_FORMAT, isCustomFormat)
			editor.apply()
		}

	//默认格式：名称-版本名称
	var renameFormat: String
		get() = SHARED_PREFERENCES.getString(Constant.RENAME_FORMAT, "%N-%V")!!
		set(renameFormat) {
			SHARED_PREFERENCES.edit()
					.putString(Constant.RENAME_FORMAT, renameFormat)
					.apply()
		}

	var nickName: String
		get() = SHARED_PREFERENCES.getString(Constant.NICK_NAME, "janyo")!!
		set(nickName) {
			SHARED_PREFERENCES.edit()
					.putString(Constant.NICK_NAME, nickName)
					.apply()
		}

	var sortType: Int
		get() = SHARED_PREFERENCES.getInt(Constant.SORT_TYPE, AppManagerUtil.SortType.SORT_TYPE_NONE)
		set(sortType) {
			SHARED_PREFERENCES.edit()
					.putInt(Constant.SORT_TYPE, sortType)
					.apply()
		}

	var cacheExpirationTime: Float
		get() = SHARED_PREFERENCES.getLong(Constant.CACHE_EXPIRATION_TIME, 0).toFloat() / 1000f / 60f / 60f / 24f
		set(cacheExpirationTime) {
			if (cacheExpirationTime <= 0)
				SHARED_PREFERENCES.edit()
						.putLong(Constant.CACHE_EXPIRATION_TIME, 0)
						.apply()
			else
				SHARED_PREFERENCES.edit()
						.putLong(Constant.CACHE_EXPIRATION_TIME, (cacheExpirationTime * 24f * 60f * 60f * 1000f).toLong())
						.apply()
		}

	var longPressAction: Int
		get() = SHARED_PREFERENCES.getInt(Constant.LONG_PRESS_ACTION, 0)
		set(value) {
			SHARED_PREFERENCES.edit()
					.putInt(Constant.LONG_PRESS_ACTION, value)
					.apply()
		}

	var saveTimeZone: Calendar
		get() {
			val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
			val calendar = Calendar.getInstance()
			val timeString = SHARED_PREFERENCES.getString(Constant.SAVE_TIME_ZONE, "")
			if (timeString != "")
				calendar.time = simpleDateFormat.parse(timeString)
			return calendar
		}
		set(value) {
			val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
			SHARED_PREFERENCES.edit()
					.putString(Constant.SAVE_TIME_ZONE, simpleDateFormat.format(value.time))
					.apply()
		}
}
