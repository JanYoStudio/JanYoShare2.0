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

package pw.janyo.janyoshare.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.activity.DirManagerActivity
import pw.janyo.janyoshare.activity.SettingsActivity
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings

class SettingsPreferenceFragment : PreferenceFragment() {
	private var tempExportDir = -1

	private var coordinatorLayout: CoordinatorLayout? = null

	private var isAutoCleanPreference: SwitchPreference? = null
	private var cacheExpirationTimePreference: EditTextPreference? = null
	private var exportDirPreference: Preference? = null
	private var customExportDirPreference: Preference? = null
	private var isCustomFormatPreference: SwitchPreference? = null
	private var customRenameFormatPreference: Preference? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		addPreferencesFromResource(R.xml.preference)
		coordinatorLayout = (activity as SettingsActivity).coordinatorLayout
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		bindView()
		initialization()
		monitor()
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	private fun bindView() {
		isAutoCleanPreference = findPreferenceById(R.string.key_auto_clean) as SwitchPreference
		cacheExpirationTimePreference = findPreferenceById(R.string.key_cache_expiration_time) as EditTextPreference
		exportDirPreference = findPreferenceById(R.string.key_export_dir)
		customExportDirPreference = findPreferenceById(R.string.key_custom_export_dir)
		isCustomFormatPreference = findPreferenceById(R.string.key_custom_format) as SwitchPreference
		customRenameFormatPreference = findPreferenceById(R.string.key_custom_rename_format)
	}

	private fun initialization() {
		isAutoCleanPreference!!.isChecked = Settings.isAutoClean
		if (Settings.isAutoClean)
			isAutoCleanPreference!!.setSummary(R.string.summary_auto_clean_on)
		else
			isAutoCleanPreference!!.setSummary(R.string.summary_auto_clean_off)
		val expirationTime = Settings.cacheExpirationTime
		if (expirationTime <= 0)
			cacheExpirationTimePreference!!.setSummary(R.string.summary_cache_expiration_time_no)
		else if (expirationTime == 1f)
			cacheExpirationTimePreference!!.setSummary(R.string.summary_cache_expiration_time_one)
		else
			cacheExpirationTimePreference!!.summary = getString(R.string.summary_cache_expiration_time, expirationTime)
		exportDirPreference!!.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
		customExportDirPreference!!.isEnabled = Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM
		isCustomFormatPreference!!.isChecked = Settings.isCustomFormat
		if (Settings.isCustomFormat) {
			val test = InstallAPP()
			test.name = getString(R.string.app_name)
			test.versionName = getString(R.string.app_version_name)
			test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
			test.packageName = getString(R.string.app_package_name)
			customRenameFormatPreference!!.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
			customRenameFormatPreference!!.isEnabled = true
		} else {
			customRenameFormatPreference!!.summary = null
			customRenameFormatPreference!!.isEnabled = false
		}
	}

	private fun monitor() {
		isAutoCleanPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			val isAutoClean = !isAutoCleanPreference!!.isChecked
			if (isAutoClean)
				AlertDialog.Builder(activity)
						.setTitle(" ")
						.setMessage(R.string.hint_warning_auto_clean)
						.setPositiveButton(R.string.action_open) { dialog, which -> Settings.isAutoClean = true }
						.setNegativeButton(android.R.string.cancel) { dialog, which ->
							isAutoCleanPreference!!.isChecked = false
							Settings.isAutoClean = false
						}
						.setOnDismissListener {
							isAutoCleanPreference!!.isChecked = Settings.isAutoClean
							if (Settings.isAutoClean)
								isAutoCleanPreference!!.setSummary(R.string.summary_auto_clean_on)
							else
								isAutoCleanPreference!!.setSummary(R.string.summary_auto_clean_off)
						}
						.show()
			else {
				Settings.isAutoClean = false
				isAutoCleanPreference!!.setSummary(R.string.summary_auto_clean_off)
			}
			true
		}
		cacheExpirationTimePreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			val value = java.lang.Float.valueOf(newValue.toString())!!
			Settings.cacheExpirationTime = value
			val expirationTime = Settings.cacheExpirationTime
			if (expirationTime <= 0)
				cacheExpirationTimePreference!!.setSummary(R.string.summary_cache_expiration_time_no)
			else if (expirationTime == 1f)
				cacheExpirationTimePreference!!.setSummary(R.string.summary_cache_expiration_time_one)
			else
				cacheExpirationTimePreference!!.summary = getString(R.string.summary_cache_expiration_time, expirationTime)
			true
		}
		exportDirPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			tempExportDir = Settings.exportDir
			val dialog = AlertDialog.Builder(activity)
					.setTitle(R.string.title_export_dir)
					.setSingleChoiceItems(R.array.exportDir, tempExportDir) { dialog, which -> tempExportDir = which }
					.setPositiveButton(android.R.string.ok, null)
					.create()
			dialog.show()
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
				if (tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_SDCARD || tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
						requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), CODE_PERMISSION_REQUEST)
						return@OnClickListener
					}
				Settings.exportDir = tempExportDir
				customExportDirPreference!!.isEnabled = tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM
				exportDirPreference!!.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
				dialog.dismiss()
			})
			true
		}
		customExportDirPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			startActivityForResult(Intent(activity, DirManagerActivity::class.java), CODE_SET_EXPORT_DIR)
			true
		}
		isCustomFormatPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			val isCustomFormat = !isCustomFormatPreference!!.isChecked
			if (isCustomFormat) {
				val test = InstallAPP()
				test.name = getString(R.string.app_name)
				test.versionName = getString(R.string.app_version_name)
				test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
				test.packageName = getString(R.string.app_package_name)
				customRenameFormatPreference!!.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
				customRenameFormatPreference!!.isEnabled = true
			} else {
				customRenameFormatPreference!!.summary = null
				customRenameFormatPreference!!.isEnabled = false
			}
			Settings.isCustomFormat = isCustomFormat
			true
		}
		customRenameFormatPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val view = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_rename_format, LinearLayout(activity), false)
			val textInputLayout = view.findViewById<TextInputLayout>(R.id.textInputLayout)
			val showText = view.findViewById<TextView>(R.id.show)

			textInputLayout.editText!!.setText(Settings.renameFormat)
			val test = InstallAPP()
			test.name = getString(R.string.app_name)
			test.versionName = getString(R.string.app_version_name)
			test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
			test.packageName = getString(R.string.app_package_name)
			showText.text = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
			textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

				override fun afterTextChanged(s: Editable) {
					val format = s.toString()
					showText.text = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, format))
				}
			})
			val dialog = AlertDialog.Builder(activity)
					.setTitle(R.string.title_custom_rename_format)
					.setView(view)
					.setPositiveButton(android.R.string.ok, null)
					.setNegativeButton(android.R.string.cancel, null)
					.setNeutralButton(R.string.action_insert, null)
					.create()
			dialog.show()
			if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null)
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
					val format = textInputLayout.editText!!.text.toString()
					if (format.isEmpty()) {
						Toast.makeText(activity, R.string.hint_custom_rename_format_empty, Toast.LENGTH_SHORT)
								.show()
						return@OnClickListener
					}
					Settings.renameFormat = format
					customRenameFormatPreference!!.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
					dialog.dismiss()
				})
			if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null)
				dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
					val temp = textInputLayout.editText!!.text.toString() + '%'
					textInputLayout.editText!!.setText(temp)
					textInputLayout.editText!!.setSelection(temp.length)
				}
			true
		}
	}

	private fun findPreferenceById(@StringRes id: Int): Preference {
		return findPreference(getString(id))
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == CODE_PERMISSION_REQUEST)
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
				Toast.makeText(activity, R.string.hint_permission_write_external, Toast.LENGTH_LONG)
						.show()
		exportDirPreference!!.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == CODE_SET_EXPORT_DIR)
			exportDirPreference!!.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
	}

	companion object {
		private const val CODE_PERMISSION_REQUEST = 23
		private const val CODE_SET_EXPORT_DIR = 24
	}
}
