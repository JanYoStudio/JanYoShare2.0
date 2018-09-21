package pw.janyo.janyoshare.repository

import androidx.lifecycle.MutableLiveData
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.repository.local.InstallAPPLocalDataSource
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.utils.Settings
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver
import java.util.*

object InstallAPPRepository {
	fun loadCacheList(mainViewModel: MainViewModel, type: Int) {
		Logs.i("loadCacheList: ")
		mainViewModel.selectedList.clear()
		val calendar = Calendar.getInstance()
		if (calendar.timeInMillis - Settings.saveTimeZone.timeInMillis >= Settings.cacheExpirationTime * 24f * 60f * 60f * 1000f)
			InstallAPPLocalDataSource.loadList(getAPPListFromType(mainViewModel, type), getOriginAPPListFromType(mainViewModel, type), type)
		else
			InstallAPPLocalDataSource.loadCacheList(getAPPListFromType(mainViewModel, type), getOriginAPPListFromType(mainViewModel, type), type)
	}

	fun loadList(mainViewModel: MainViewModel, type: Int) {
		Logs.i("loadList: ")
		mainViewModel.selectedList.clear()
		InstallAPPLocalDataSource.loadList(getAPPListFromType(mainViewModel, type), getOriginAPPListFromType(mainViewModel, type), type)
	}

	fun query(mainViewModel: MainViewModel, type: Int, keyWord: String) {
		RxObservable<List<InstallAPP>>()
				.doThings {
					val originList = getOriginAPPListFromType(mainViewModel, type).value
					if (originList != null) {
						if (keyWord.isNotEmpty())
							it.onFinish(AppManagerUtil.search(originList, keyWord))
						else
							it.onFinish(originList)
					}
				}
				.subscribe(object : RxObserver<List<InstallAPP>>() {
					override fun onError(e: Throwable) {
						setAPPListFromType(mainViewModel, type, PackageData.error(e))
					}

					override fun onFinish(data: List<InstallAPP>?) {
						if (data == null || data.isEmpty())
							setAPPListFromType(mainViewModel, type, PackageData.empty())
						else
							setAPPListFromType(mainViewModel, type, PackageData.content(data))
					}
				})
	}

	private fun getAPPListFromType(mainViewModel: MainViewModel, type: Int): MutableLiveData<PackageData<List<InstallAPP>>> = when (type) {
		AppManagerUtil.AppType.SYSTEM -> mainViewModel.systemAPPList
		AppManagerUtil.AppType.USER -> mainViewModel.userAPPList
		else -> throw NullPointerException("type error")
	}

	private fun getOriginAPPListFromType(mainViewModel: MainViewModel, type: Int): MutableLiveData<List<InstallAPP>> = when (type) {
		AppManagerUtil.AppType.SYSTEM -> mainViewModel.originSystemAPPList
		AppManagerUtil.AppType.USER -> mainViewModel.originUserAPPList
		else -> throw NullPointerException("type error")
	}

	private fun setAPPListFromType(mainViewModel: MainViewModel, type: Int, packageData: PackageData<List<InstallAPP>>) {
		when (type) {
			AppManagerUtil.AppType.SYSTEM -> mainViewModel.systemAPPList.value = packageData
			AppManagerUtil.AppType.USER -> mainViewModel.userAPPList.value = packageData
		}
	}
}