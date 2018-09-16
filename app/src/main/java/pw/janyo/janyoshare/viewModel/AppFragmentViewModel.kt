package pw.janyo.janyoshare.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.util.AppManager

class AppFragmentViewModel(private val mApplication: Application,
						   val type: Int) : AndroidViewModel(mApplication) {
	var appList = MutableLiveData<List<InstallAPP>>()
	var showList = MutableLiveData<List<InstallAPP>>()

	fun refreshList(): MutableLiveData<List<InstallAPP>> {
		val list = MutableLiveData<List<InstallAPP>>()
		val installList = AppManager.getInstallAPPList(mApplication, type)
	}
}