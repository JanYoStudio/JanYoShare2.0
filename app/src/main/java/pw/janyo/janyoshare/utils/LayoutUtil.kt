package pw.janyo.janyoshare.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.databinding.DialogLicenseBinding
import vip.mystery0.logs.Logs
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver

object LayoutUtil {
	fun showLicense(context: Context) {
		val binding = DialogLicenseBinding.inflate(LayoutInflater.from(context))
		RxObservable<View>()
				.doThings {
					val point = VectorDrawableCompat.create(context.resources, R.drawable.ic_point, null)
					point?.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
					binding.licensePoint1.setCompoundDrawables(point, null, null, null)
					binding.licensePoint2.setCompoundDrawables(point, null, null, null)
					binding.licensePoint3.setCompoundDrawables(point, null, null, null)
					it.onFinish(binding.root)
				}
				.subscribe(object :RxObserver<View>(){
					override fun onError(e: Throwable) {
						Logs.wtfm("onError: ", e)
					}

					override fun onFinish(data: View?) {
						AlertDialog.Builder(context)
								.setTitle(" ")
								.setView(data)
								.setPositiveButton(android.R.string.ok, null)
								.show()
					}
				})
	}
}