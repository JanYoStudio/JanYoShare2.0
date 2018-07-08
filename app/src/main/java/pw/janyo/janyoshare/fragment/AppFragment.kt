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

package pw.janyo.janyoshare.fragment

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.adapter.AppAdapter
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.databinding.FragmentAppBinding
import pw.janyo.janyoshare.util.APPCacheUtil
import pw.janyo.janyoshare.util.AppManager
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseFragment

class AppFragment : BaseFragment(R.layout.fragment_app) {
	private lateinit var binding: FragmentAppBinding
	private var type = 0
	private var sortType = Settings.sortType
	private val list = ArrayList<InstallAPP>()
	private val originList = ArrayList<InstallAPP>()
	private var appAdapter: AppAdapter? = null
	var isSelecting = false
	val appList = ArrayList<InstallAPP>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		type = arguments!!.getInt("type")
	}

	override fun inflateView(layoutId: Int, inflater: LayoutInflater, container: ViewGroup?): View {
		binding = FragmentAppBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun initView() {
		binding.swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light)
		binding.recyclerView.layoutManager = LinearLayoutManager(activity)
		appAdapter = AppAdapter(activity!!, list, this)
		binding.recyclerView.adapter = appAdapter
		binding.recyclerView.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL))
		binding.swipeRefreshLayout.setOnRefreshListener { refreshList() }
		loadCacheList()
	}

	fun mark(data: InstallAPP) {
		if (!appList.contains(data))
			appList.add(data)
		isSelecting = appList.isNotEmpty()
	}

	fun unMark(data: InstallAPP) {
		if (appList.contains(data))
			appList.remove(data)
		isSelecting = appList.isNotEmpty()
	}

	fun isChecked(installAPP: InstallAPP): Boolean {
		appList.forEach {
			if (it.packageName == installAPP.packageName)
				return true
		}
		return false
	}

	fun shouldRefresh(): Boolean {
		return list.isEmpty() || sortType != Settings.sortType
	}

	fun search(query: String) {
		Observable.create<List<InstallAPP>> { subscriber ->
			while (true) {
				if (appAdapter != null)
					break
				Thread.sleep(200)
			}
			list.clear()
			list.addAll(originList)
			if (query.isNotEmpty())
				AppManager.search(list, query)
			subscriber.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<List<InstallAPP>> {
					override fun onSubscribe(d: Disposable) {

					}

					override fun onNext(installAPPList: List<InstallAPP>) {

					}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
					}

					override fun onComplete() {
						appAdapter!!.notifyDataSetChanged()
					}
				})
	}

	fun refreshList() {
		Observable.create<Boolean> { subscriber ->
			val appList = AppManager.getInstallAPPList(activity!!, type)
			list.clear()
			list.addAll(appList)
			originList.clear()
			originList.addAll(appList)
			Settings.setCurrentListSize(type, list.size)
			subscriber.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<Boolean> {
					override fun onSubscribe(d: Disposable) {
						binding.swipeRefreshLayout.isRefreshing = true
					}

					override fun onNext(aBoolean: Boolean) {}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
						binding.swipeRefreshLayout.isRefreshing = false
					}

					override fun onComplete() {
						appAdapter!!.notifyDataSetChanged()
						binding.swipeRefreshLayout.isRefreshing = false
					}
				})
	}

	fun loadCacheList() {
		APPCacheUtil.loadCacheList(type, {
			while (true) {
				if (appAdapter != null)
					break
				Thread.sleep(200)
			}
		}, object : Observer<List<InstallAPP>> {
			private val installAPPList = ArrayList<InstallAPP>()

			override fun onSubscribe(d: Disposable) {
				sortType = Settings.sortType
			}

			override fun onNext(installAPPList: List<InstallAPP>) {
				this.installAPPList.clear()
				this.installAPPList.addAll(installAPPList)
			}

			override fun onError(e: Throwable) {
				refreshList()
			}

			override fun onComplete() {
				if (installAPPList.size != 0) {
					list.clear()
					list.addAll(installAPPList)
					originList.clear()
					originList.addAll(installAPPList)
					appAdapter!!.notifyDataSetChanged()
					binding.swipeRefreshLayout.isRefreshing = false
				} else
					refreshList()
			}
		})
	}

	fun notifyAdapter() {
		appAdapter?.notifyDataSetChanged()
	}

	override fun onDestroy() {
		super.onDestroy()
		APPCacheUtil.saveCacheList(list, when (type) {
			AppManager.AppType.SYSTEM -> "${JanYoFileUtil.SYSTEM_LIST_FILE}$sortType"
			AppManager.AppType.USER -> "${JanYoFileUtil.USER_LIST_FILE}$sortType"
			else -> throw NullPointerException("app type is error!!!")
		}, object : Observer<Boolean> {
			override fun onComplete() {
			}

			override fun onSubscribe(d: Disposable) {
			}

			override fun onNext(t: Boolean) {
				Logs.i("onNext: save $t")
			}

			override fun onError(e: Throwable) {
			}
		})
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
