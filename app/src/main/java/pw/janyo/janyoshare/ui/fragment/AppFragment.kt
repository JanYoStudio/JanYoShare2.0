/*
 * Created by Mystery0 on 18-2-10 下午4:00.
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
 * Last modified 18-2-10 下午4:00
 */

package pw.janyo.janyoshare.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProviders

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.ui.adapter.AppAdapter
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.databinding.FragmentAppBinding
import pw.janyo.janyoshare.repository.InstallAPPRepository
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.utils.Settings
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.Status.*
import vip.mystery0.tools.base.binding.BaseBindingFragment

class AppFragment : BaseBindingFragment<FragmentAppBinding>(R.layout.fragment_app) {
	private lateinit var mainViewModel: MainViewModel
	private var type = 0
	private var sortType = Settings.sortType
	private lateinit var appAdapter: AppAdapter

	private val appListObserver = Observer<PackageData<List<InstallAPP>>> {
		when (it.status) {
			Content -> {
				binding.swipeRefreshLayout.isRefreshing = false
				appAdapter.replaceAll(it.data!!, true)
			}
			Loading -> binding.swipeRefreshLayout.isRefreshing = true
			Empty -> binding.swipeRefreshLayout.isRefreshing = false
			Error -> {
				binding.swipeRefreshLayout.isRefreshing = false
				Logs.wtfm("appListObserver: ", it.error)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		type = arguments!!.getInt("type")
	}

	override fun initView() {
		initViewModel()
		binding.swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light)
		binding.recyclerView.layoutManager = LinearLayoutManager(activity)
		appAdapter = AppAdapter(activity!!, this, mainViewModel)
		binding.recyclerView.adapter = appAdapter
		binding.recyclerView.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL))
		binding.swipeRefreshLayout.setOnRefreshListener { refreshList() }
		loadCacheList()
	}

	private fun initViewModel() {
		mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
		when (type) {
			AppManagerUtil.AppType.USER -> mainViewModel.userAPPList.observe(this, appListObserver)
			AppManagerUtil.AppType.SYSTEM -> mainViewModel.systemAPPList.observe(this, appListObserver)
		}
	}

	fun mark(data: InstallAPP) {
		if (!mainViewModel.selectedList.contains(data))
			mainViewModel.selectedList.add(data)
	}

	fun unMark(data: InstallAPP) {
		if (mainViewModel.selectedList.contains(data))
			mainViewModel.selectedList.remove(data)
	}

	fun isChecked(installAPP: InstallAPP): Boolean {
		mainViewModel.selectedList.forEach {
			if (it.packageName == installAPP.packageName)
				return true
		}
		return false
	}

	fun shouldRefresh(): Boolean {
		return appAdapter.items.isEmpty() || sortType != Settings.sortType
	}

	fun search(query: String) {
	}

	fun refreshList() {
		InstallAPPRepository.loadList(mainViewModel, type)
	}

	fun loadCacheList() {
//		APPCacheUtil.loadCacheList(type, {
//			while (true) {
//				if (appAdapter != null)
//					break
//				Thread.sleep(200)
//			}
//		}, object : Observer<List<InstallAPP>> {
//			private val installAPPList = ArrayList<InstallAPP>()
//
//			override fun onSubscribe(d: Disposable) {
//				sortType = Settings.sortType
//			}
//
//			override fun onNext(installAPPList: List<InstallAPP>) {
//				this.installAPPList.clear()
//				this.installAPPList.addAll(installAPPList)
//			}
//
//			override fun onError(e: Throwable) {
//				refreshList()
//			}
//
//			override fun onComplete() {
//				if (installAPPList.size != 0) {
//					list.clear()
//					list.addAll(installAPPList)
//					originList.clear()
//					originList.addAll(installAPPList)
//					appAdapter!!.notifyDataSetChanged()
//					binding.swipeRefreshLayout.isRefreshing = false
//				} else
//					refreshList()
//			}
//		})
	}

	fun notifyAdapter() {
		appAdapter.notifyDataSetChanged()
	}

	companion object {
		fun newInstance(type: Int): AppFragment {
			val bundle = Bundle()
			bundle.putInt("type", type)
			val fragment = AppFragment()
			fragment.arguments = bundle
			return fragment
		}
	}
}
