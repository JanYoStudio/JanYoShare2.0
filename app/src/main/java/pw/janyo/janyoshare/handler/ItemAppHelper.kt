/*
 * Created by Mystery0 on 6/14/18 5:25 PM.
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
 * Last modified 6/14/18 5:25 PM
 */

package pw.janyo.janyoshare.handler

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.ImageView
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.ui.adapter.AppAdapter
import pw.janyo.janyoshare.ui.fragment.AppFragment
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.utils.JanYoFileUtil
import pw.janyo.janyoshare.utils.Settings
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver
import vip.mystery0.tools.utils.CommandTools
import vip.mystery0.tools.utils.IntentTools
import java.io.File
import android.content.Intent
import android.net.Uri

class ItemAppHelper(private val coordinatorLayout: CoordinatorLayout,
					private val context: Context,
					private val appAdapter: AppAdapter,
					private val appFragment: AppFragment,
					private val mainViewModel: MainViewModel) {

	private val loadingDialog: ZLoadingDialog = ZLoadingDialog(context)
			.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)
			.setHintTextSize(16f)
			.setCancelable(false)
			.setCanceledOnTouchOutside(false)
			.setLoadingColor(ContextCompat.getColor(context, R.color.colorAccent))
			.setHintTextColor(ContextCompat.getColor(context, R.color.colorAccent))

	private var checkBoxAnimator: Animator? = null
	private var imageViewAnimator: Animator? = null

	/**
	 * item单击事件
	 *
	 * @param data 点击的item对应的InstallAPP对象
	 */
	fun click(data: InstallAPP) {
		if (mainViewModel.selectedList.isNotEmpty())
			clickWhenSelecting()
		else
			clickSingle(data)
	}

	/**
	 * item长按事件
	 *
	 * @param data 长按的item对应的InstallAPP对象
	 */
	fun longClick(data: InstallAPP) {
		if (mainViewModel.selectedList.isNotEmpty())
			return
		when (Settings.longPressAction) {
			0//仅导出
			-> export(data, 0)
			1//提取并分享
			-> export(data, 1, 0)
			2//重命名并分享
			-> export(data, 1, 1)
			3//重命名扩展名
			-> export(data, 1, 2)
			4//和数据包一起提取分享
			-> export(data, 1, 3)
			5//面对面分享
			-> export(data, 1, 4)
			6//拷贝名称
			-> copyInfoToClipboard(data, 0)
			7//拷贝包名
			-> copyInfoToClipboard(data, 1)
			8//拷贝版本名称
			-> copyInfoToClipboard(data, 2)
			9//拷贝版本号
			-> copyInfoToClipboard(data, 3)
			10//普通卸载
			-> AppManagerUtil.uninstallAPP(context, data)
			11//root卸载
			-> showAlert(true, false, arrayListOf(data))
			12//冻结
			-> showAlert(false, true, arrayListOf(data))
			13//解除冻结
			-> showAlert(false, false, arrayListOf(data))
			14//跳转应用详情页面
			-> linkToDetailSetting(data)
		}
	}

	private fun clickSingle(data: InstallAPP) {
		AlertDialog.Builder(context)
				.setTitle(R.string.title_dialog_select_operation)
				.setItems(
						if (data.isDisable)
							R.array.doOperationDisable
						else
							R.array.doOperationEnable
				) { _, which ->
					when (which) {
						0, 1 -> export(data, which)
						2 -> copyInfoToClipboard(data)
						3 -> selectUninstallType(data)
						4 -> showAlert(false, !data.isDisable, arrayListOf(data))
					}
				}
				.show()
	}

	private fun clickWhenSelecting() {
		AlertDialog.Builder(context)
				.setTitle(R.string.title_dialog_select_operation)
				.setItems(R.array.doOperationSelected) { _, which ->
					when (which) {
						0//仅导出
						-> exportMany(mainViewModel.selectedList, 0)
						1//导出并分享
						-> exportMany(mainViewModel.selectedList, 1)
						2//和数据包一起分享
						-> exportMany(mainViewModel.selectedList, 2)
						3//面对面分享
						-> exportMany(mainViewModel.selectedList, 3)
						4//正常卸载
						-> mainViewModel.selectedList.forEach {
							AppManagerUtil.uninstallAPP(context, it)
						}
						5//使用Root卸载
						-> showAlert(true, false, mainViewModel.selectedList)
						6//冻结
						-> showAlert(false, true, mainViewModel.selectedList)
						7//解除冻结
						-> showAlert(false, false, mainViewModel.selectedList)
					}
				}
				.show()
	}

	fun showAnimation(checkBox: CheckBox, imageView: ImageView, isMark: Boolean) {
		checkBoxAnimator?.cancel()
		imageViewAnimator?.cancel()
		checkBoxAnimator = ObjectAnimator.ofFloat(checkBox, "alpha", if (isMark) 0f else 1f, if (isMark) 1f else 0f)
		imageViewAnimator = ObjectAnimator.ofFloat(imageView, "alpha", if (isMark) 1f else 0f, if (isMark) 0f else 1f)
		checkBoxAnimator?.start()
		imageViewAnimator?.start()
	}

	/**
	 * 导出当前应用并执行后续操作
	 *
	 * @param installAPP  需要导出的APP
	 * @param choose      选择的操作
	 * @param doAction    后续执行的操作
	 */
	private fun export(installAPP: InstallAPP, choose: Int, doAction: Int = -1) {
		loadingDialog.show()
		RxObservable<Int>()
				.doThings {
					it.onFinish(JanYoFileUtil.exportAPK(installAPP))
				}
				.subscribe(object : RxObserver<Int>() {
					override fun onError(e: Throwable) {
						loadingDialog.dismiss()
						Logs.wtfm("doOperation: onError: ", e)
					}

					override fun onFinish(data: Int?) {
						loadingDialog.dismiss()
						when (data) {
							JanYoFileUtil.Code.DIR_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.FILE_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_export_failed, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.DONE ->
								when {
									choose == 0 ->
										Snackbar.make(coordinatorLayout, context.getString(R.string.hint_export_done, JanYoFileUtil.exportDirPath), Snackbar.LENGTH_SHORT)
												.show()
									choose != 0 && doAction == -1 ->
										AlertDialog.Builder(context)
												.setTitle(R.string.title_dialog_select_share_method)
												.setItems(R.array.shareMethod) { _, which ->
													doSomething(installAPP, which)
												}
												.show()
									choose != 0 && doAction != -1 -> doSomething(installAPP, doAction)
								}
						}
					}
				})
	}

	private fun exportMany(appList: ArrayList<InstallAPP>, doAction: Int) {
		val fileList = ArrayList<File>()
		RxObservable<Map<String, Int>>()
				.doThings { emitter ->
					val map = HashMap<String, Int>()
					var num = 0
					var obbNum = 0
					appList.forEach {
						if (JanYoFileUtil.exportAPK(it) == JanYoFileUtil.Code.DONE) {
							fileList.add(JanYoFileUtil.getExportFile(it))
							if (doAction == 2) {
								val obbList = JanYoFileUtil.checkObb(it.packageName)
								if (obbList.isNotEmpty()) {
									fileList.addAll(obbList)
									obbNum += obbList.size
								}
							}
							num++
						}
					}
					map["file"] = num
					map["obb"] = obbNum
					emitter.onFinish(map)
				}
				.subscribe(object : RxObserver<Map<String, Int>>() {
					override fun onFinish(data: Map<String, Int>?) {
						loadingDialog.dismiss()
						if (data == null)
							return
						when (doAction) {
							0 ->//仅导出
							{
								val fileNum = if (data.containsKey("file")) data["file"] else -1
								if (fileNum == -1)
									Snackbar.make(coordinatorLayout, R.string.hint_export_failed, Snackbar.LENGTH_LONG)
								else
									Snackbar.make(coordinatorLayout,
											if (fileNum == 1)
												context.getString(R.string.hint_export_finish, fileNum)
											else
												context.getString(R.string.hint_export_finish_s, fileNum)
											, Snackbar.LENGTH_LONG)
											.show()
							}
							1 ->//导出并分享
							{
								val fileNum = if (data.containsKey("file")) data["file"] else 0
								Snackbar.make(coordinatorLayout,
										if (fileNum == 1)
											context.getString(R.string.hint_export_finish, fileNum)
										else
											context.getString(R.string.hint_export_finish_s, fileNum)
										, Snackbar.LENGTH_LONG)
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												IntentTools.shareMultFile(context, context.getString(R.string.title_activity_share), fileList, context.getString(R.string.authorities))
											}
										})
										.show()
							}
							2 ->//和数据一起分享
							{
								val fileNum = if (data.containsKey("file")) data["file"] else 0
								val obbNum = if (data.containsKey("obb")) data["obb"] else 0
								Snackbar.make(coordinatorLayout,
										when {
											fileNum == 1 && obbNum == 1 -> context.getString(R.string.hint_export_with_data_finish___, fileNum, obbNum)
											fileNum == 1 && obbNum != 1 -> context.getString(R.string.hint_export_with_data_finish__s, fileNum, obbNum)
											fileNum != 1 && obbNum == 1 -> context.getString(R.string.hint_export_with_data_finish_s_, fileNum, obbNum)
											fileNum != 1 && obbNum != 1 -> context.getString(R.string.hint_export_with_data_finish_ss, fileNum, obbNum)
											else -> context.getString(R.string.hint_export_with_data_finish_ss, fileNum, obbNum)
										}, Snackbar.LENGTH_LONG)
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												IntentTools.shareMultFile(context, context.getString(R.string.title_activity_share), fileList, context.getString(R.string.authorities))
											}
										})
										.show()
							}
							3 ->//面对面分享
								Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
										.show()
						}
					}

					override fun onError(e: Throwable) {
						loadingDialog.dismiss()
						Logs.wtfm("doOperation: onError: ", e)
					}
				})
	}

	/**
	 * 导出后执行的后续操作
	 *
	 * @param installAPP    导出的APP
	 * @param whatToDo      执行的操作
	 */
	private fun doSomething(installAPP: InstallAPP, whatToDo: Int) {
		when (whatToDo) {
			0//提取并分享
			-> IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getExportFile(installAPP), context.getString(R.string.authorities))
			1//重命名后分享
			-> {
				val oldFileName = JanYoFileUtil.formatName(installAPP, Settings.renameFormat)
				val renameFileNameView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, TextInputLayout(context), false)
				val renameFileNameTextInputLayout = renameFileNameView.findViewById<TextInputLayout>(R.id.layout)
				renameFileNameTextInputLayout.hint = oldFileName

				renameFileNameTextInputLayout.editText!!.setText(oldFileName)
				AlertDialog.Builder(context)
						.setTitle(" ")
						.setView(renameFileNameView)
						.setPositiveButton(android.R.string.ok) { _, _ ->
							val newName = renameFileNameTextInputLayout.editText!!.text.toString()
							val code = JanYoFileUtil.renameFile(installAPP, newName, false)
							when (code) {
								JanYoFileUtil.Code.FILE_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.FILE_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_file_exist, Snackbar.LENGTH_LONG)
										.setAction(R.string.action_redo) {
											if (JanYoFileUtil.renameFile(installAPP, newName, true) == JanYoFileUtil.Code.DONE)
												IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)), context.getString(R.string.authorities))
											else
												Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
														.show()
										}
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
													IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)), context.getString(R.string.authorities))
											}
										})
										.show()
								JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.DONE -> IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)), context.getString(R.string.authorities))
							}
						}
						.show()
			}
			2//重命名扩展名
			-> {
				val oldExtensionName = JanYoFileUtil.getExtensionFileName(installAPP.sourceDir)
				val renameExtensionView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, TextInputLayout(context), false)
				val renameExtensionTextInputLayout = renameExtensionView.findViewById<TextInputLayout>(R.id.layout)
				renameExtensionTextInputLayout.hint = oldExtensionName
				renameExtensionTextInputLayout.editText!!.setText(oldExtensionName)
				AlertDialog.Builder(context)
						.setTitle(" ")
						.setView(renameExtensionView)
						.setPositiveButton(android.R.string.ok) { _, _ ->
							val newExtensionName = renameExtensionTextInputLayout.editText!!.text.toString()
							val code = JanYoFileUtil.renameExtension(installAPP, newExtensionName, false)
							when (code) {
								JanYoFileUtil.Code.FILE_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.FILE_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_file_exist, Snackbar.LENGTH_LONG)
										.setAction(R.string.action_redo) {
											if (JanYoFileUtil.renameExtension(installAPP, newExtensionName, true) == JanYoFileUtil.Code.DONE)
												IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"), context.getString(R.string.authorities))
											else
												Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
														.show()
										}
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
													IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"), context.getString(R.string.authorities))
											}
										})
										.show()
								JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.DONE -> IntentTools.shareFile(context, context.getString(R.string.title_activity_share_app, installAPP.name), JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"), context.getString(R.string.authorities))
							}
						}
						.show()
			}
			3//和数据包一起提取分享
			-> {
				val shareList = ArrayList<File>()
				shareList.add(JanYoFileUtil.getExportFile(installAPP))
				val obbList = JanYoFileUtil.checkObb(installAPP.packageName)
				shareList.addAll(obbList)
				when {
					obbList.isEmpty() -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb_no), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									IntentTools.shareMultFile(context, context.getString(R.string.title_activity_share), shareList, context.getString(R.string.authorities))
								}
							})
							.show()
					obbList.size == 1 -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb, obbList.size), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									IntentTools.shareMultFile(context, context.getString(R.string.title_activity_share), shareList, context.getString(R.string.authorities))
								}
							})
							.show()
					else -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb_s, obbList.size), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									IntentTools.shareMultFile(context, context.getString(R.string.title_activity_share), shareList, context.getString(R.string.authorities))
								}
							})
							.show()
				}
			}
			4//面对面分享
			-> Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
					.show()
		}
	}

	/**
	 * 拷贝信息到剪切板
	 *
	 * @param installAPP    需要拷贝信息的APP
	 * @param doAction      拷贝的具体操作
	 */
	private fun copyInfoToClipboard(installAPP: InstallAPP, doAction: Int = -1) {
		if (doAction != -1) {
			when (doAction) {
				0 -> copyToClipboard(installAPP.name, installAPP.name)
				1 -> copyToClipboard(installAPP.name, installAPP.packageName)
				2 -> copyToClipboard(installAPP.name, installAPP.versionName)
				3 -> copyToClipboard(installAPP.name, installAPP.versionCode.toString())
			}
		} else
			AlertDialog.Builder(context)
					.setTitle(R.string.title_dialog_select_copy_info)
					.setItems(R.array.applicationInfo) { _, which ->
						when (which) {
							0 -> copyToClipboard(installAPP.name, installAPP.name)
							1 -> copyToClipboard(installAPP.name, installAPP.packageName)
							2 -> copyToClipboard(installAPP.name, installAPP.versionName)
							3 -> copyToClipboard(installAPP.name, installAPP.versionCode.toString())
							4 -> linkToDetailSetting(installAPP)
						}
					}
					.show()
	}

	private fun linkToDetailSetting(installAPP: InstallAPP) {
		val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
		intent.data = Uri.fromParts("package", installAPP.packageName, null)
		context.startActivity(intent)
	}

	/**
	 * 拷贝具体信息到剪切板
	 *
	 * @param label 标记
	 * @param text  信息
	 */
	private fun copyToClipboard(label: String?, text: String?) {
		val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		clipboardManager.primaryClip = ClipData.newPlainText(label, text)
		Snackbar.make(coordinatorLayout, R.string.hint_copy_info_done, Snackbar.LENGTH_SHORT)
				.show()
	}

	/**
	 * 选择卸载的类型
	 *
	 * @param installAPP 需要卸载的APP
	 */
	private fun selectUninstallType(installAPP: InstallAPP) {
		AlertDialog.Builder(context)
				.setTitle(R.string.title_dialog_select_uninstall_type)
				.setItems(R.array.uninstallType) { _, which ->
					when (which) {
						0 -> AppManagerUtil.uninstallAPP(context, installAPP)
						1 -> showAlert(true, false, arrayListOf(installAPP))
					}
				}
				.show()
	}

	/**
	 * 显示警告信息
	 *
	 * @param isUninstall   是否是卸载
	 * @param isDisable     是否是冻结
	 * @param appList       需要执行操作的APP列表
	 */
	private fun showAlert(isUninstall: Boolean, isDisable: Boolean, appList: ArrayList<InstallAPP>) {
		RxObservable<Boolean>()
				.doThings {
					val result = CommandTools.execRootCommand("echo test")
					it.onFinish(result.isSuccess())
				}
				.subscribe(object : RxObserver<Boolean>() {
					override fun onFinish(data: Boolean?) {
						if (data == null)
							return
						if (!data) {
							Snackbar.make(coordinatorLayout, R.string.hint_no_su, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_request_again) {
										showAlert(isUninstall, isDisable, appList)
									}
									.show()
							return
						}
						val stringBuilder = StringBuilder()
						appList.forEach {
							stringBuilder.append(it.name)
									.append('(')
									.append(it.packageName)
									.append(')')
									.append('\n')
						}
						AlertDialog.Builder(context)
								.setTitle(when {
									isUninstall -> R.string.title_dialog_uninstall_alert
									!isUninstall && isDisable -> R.string.title_dialog_disable_alert
									else -> R.string.title_dialog_enable_alert
								})
								.setMessage(stringBuilder.toString())
								.setPositiveButton(android.R.string.ok) { _, _ ->
									when {
										isUninstall -> doUnInstall(appList)
										!isUninstall && isDisable -> doDisable(appList)
										else -> doEnable(appList)
									}
								}
								.setNegativeButton(android.R.string.cancel, null)
								.show()
					}

					override fun onError(e: Throwable) {
						Logs.wtfm("onError: request root", e)
					}
				})
	}

	/**
	 * 执行卸载操作（使用Root）
	 *
	 * @param appList 需要卸载的APP列表
	 */
	private fun doUnInstall(appList: ArrayList<InstallAPP>) =
			RxObservable<CommandTools.CommandResult>()
					.doThings {
						it.onFinish(AppManagerUtil.uninstallAPPByRoot(appList))
					}
					.subscribe(object : RxObserver<CommandTools.CommandResult>() {
						override fun onFinish(data: CommandTools.CommandResult?) {
							loadingDialog.dismiss()
							if (data == null)
								return
							if (data.isSuccess()) {
								appAdapter.removeList(appList)
								Snackbar.make(coordinatorLayout, R.string.hint_uninstall_finish, Snackbar.LENGTH_SHORT)
										.show()
							} else {
								Logs.wtf("onComplete: uninstall", data.toString())
								Snackbar.make(coordinatorLayout, R.string.hint_uninstall_error, Snackbar.LENGTH_LONG)
										.setAction(R.string.action_refresh_list) {
											appFragment.refreshList()
										}
										.show()
							}
						}

						override fun onError(e: Throwable) {
							loadingDialog.dismiss()
							Logs.wtfm("onError: ", e)
						}
					})


	/**
	 * 执行冻结操作（使用Root）
	 *
	 * @param appList 需要冻结的APP列表
	 */
	private fun doDisable(appList: ArrayList<InstallAPP>) {
		RxObservable<CommandTools.CommandResult>()
				.doThings {
					it.onFinish(AppManagerUtil.disableAPP(appList))
				}
				.subscribe(object : Observer<CommandTools.CommandResult> {
					private lateinit var result: CommandTools.CommandResult
					override fun onComplete() {
						loadingDialog.dismiss()
						if (result.isSuccess()) {
							appList.forEach {
								it.isDisable = true
							}
							appAdapter.updateList(appList)
							Snackbar.make(coordinatorLayout, R.string.hint_disable_finish, Snackbar.LENGTH_SHORT)
									.show()
						} else {
							Logs.wtf("onComplete: disable", result.toString())
							Snackbar.make(coordinatorLayout, R.string.hint_disable_error, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_refresh_list) {
										appFragment.refreshList()
									}
									.show()
						}
					}

					override fun onSubscribe(d: Disposable) {
						loadingDialog.show()
					}

					override fun onNext(t: CommandTools.CommandResult) {
						result = t
					}

					override fun onError(e: Throwable) {
						loadingDialog.dismiss()
					}
				})
	}


	/**
	 * 执行解除冻结操作（使用Root）
	 *
	 * @param appList 需要解除冻结的APP列表
	 */
	private fun doEnable(appList: ArrayList<InstallAPP>) {
		RxObservable<CommandTools.CommandResult>()
				.doThings {
					it.onFinish(AppManagerUtil.enableAPP(appList))
				}
				.subscribe(object : Observer<CommandTools.CommandResult> {
					private lateinit var result: CommandTools.CommandResult
					override fun onComplete() {
						loadingDialog.dismiss()
						if (result.isSuccess()) {
							appList.forEach {
								it.isDisable = false
							}
							appAdapter.updateList(appList)
							Snackbar.make(coordinatorLayout, R.string.hint_enable_finish, Snackbar.LENGTH_SHORT)
									.show()
						} else {
							Logs.wtf("onComplete: enable", result.toString())
							Snackbar.make(coordinatorLayout, R.string.hint_enable_error, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_refresh_list) {
										appFragment.refreshList()
									}
									.show()
						}
					}

					override fun onSubscribe(d: Disposable) {
						loadingDialog.show()
					}

					override fun onNext(t: CommandTools.CommandResult) {
						result = t
					}

					override fun onError(e: Throwable) {
						loadingDialog.dismiss()
					}
				})
	}
}