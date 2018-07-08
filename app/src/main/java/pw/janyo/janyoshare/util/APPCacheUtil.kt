package pw.janyo.janyoshare.util

import android.content.Context
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pw.janyo.janyoshare.APP
import pw.janyo.janyoshare.classes.InstallAPP
import vip.mystery0.logs.Logs
import java.io.File
import java.util.ArrayList

object APPCacheUtil {
	fun loadCacheList(type: Int, init: () -> Unit, observer: Observer<List<InstallAPP>>) {
		Observable.create<List<InstallAPP>> { subscriber ->
			init.invoke()
			Logs.i("loadCacheList: ")
			val fileName: String = when (type) {
				AppManager.AppType.USER -> "${JanYoFileUtil.USER_LIST_FILE}${Settings.sortType}"
				AppManager.AppType.SYSTEM -> "${JanYoFileUtil.SYSTEM_LIST_FILE}${Settings.sortType}"
				else -> {
					Logs.e("subscribe: 应用类型错误")
					""
				}
			}
			val file = File(APP.context.externalCacheDir, fileName)
			val list = JanYoFileUtil.getListFromFile(file, InstallAPP::class.java)
			if (list.size != Settings.getCurrentListSize(type) || !JanYoFileUtil.isCacheAvailable(APP.context, fileName))
				subscriber.onNext(ArrayList())
			else
				subscriber.onNext(list)
			subscriber.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer)
	}

	fun saveCacheList(installAPPList: List<InstallAPP>, fileName: String, observer: Observer<Boolean>) {
		Observable.create<Boolean> {
			val saveUserResult = JanYoFileUtil.saveAppList(APP.context, installAPPList, fileName)
			it.onNext(saveUserResult)
			it.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(observer)
	}
}