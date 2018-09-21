package pw.janyo.janyoshare.repository.local

import androidx.lifecycle.MutableLiveData
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.repository.dataSource.InstallAPPDataSource
import pw.janyo.janyoshare.repository.local.service.impl.InstallAPPServiceImpl
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.utils.Settings
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver
import java.util.*

object InstallAPPLocalDataSource : InstallAPPDataSource {
	private val installAPPService = InstallAPPServiceImpl()
	override fun loadCacheList(appListLiveData: MutableLiveData<PackageData<List<InstallAPP>>>, originAPPListLiveData: MutableLiveData<List<InstallAPP>>, type: Int) {
		appListLiveData.value = PackageData.loading()
		RxObservable<List<InstallAPP>>()
				.doThings {
					it.onFinish(AppManagerUtil.sort(installAPPService.queryAPPForType(type)))
				}
				.subscribe(object : RxObserver<List<InstallAPP>>() {
					override fun onError(e: Throwable) {
						appListLiveData.value = PackageData.error(e)
					}

					override fun onFinish(data: List<InstallAPP>?) {
						if (data == null || data.isEmpty())
							appListLiveData.value = PackageData.empty()
						else {
							originAPPListLiveData.value = data
							appListLiveData.value = PackageData.content(data)
						}
					}
				})
	}

	override fun loadList(appListLiveData: MutableLiveData<PackageData<List<InstallAPP>>>, originAPPListLiveData: MutableLiveData<List<InstallAPP>>, type: Int) {
		appListLiveData.value = PackageData.loading()
		RxObservable<List<InstallAPP>>()
				.doThings {
					val list = AppManagerUtil.getInstallAPPList(type)
					saveList(list, type)
					it.onFinish(list)
				}
				.subscribe(object : RxObserver<List<InstallAPP>>() {
					override fun onError(e: Throwable) {
						appListLiveData.value = PackageData.error(e)
					}

					override fun onFinish(data: List<InstallAPP>?) {
						if (data == null || data.isEmpty())
							appListLiveData.value = PackageData.empty()
						else {
							originAPPListLiveData.value = data
							appListLiveData.value = PackageData.content(data)
						}
					}
				})
	}

	private fun saveList(list: List<InstallAPP>, type: Int) {
		val savedList = installAPPService.queryAPPForType(type)
		savedList.forEach { installAPPService.delInstallAPP(it) }
		list.forEach { installAPPService.addInstallAPP(it) }
		Settings.saveTimeZone = Calendar.getInstance()
	}
}