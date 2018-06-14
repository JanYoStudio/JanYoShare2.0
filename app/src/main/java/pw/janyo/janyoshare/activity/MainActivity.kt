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

package pw.janyo.janyoshare.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import java.util.Calendar

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.adapter.ViewPagerAdapter
import pw.janyo.janyoshare.databinding.ActivityMainBinding
import pw.janyo.janyoshare.databinding.AppBarMainBinding
import pw.janyo.janyoshare.fragment.AppFragment
import pw.janyo.janyoshare.util.AppManager
import pw.janyo.janyoshare.util.JanYoFileUtil
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseActivity

class MainActivity : BaseActivity(R.layout.activity_main) {
	private lateinit var mainBinding: ActivityMainBinding
	private lateinit var binding: AppBarMainBinding
	private var lastPressTime: Long = 0
	private lateinit var currentFragment: AppFragment
	val coordinatorLayout: CoordinatorLayout
		get() = binding.coordinatorLayout

	override fun inflateView(layoutId: Int) {
		mainBinding = DataBindingUtil.setContentView(this, layoutId)
		binding = mainBinding.include!!
	}

	override fun initData() {
		val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
		val userFragment = AppFragment.newInstance(AppManager.AppType.USER)
		val systemFragment = AppFragment.newInstance(AppManager.AppType.SYSTEM)
		viewPagerAdapter.addFragment(userFragment, getString(R.string.title_fragment_user))
		viewPagerAdapter.addFragment(systemFragment, getString(R.string.title_fragment_system))
		currentFragment = userFragment
		currentFragment.loadCacheList()
		binding.viewPager.adapter = viewPagerAdapter
		binding.titleTabs.setupWithViewPager(binding.viewPager)
		binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

			override fun onPageSelected(position: Int) {
				val fragment = viewPagerAdapter.getItem(position) as AppFragment
				if (fragment.shouldRefresh())
					fragment.loadCacheList()
				currentFragment = fragment
			}

			override fun onPageScrollStateChanged(state: Int) {}
		})

		setSupportActionBar(binding.toolbar)
		val toggle = ActionBarDrawerToggle(
				this, mainBinding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		mainBinding.drawerLayout.addDrawerListener(toggle)
		toggle.syncState()

		if (Settings.isAutoClean)
			if (Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_SDCARD || Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM)
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
					ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_AUTO_CLEAN)
				else
					clearFiles()
	}

	override fun monitor() {
		mainBinding.navView.setNavigationItemSelectedListener { item ->
			when (item.itemId) {
				R.id.action_face_to_face_share ->
					Snackbar.make(binding.coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
							.show()
				R.id.action_clear_temp_dir ->
					clearFiles()
				R.id.action_night_mode -> {
					val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
					delegate.setLocalNightMode(if (currentNightMode == Configuration.UI_MODE_NIGHT_NO)
						AppCompatDelegate.MODE_NIGHT_YES
					else
						AppCompatDelegate.MODE_NIGHT_NO)
					reStartActivity()
				}
				R.id.action_license ->
					Observable.create<View> { subscriber ->
						val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_license, NestedScrollView(this@MainActivity), false)
						val licensePoint1 = view.findViewById<TextView>(R.id.license_point1)
						val licensePoint2 = view.findViewById<TextView>(R.id.license_point2)
						val licensePoint3 = view.findViewById<TextView>(R.id.license_point3)
						val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
						point?.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
						licensePoint1.setCompoundDrawables(point, null, null, null)
						licensePoint2.setCompoundDrawables(point, null, null, null)
						licensePoint3.setCompoundDrawables(point, null, null, null)
						subscriber.onNext(view)
						subscriber.onComplete()
					}
							.subscribeOn(Schedulers.newThread())
							.unsubscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(object : DisposableObserver<View>() {
								private var view: View? = null

								override fun onNext(view: View) {
									this.view = view
								}

								override fun onError(e: Throwable) {
									Logs.wtf("onError: ", e)
								}

								override fun onComplete() {
									AlertDialog.Builder(this@MainActivity)
											.setTitle(" ")
											.setView(view)
											.setPositiveButton(android.R.string.ok) { _, _ -> Logs.i("onClick: ") }
											.show()
								}
							})
				R.id.action_support_us -> Snackbar.make(binding.coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
						.show()
				R.id.action_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
			}
			mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
			true
		}
	}

	private fun reStartActivity() {
		finish()
		startActivity(intent)
	}

	private fun clearFiles() {
		when (JanYoFileUtil.cleanFileDir()) {
			JanYoFileUtil.Code.MAKE_DIR_ERROR -> Snackbar.make(binding.coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
					.show()
			JanYoFileUtil.Code.DONE -> Snackbar.make(binding.coordinatorLayout, R.string.hint_clean_dir_done, Snackbar.LENGTH_LONG)
					.show()
			JanYoFileUtil.Code.ERROR -> Snackbar.make(binding.coordinatorLayout, R.string.hint_clean_dir_error, Snackbar.LENGTH_LONG)
					.show()
		}
	}

	override fun onBackPressed() {
		when {
			mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START) ->
				mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
			Calendar.getInstance().timeInMillis - lastPressTime >= 3000 -> {
				lastPressTime = Calendar.getInstance().timeInMillis
				Snackbar.make(binding.coordinatorLayout, R.string.hint_twice_press_exit, Snackbar.LENGTH_LONG)
						.show()
			}
			else -> finish()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main, menu)
		val searchView = menu.findItem(R.id.action_search).actionView as SearchView
		searchView.isIconified = true
		menu.findItem(R.id.action_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
			override fun onMenuItemActionExpand(item: MenuItem): Boolean {
				menu.findItem(R.id.action_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
				return true
			}

			override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
				menu.findItem(R.id.action_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
				return true
			}
		})
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				currentFragment.search(query)
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean {
				currentFragment.search(newText)
				return false
			}
		})
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_search -> {
			}
			R.id.action_sort -> {
				val current = Settings.sortType
				val temp = intArrayOf(current)
				AlertDialog.Builder(this@MainActivity)
						.setTitle(R.string.title_dialog_select_sort_type)
						.setSingleChoiceItems(R.array.sortType, current) { _, which -> temp[0] = which }
						.setPositiveButton(android.R.string.ok) { _, _ ->
							val selected = temp[0]
							Settings.sortType = selected
							if (current != selected)
								currentFragment.loadCacheList()
						}
						.show()
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if (requestCode == PERMISSION_AUTO_CLEAN) {
			var result = true
			for (temp in grantResults)
				result = result and (temp == PackageManager.PERMISSION_GRANTED)
			if (!result) {
				Snackbar.make(binding.coordinatorLayout, R.string.hint_permission_write_external, Snackbar.LENGTH_LONG)
						.setAction(R.string.action_grant_permission) { ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_AUTO_CLEAN) }
						.addCallback(object : Snackbar.Callback() {
							override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
								if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
									Snackbar.make(binding.coordinatorLayout, R.string.hint_permission_denied, Snackbar.LENGTH_LONG)
											.show()
							}
						})
						.show()
			}
		}
	}

	companion object {
		private const val PERMISSION_AUTO_CLEAN = 233
	}
}
