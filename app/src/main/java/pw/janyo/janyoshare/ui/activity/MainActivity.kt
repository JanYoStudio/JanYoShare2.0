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

package pw.janyo.janyoshare.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders

import java.util.Calendar

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.ui.adapter.ViewPagerAdapter
import pw.janyo.janyoshare.ui.fragment.AppFragment
import pw.janyo.janyoshare.utils.AppManagerUtil
import pw.janyo.janyoshare.utils.JanYoFileUtil
import pw.janyo.janyoshare.utils.LayoutUtil
import pw.janyo.janyoshare.utils.Settings
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.tools.base.BaseActivity

class MainActivity : BaseActivity(R.layout.activity_main) {
	private lateinit var mainViewModel: MainViewModel
	private var lastPressTime: Long = 0
	private lateinit var currentFragment: AppFragment
	private lateinit var viewPagerAdapter: ViewPagerAdapter

	override fun initView() {
		super.initView()
		setSupportActionBar(toolbar)
		val toggle = ActionBarDrawerToggle(
				this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawerLayout.addDrawerListener(toggle)
		toggle.syncState()
	}

	override fun initData() {
		super.initData()
		initViewModel()
		viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
		val userFragment = AppFragment.newInstance(AppManagerUtil.AppType.USER)
		val systemFragment = AppFragment.newInstance(AppManagerUtil.AppType.SYSTEM)
		viewPagerAdapter.addFragment(userFragment, getString(R.string.title_fragment_user))
		viewPagerAdapter.addFragment(systemFragment, getString(R.string.title_fragment_system))
		currentFragment = userFragment
		viewPager.adapter = viewPagerAdapter
		tabLayout.setupWithViewPager(viewPager)

		if (Settings.isAutoClean)
			if (Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_SDCARD || Settings.exportDir == JanYoFileUtil.Export.EXPORT_DIR_CUSTOM)
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
					ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_AUTO_CLEAN)
				else
					clearFiles()
	}

	private fun initViewModel() {
		mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
	}

	override fun monitor() {
		viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

			override fun onPageSelected(position: Int) {
				val fragment = viewPagerAdapter.getItem(position) as AppFragment
				if (fragment.shouldRefresh())
					fragment.loadCacheList()
				currentFragment = fragment
			}

			override fun onPageScrollStateChanged(state: Int) {}
		})
		navView.setNavigationItemSelectedListener { item ->
			when (item.itemId) {
				R.id.action_face_to_face_share -> Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
						.show()
				R.id.action_clear_temp_dir -> clearFiles()
				R.id.action_night_mode -> {
					val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
					delegate.setLocalNightMode(
							if (currentNightMode == Configuration.UI_MODE_NIGHT_NO)
								AppCompatDelegate.MODE_NIGHT_YES
							else
								AppCompatDelegate.MODE_NIGHT_NO)
					recreate()
				}
				R.id.action_license -> LayoutUtil.showLicense(this)
				R.id.action_support_us -> Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
						.show()
				R.id.action_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
			}
			drawerLayout.closeDrawer(GravityCompat.START)
			true
		}
	}

	private fun clearFiles() {
		when (JanYoFileUtil.cleanFileDir()) {
			JanYoFileUtil.Code.MAKE_DIR_ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
					.show()
			JanYoFileUtil.Code.DONE -> Snackbar.make(coordinatorLayout, R.string.hint_clean_dir_done, Snackbar.LENGTH_LONG)
					.show()
			JanYoFileUtil.Code.ERROR -> Snackbar.make(coordinatorLayout, R.string.hint_clean_dir_error, Snackbar.LENGTH_LONG)
					.show()
		}
	}

	override fun onBackPressed() {
		when {
			drawerLayout.isDrawerOpen(GravityCompat.START) ->
				drawerLayout.closeDrawer(GravityCompat.START)
			Calendar.getInstance().timeInMillis - lastPressTime >= 3000 -> {
				lastPressTime = Calendar.getInstance().timeInMillis
				Snackbar.make(coordinatorLayout, R.string.hint_twice_press_exit, Snackbar.LENGTH_LONG)
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
				Snackbar.make(coordinatorLayout, R.string.hint_permission_write_external, Snackbar.LENGTH_LONG)
						.setAction(R.string.action_grant_permission) { ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_AUTO_CLEAN) }
						.addCallback(object : Snackbar.Callback() {
							override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
								if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
									Snackbar.make(coordinatorLayout, R.string.hint_permission_denied, Snackbar.LENGTH_LONG)
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
