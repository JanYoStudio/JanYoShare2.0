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
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import java.io.File
import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.adapter.AppAdapter
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.util.AppManager
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseFragment

class AppFragment : BaseFragment(R.layout.fragment_app) {
	private var type = 0
	private var sortType = Settings.sortType
	private var swipeRefreshLayout: SwipeRefreshLayout? = null
	private val list = ArrayList<InstallAPP>()
	private val originList = ArrayList<InstallAPP>()
	private var appAdapter: AppAdapter? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		type = arguments!!.getInt("type")
	}

	override fun initView() {
		val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
		swipeRefreshLayout = findViewById(R.id.swipe_refresh)
		swipeRefreshLayout!!.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light)
		recyclerView.layoutManager = LinearLayoutManager(activity)
		appAdapter = AppAdapter(activity!!, list)
		recyclerView.adapter = appAdapter
		recyclerView.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL))
		swipeRefreshLayout!!.setOnRefreshListener { refreshList() }
	}

	fun shouldRefresh(): Boolean {
		return list.isEmpty() || sortType != Settings.sortType
	}

	fun search(query: String) {
		Logs.i("search: $query")
		Observable.create(ObservableOnSubscribe<List<InstallAPP>> { subscriber ->
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
		})
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

	private fun refreshList() {
		Observable.create(ObservableOnSubscribe<Boolean> { subscriber ->
			val appList = AppManager.getInstallAPPList(activity!!, type)
			list.clear()
			list.addAll(appList)
			originList.clear()
			originList.addAll(appList)
			Settings.setCurrentListSize(type, list.size)
			subscriber.onComplete()
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<Boolean> {
					override fun onSubscribe(d: Disposable) {
						swipeRefreshLayout!!.isRefreshing = true
					}

					override fun onNext(aBoolean: Boolean) {}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
						swipeRefreshLayout!!.isRefreshing = false
					}

					override fun onComplete() {
						Logs.i("onComplete: " + list.size)
						appAdapter!!.notifyDataSetChanged()
						swipeRefreshLayout!!.isRefreshing = false
					}
				})
	}

	fun loadCacheList() {
		Observable.create(ObservableOnSubscribe<List<InstallAPP>> { subscriber ->
			while (true) {
				if (appAdapter != null)
					break
				Thread.sleep(200)
			}
			sortType = Settings.sortType
			val fileName: String = when (type) {
				AppManager.AppType.USER -> JanYoFileUtil.USER_LIST_FILE + Settings.sortType.toString()
				AppManager.AppType.SYSTEM -> JanYoFileUtil.SYSTEM_LIST_FILE + Settings.sortType.toString()
				else -> {
					Logs.e("subscribe: 应用类型错误")
					""
				}
			}
			val file = File(activity!!.externalCacheDir, fileName)
			val list = JanYoFileUtil.getListFromFile(file, InstallAPP::class.java)
			if (list.size != Settings.getCurrentListSize(type) || !JanYoFileUtil.isCacheAvailable(activity!!, fileName))
				subscriber.onNext(ArrayList())
			else
				subscriber.onNext(list)
			subscriber.onComplete()
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<List<InstallAPP>> {
					private val installAPPList = ArrayList<InstallAPP>()

					override fun onSubscribe(d: Disposable) {
						Logs.i("onSubscribe: ")
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
							swipeRefreshLayout!!.isRefreshing = false
						} else
							refreshList()
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
