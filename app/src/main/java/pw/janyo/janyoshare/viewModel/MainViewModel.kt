package pw.janyo.janyoshare.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.janyoshare.model.InstallAPP
import vip.mystery0.rxpackagedata.PackageData

class MainViewModel : ViewModel() {
	var originUserAPPList = MutableLiveData<List<InstallAPP>>()
	var originSystemAPPList = MutableLiveData<List<InstallAPP>>()
	var userAPPList = MutableLiveData<PackageData<List<InstallAPP>>>()
	var systemAPPList = MutableLiveData<PackageData<List<InstallAPP>>>()
	var selectedList = ArrayList<InstallAPP>()
}