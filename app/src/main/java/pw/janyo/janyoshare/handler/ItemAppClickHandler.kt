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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.fragment.AppFragment
import pw.janyo.janyoshare.util.AppManager
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.logs.Logs
import vip.mystery0.tools.utils.CommandTools
import java.io.File

class ItemAppClickHandler(val coordinatorLayout: CoordinatorLayout,
						  val context: Context,
						  val fragment: AppFragment,
						  val list: ArrayList<InstallAPP>) {
	private val exportDialog: ZLoadingDialog = ZLoadingDialog(context)
			.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)
			.setHintTextSize(16f)
			.setCancelable(false)
			.setCanceledOnTouchOutside(false)
			.setLoadingColor(ContextCompat.getColor(context, R.color.colorAccent))
			.setHintTextColor(ContextCompat.getColor(context, R.color.colorAccent))

	fun click(data: InstallAPP) {
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

	private fun export(installAPP: InstallAPP, choose: Int) {
		Observable.create(ObservableOnSubscribe<Int> { subscriber ->
			subscriber.onNext(JanYoFileUtil.exportAPK(installAPP))
			subscriber.onComplete()
		})
				.subscribeOn(Schedulers.io())
				.unsubscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<Int> {
					private var code: Int = 0

					override fun onSubscribe(d: Disposable) {
						Logs.i("onSubscribe: ")
						exportDialog.show()
					}

					override fun onNext(integer: Int) {
						code = integer
					}

					override fun onError(e: Throwable) {
						exportDialog.dismiss()
						Log.wtf("doOperation: onError: ", e)
					}

					override fun onComplete() {
						exportDialog.dismiss()
						when (code) {
							JanYoFileUtil.Code.DIR_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.FILE_NOT_EXIST -> Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_export_failed, Snackbar.LENGTH_LONG)
									.show()
							JanYoFileUtil.Code.DONE ->
								if (choose == 0)
									Snackbar.make(coordinatorLayout, context.getString(R.string.hint_export_done, JanYoFileUtil.exportDirPath), Snackbar.LENGTH_SHORT)
											.show()
								else
									AlertDialog.Builder(context)
											.setTitle(R.string.title_dialog_select_share_method)
											.setItems(R.array.shareMethod) { _, which ->
												doSomething(installAPP, which)
											}
											.show()
						}
					}
				})
	}

	private fun doSomething(installAPP: InstallAPP, whatToDo: Int) {
		when (whatToDo) {
			0//提取并分享
			-> JanYoFileUtil.share(context, JanYoFileUtil.getExportFile(installAPP))
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
												JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)))
											else
												Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
														.show()
										}
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
													JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)))
											}
										})
										.show()
								JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.DONE -> JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.sourceDir)))
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
												JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"))
											else
												Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
														.show()
										}
										.addCallback(object : Snackbar.Callback() {
											override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
												if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
													JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"))
											}
										})
										.show()
								JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
										.show()
								JanYoFileUtil.Code.DONE -> JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.renameFormat) + if (newExtensionName.isEmpty()) "" else ".$newExtensionName"))
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
									JanYoFileUtil.doShareFile(context, shareList)
								}
							})
							.show()
					obbList.size == 1 -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb, obbList.size), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									JanYoFileUtil.doShareFile(context, shareList)
								}
							})
							.show()
					else -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb_s, obbList.size), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									JanYoFileUtil.doShareFile(context, shareList)
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

	private fun copyInfoToClipboard(installAPP: InstallAPP) {
		AlertDialog.Builder(context)
				.setTitle(R.string.title_dialog_select_copy_info)
				.setItems(R.array.copyInfo) { _, which ->
					when (which) {
						0 -> copyToClipboard(installAPP.name, installAPP.name)
						1 -> copyToClipboard(installAPP.name, installAPP.packageName)
						2 -> copyToClipboard(installAPP.name, installAPP.versionName)
						3 -> copyToClipboard(installAPP.name, installAPP.versionCode.toString())
					}
				}
				.show()
	}

	private fun copyToClipboard(label: String?, text: String?) {
		val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		clipboardManager.primaryClip = ClipData.newPlainText(label, text)
		Snackbar.make(coordinatorLayout, R.string.hint_copy_info_done, Snackbar.LENGTH_SHORT)
				.show()
	}

	private fun selectUninstallType(installAPP: InstallAPP) {
		AlertDialog.Builder(context)
				.setTitle(R.string.title_dialog_select_uninstall_type)
				.setItems(R.array.uninstallType) { _, which ->
					when (which) {
						0 -> AppManager.uninstallAPP(context, installAPP)
						1 -> showAlert(true, false, arrayListOf(installAPP))
					}
				}
				.show()
	}

	private fun showAlert(isUninstall: Boolean, isDisable: Boolean, appList: ArrayList<InstallAPP>) {
		Observable.create<Boolean> {
			val result = CommandTools.execRootCommand("echo test")
			Logs.i("showAlert: $result")
			it.onNext(result.isSuccess())
			it.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<Boolean> {
					private var isRoot = false
					override fun onComplete() {
						if (!isRoot) {
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

					override fun onSubscribe(d: Disposable) {
					}

					override fun onNext(t: Boolean) {
						isRoot = t
					}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: request root", e)
					}
				})
	}

	private fun doUnInstall(appList: ArrayList<InstallAPP>) {
		Observable.create<CommandTools.CommandResult> {
			it.onNext(AppManager.uninstallAPPByRoot(appList))
			it.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<CommandTools.CommandResult> {
					private lateinit var result: CommandTools.CommandResult
					override fun onComplete() {
						if (result.isSuccess()) {
							list.removeAll(appList)
							fragment.notifyAdapter()
							Snackbar.make(coordinatorLayout, R.string.hint_uninstall_finish, Snackbar.LENGTH_SHORT)
									.show()
						} else {
							Logs.wtf("onComplete: uninstall", result.toString())
							Snackbar.make(coordinatorLayout, R.string.hint_uninstall_error, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_refresh_list) {
										fragment.refreshList()
									}
									.show()
						}
					}

					override fun onSubscribe(d: Disposable) {
					}

					override fun onNext(t: CommandTools.CommandResult) {
						result = t
					}

					override fun onError(e: Throwable) {
					}
				})
	}

	private fun doDisable(appList: ArrayList<InstallAPP>) {
		Observable.create<CommandTools.CommandResult> {
			it.onNext(AppManager.disableAPP(appList))
			it.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<CommandTools.CommandResult> {
					private lateinit var result: CommandTools.CommandResult
					override fun onComplete() {
						if (result.isSuccess()) {
							appList.forEach {
								it.isDisable = true
							}
							fragment.notifyAdapter()
							Snackbar.make(coordinatorLayout, R.string.hint_disable_finish, Snackbar.LENGTH_SHORT)
									.show()
						} else {
							Logs.wtf("onComplete: disable", result.toString())
							Snackbar.make(coordinatorLayout, R.string.hint_disable_error, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_refresh_list) {
										fragment.refreshList()
									}
									.show()
						}
					}

					override fun onSubscribe(d: Disposable) {
					}

					override fun onNext(t: CommandTools.CommandResult) {
						result = t
					}

					override fun onError(e: Throwable) {
					}
				})
	}

	private fun doEnable(appList: ArrayList<InstallAPP>) {
		Observable.create<CommandTools.CommandResult> {
			it.onNext(AppManager.enableAPP(appList))
			it.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<CommandTools.CommandResult> {
					private lateinit var result: CommandTools.CommandResult
					override fun onComplete() {
						if (result.isSuccess()) {
							appList.forEach {
								it.isDisable = false
							}
							fragment.notifyAdapter()
							Snackbar.make(coordinatorLayout, R.string.hint_enable_finish, Snackbar.LENGTH_SHORT)
									.show()
						} else {
							Logs.wtf("onComplete: enable", result.toString())
							Snackbar.make(coordinatorLayout, R.string.hint_enable_error, Snackbar.LENGTH_LONG)
									.setAction(R.string.action_refresh_list) {
										fragment.refreshList()
									}
									.show()
						}
					}

					override fun onSubscribe(d: Disposable) {
					}

					override fun onNext(t: CommandTools.CommandResult) {
						result = t
					}

					override fun onError(e: Throwable) {
					}
				})
	}
}