package pw.janyo.janyoshare.repository

import androidx.lifecycle.MutableLiveData
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.repository.local.InstallAPPLocalDataSource
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver

object InstallAPPRepository {
	fun loadCacheList(mainViewModel: MainViewModel, type: Int) {
		mainViewModel.selectedList.clear()
	}

	fun loadList(mainViewModel: MainViewModel, type: Int) {
		mainViewModel.selectedList.clear()
		InstallAPPLocalDataSource.loadList(getAPPListFromType(mainViewModel, type), getOriginAPPListFromType(mainViewModel, type), type)
	}

	fun query(mainViewModel: MainViewModel, type: Int, keyWord: String) {
		RxObservable<List<InstallAPP>>()
				.doThings {
					val originList = getOriginAPPListFromType(mainViewModel, type).value
					if (originList != null && keyWord.isNotEmpty())
						it.onFinish(AppManagerUtil.search(originList, keyWord))
				}
				.subscribe(object : RxObserver<List<InstallAPP>>() {
					override fun onError(e: Throwable) {

					}

					override fun onFinish(data: List<InstallAPP>?) {
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
}