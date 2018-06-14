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
import android.view.View
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
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.logs.Logs
import java.io.File
import java.util.ArrayList

class ItemAppClickHandler(val coordinatorLayout: CoordinatorLayout,
						  val context: Context) {
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
				.setItems(R.array.copyOperation) { _, which ->
					exportThen(data, which)
				}
				.show()
	}

	private fun exportThen(installAPP: InstallAPP, choose: Int) {
		if (choose == 6) {
			copyInfoToClipboard(installAPP)
			return
		}
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
						Log.wtf("exportThen: onError: ", e)
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
							JanYoFileUtil.Code.DONE -> doSomething(installAPP, choose)
						}
					}
				})
	}

	private fun doSomething(installAPP: InstallAPP, whatToDo: Int) {
		when (whatToDo) {
			0//仅提取
			-> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_export_done, JanYoFileUtil.exportDirPath), Snackbar.LENGTH_SHORT)
					.show()
			1//提取并分享
			-> JanYoFileUtil.share(context, JanYoFileUtil.getExportFile(installAPP))
			2//重命名后分享
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
			3//重命名扩展名
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
			4//和数据包一起提取分享
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
			5//面对面分享
			-> Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
					.show()
		}//静默卸载
		//				PackagesUtil.uninstall(context, installAPP.getPackageName());
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
}