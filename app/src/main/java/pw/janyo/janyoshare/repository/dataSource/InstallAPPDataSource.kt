package pw.janyo.janyoshare.repository.dataSource

import androidx.lifecycle.MutableLiveData
import pw.janyo.janyoshare.model.InstallAPP
import vip.mystery0.rxpackagedata.PackageData

interface InstallAPPDataSource {
	fun loadCacheList(appListLiveData: MutableLiveData<PackageData<List<InstallAPP>>>, originAPPListLiveData: MutableLiveData<List<InstallAPP>>, type: Int)

	fun loadList(appListLiveData: MutableLiveData<PackageData<List<InstallAPP>>>, originAPPListLiveData: MutableLiveData<List<InstallAPP>>, type: Int)
}