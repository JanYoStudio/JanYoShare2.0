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

package pw.janyo.janyoshare.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.base.BasePreferenceFragment
import pw.janyo.janyoshare.ui.activity.DirManagerActivity
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.databinding.DialogCustomRenameFormatBinding
import pw.janyo.janyoshare.databinding.DialogViewEdittextBinding
import pw.janyo.janyoshare.utils.DecimalInputTextWatcher
import pw.janyo.janyoshare.utils.JanYoFileUtil
import pw.janyo.janyoshare.utils.Settings

class SettingsPreferenceFragment : BasePreferenceFragment(R.xml.preference) {
	private var tempExportDir = -1

	private lateinit var isAutoCleanPreference: SwitchPreference
	private lateinit var cacheExpirationTimePreference: Preference
	private lateinit var exportDirPreference: Preference
	private lateinit var customExportDirPreference: Preference
	private lateinit var isCustomFormatPreference: SwitchPreference
	private lateinit var customRenameFormatPreference: Preference
	private lateinit var longPressActionPreference: Preference

	override fun initPreference() {
		super.initPreference()
		bindView()
		initialization()
	}

	private fun bindView() {
		isAutoCleanPreference = findPreferenceById(R.string.key_auto_clean)
		cacheExpirationTimePreference = findPreferenceById(R.string.key_cache_expiration_time)
		exportDirPreference = findPreferenceById(R.string.key_export_dir)
		customExportDirPreference = findPreferenceById(R.string.key_custom_export_dir)
		isCustomFormatPreference = findPreferenceById(R.string.key_custom_format)
		customRenameFormatPreference = findPreferenceById(R.string.key_custom_rename_format)
		longPressActionPreference = findPreferenceById(R.string.key_long_press_action)
	}

	private fun initialization() {
		isAutoCleanPreference.isChecked = Settings.isAutoClean
		if (Settings.isAutoClean)
			isAutoCleanPreference.setSummary(R.string.summary_auto_clean_on)
		else
			isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off)
		val expirationTime = Settings.cacheExpirationTime
		when {
			expirationTime <= 0 -> cacheExpirationTimePreference.setSummary(R.string.summary_cache_expiration_time_no)
			expirationTime == 1f -> cacheExpirationTimePreference.setSummary(R.string.summary_cache_expiration_time_one)
			else -> cacheExpirationTimePreference.summary = getString(R.string.summary_cache_expiration_time, expirationTime)
		}
		exportDirPreference.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
		customExportDirPreference.isEnabled = Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM
		isCustomFormatPreference.isChecked = Settings.isCustomFormat
		if (Settings.isCustomFormat) {
			val test = InstallAPP()
			test.name = getString(R.string.app_name)
			test.versionName = getString(R.string.app_version_name)
			test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
			test.packageName = getString(R.string.app_package_name)
			customRenameFormatPreference.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
			customRenameFormatPreference.isEnabled = true
		} else {
			customRenameFormatPreference.summary = null
			customRenameFormatPreference.isEnabled = false
		}
		setLongPressActionSummary()
	}


	override fun monitor() {
		isAutoCleanPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			val isAutoClean = !isAutoCleanPreference.isChecked
			if (isAutoClean)
				AlertDialog.Builder(activity!!)
						.setTitle(" ")
						.setMessage(R.string.hint_warning_auto_clean)
						.setPositiveButton(R.string.action_open) { _, _ -> Settings.isAutoClean = true }
						.setNegativeButton(android.R.string.cancel) { _, _ ->
							isAutoCleanPreference.isChecked = false
							Settings.isAutoClean = false
						}
						.setOnDismissListener {
							isAutoCleanPreference.isChecked = Settings.isAutoClean
							if (Settings.isAutoClean)
								isAutoCleanPreference.setSummary(R.string.summary_auto_clean_on)
							else
								isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off)
						}
						.show()
			else {
				Settings.isAutoClean = false
				isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off)
			}
			true
		}
		cacheExpirationTimePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
			AlertDialog.Builder(activity!!)
					.setTitle(R.string.title_dialog_set_cache_expiration_time)
					.setItems(R.array.cacheExpirationTime) { _, which ->
						when (which) {
							0 -> Settings.cacheExpirationTime = 0.5f
							1 -> Settings.cacheExpirationTime = 1f
							2 -> Settings.cacheExpirationTime = 3f
							3 -> Settings.cacheExpirationTime = 7f
							4 -> Settings.cacheExpirationTime = 30f
							5 -> {
								val binding = DialogViewEdittextBinding.inflate(LayoutInflater.from(activity))
								binding.cacheExpirationTime = Settings.cacheExpirationTime.toString()
								binding.editText.addTextChangedListener(DecimalInputTextWatcher(binding.editText))
								AlertDialog.Builder(activity!!)
										.setTitle(R.string.title_dialog_set_cache_expiration_time_custom)
										.setView(binding.root)
										.setPositiveButton(android.R.string.ok) { _, _ ->
											Settings.cacheExpirationTime = binding.editText.text.toString().toFloat()
										}
										.setNegativeButton(android.R.string.cancel, null)
										.setOnDismissListener {
											setCacheExpirationTimeSummary()
										}
										.show()
							}
						}
						setCacheExpirationTimeSummary()
					}
					.show()
			true
		}
		exportDirPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
			tempExportDir = Settings.exportDir
			val dialog = AlertDialog.Builder(activity!!)
					.setTitle(R.string.title_export_dir)
					.setSingleChoiceItems(R.array.exportDir, tempExportDir) { _, which -> tempExportDir = which }
					.setPositiveButton(android.R.string.ok, null)
					.create()
			dialog.show()
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
				if (tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_SDCARD || tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
						requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), CODE_PERMISSION_REQUEST)
						return@OnClickListener
					}
				Settings.exportDir = tempExportDir
				customExportDirPreference.isEnabled = tempExportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM
				exportDirPreference.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
				dialog.dismiss()
			})
			true
		}
		customExportDirPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			startActivityForResult(Intent(activity, DirManagerActivity::class.java), CODE_SET_EXPORT_DIR)
			true
		}
		isCustomFormatPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
			val isCustomFormat = !isCustomFormatPreference.isChecked
			if (isCustomFormat) {
				val test = InstallAPP()
				test.name = getString(R.string.app_name)
				test.versionName = getString(R.string.app_version_name)
				test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
				test.packageName = getString(R.string.app_package_name)
				customRenameFormatPreference.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
				customRenameFormatPreference.isEnabled = true
			} else {
				customRenameFormatPreference.summary = null
				customRenameFormatPreference.isEnabled = false
			}
			Settings.isCustomFormat = isCustomFormat
			true
		}
		customRenameFormatPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
			val binding = DialogCustomRenameFormatBinding.inflate(LayoutInflater.from(activity))
			binding.renameFormat = Settings.renameFormat
			val test = InstallAPP()
			test.name = getString(R.string.app_name)
			test.versionName = getString(R.string.app_version_name)
			test.versionCode = Integer.parseInt(getString(R.string.app_version_code))
			test.packageName = getString(R.string.app_package_name)
			binding.show.text = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
			binding.textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

				override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

				override fun afterTextChanged(s: Editable) {
					val format = s.toString()
					binding.show.text = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, format))
				}
			})
			val dialog = AlertDialog.Builder(activity!!)
					.setTitle(R.string.title_custom_rename_format)
					.setView(binding.root)
					.setPositiveButton(android.R.string.ok, null)
					.setNegativeButton(android.R.string.cancel, null)
					.setNeutralButton(R.string.action_insert, null)
					.create()
			dialog.show()
			if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null)
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
					val format = binding.textInputLayout.editText!!.text.toString()
					if (format.isEmpty()) {
						toastMessage(R.string.hint_custom_rename_format_empty)
						return@OnClickListener
					}
					Settings.renameFormat = format
					customRenameFormatPreference.summary = getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.renameFormat))
					dialog.dismiss()
				})
			if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null)
				dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
					val temp = binding.textInputLayout.editText!!.text.toString() + '%'
					binding.textInputLayout.editText!!.setText(temp)
					binding.textInputLayout.editText!!.setSelection(temp.length)
				}
			true
		}
		longPressActionPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			var select = Settings.longPressAction
			val array = resources.getStringArray(R.array.doOperationLongPress)
			if (select < 0 || select >= array.size) {
				select = 0
				Settings.longPressAction = 0
			}
			AlertDialog.Builder(activity!!)
					.setTitle(R.string.title_dialog_set_long_press_action)
					.setSingleChoiceItems(R.array.doOperationLongPress, select) { _, which ->
						select = which
					}
					.setPositiveButton(android.R.string.ok) { _, _ ->
						Settings.longPressAction = select
						setLongPressActionSummary()
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
			true
		}
	}

	private fun setCacheExpirationTimeSummary() {
		val expirationTime = Settings.cacheExpirationTime
		when {
			expirationTime <= 0 -> cacheExpirationTimePreference.setSummary(R.string.summary_cache_expiration_time_no)
			expirationTime == 1f -> cacheExpirationTimePreference.setSummary(R.string.summary_cache_expiration_time_one)
			else -> cacheExpirationTimePreference.summary = getString(R.string.summary_cache_expiration_time, expirationTime)
		}
	}

	private fun setLongPressActionSummary() {
		val longPressAction = Settings.longPressAction
		val array = resources.getStringArray(R.array.doOperationLongPress)
		if (longPressAction >= 0 && longPressAction < array.size)
			longPressActionPreference.summary = array[longPressAction]
		else {
			longPressActionPreference.summary = array[0]
			Settings.longPressAction = 0
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == CODE_PERMISSION_REQUEST)
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
				toastMessage(R.string.hint_permission_write_external, true)
		exportDirPreference.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == CODE_SET_EXPORT_DIR)
			exportDirPreference.summary = getString(R.string.summary_export_dir, JanYoFileUtil.exportDirPath)
	}

	companion object {
		private const val CODE_PERMISSION_REQUEST = 23
		private const val CODE_SET_EXPORT_DIR = 24
	}
}
