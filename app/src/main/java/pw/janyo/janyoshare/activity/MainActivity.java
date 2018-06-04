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

package pw.janyo.janyoshare.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.adapter.ViewPagerAdapter;
import pw.janyo.janyoshare.fragment.AppFragment;
import pw.janyo.janyoshare.util.AppManager;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import pw.janyo.janyoshare.util.Settings;
import vip.mystery0.logs.Logs;
import vip.mystery0.tools.base.BaseActivity;

public class MainActivity extends BaseActivity {
	private final static int PERMISSION_AUTO_CLEAN = 233;
	private long lastPressTime = 0;
	private AppFragment currentFragment;
	private Toolbar toolbar;
	private CoordinatorLayout coordinatorLayout;
	private DrawerLayout drawer;
	private NavigationView navigationView;
	private TabLayout tabLayout;
	private ViewPager viewPager;

	public MainActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void bindView() {
		super.bindView();
		toolbar = findViewById(R.id.toolbar);
		coordinatorLayout = findViewById(R.id.coordinatorLayout);
		drawer = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.nav_view);
		tabLayout = findViewById(R.id.title_tabs);
		viewPager = findViewById(R.id.viewpager);
	}

	@Override
	public void initData() {
		final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		AppFragment userFragment = AppFragment.newInstance(AppManager.USER);
		AppFragment systemFragment = AppFragment.newInstance(AppManager.SYSTEM);
		viewPagerAdapter.addFragment(userFragment, getString(R.string.title_fragment_user));
		viewPagerAdapter.addFragment(systemFragment, getString(R.string.title_fragment_system));
		currentFragment = userFragment;
		currentFragment.loadCacheList();
		viewPager.setAdapter(viewPagerAdapter);
		tabLayout.setupWithViewPager(viewPager);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				AppFragment fragment = (AppFragment) viewPagerAdapter.getItem(position);
				if (fragment.shouldRefresh())
					fragment.loadCacheList();
				currentFragment = fragment;
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		setSupportActionBar(toolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		if (Settings.isAutoClean())
			if (Settings.getExportDir() == JanYoFileUtil.EXPORT_DIR_SDCARD || Settings.getExportDir() == JanYoFileUtil.EXPORT_DIR_CUSTOM)
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_AUTO_CLEAN);
				else
					clearFiles();
	}

	@Override
	public void monitor() {
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_face_to_face_share:
//                        startActivity(new Intent(MainActivity.this, FaceToFaceActivity.class));
						Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
								.show();
						break;
					case R.id.action_clear_temp_dir:
						clearFiles();
						break;
					case R.id.action_night_mode:
						int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
						getDelegate().setLocalNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_NO
								? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
						reStartActivity();
						break;
					case R.id.action_license:
						Observable.create(new ObservableOnSubscribe<View>() {
							@Override
							public void subscribe(ObservableEmitter<View> subscriber) {
								View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_license, new NestedScrollView(MainActivity.this), false);
								TextView licensePoint1 = view.findViewById(R.id.license_point1);
								TextView licensePoint2 = view.findViewById(R.id.license_point2);
								TextView licensePoint3 = view.findViewById(R.id.license_point3);
								VectorDrawableCompat point = VectorDrawableCompat.create(getResources(), R.drawable.ic_point, null);
								if (point != null)
									point.setBounds(0, 0, point.getMinimumWidth(), point.getMinimumHeight());
								licensePoint1.setCompoundDrawables(point, null, null, null);
								licensePoint2.setCompoundDrawables(point, null, null, null);
								licensePoint3.setCompoundDrawables(point, null, null, null);
								subscriber.onNext(view);
								subscriber.onComplete();
							}
						})
								.subscribeOn(Schedulers.newThread())
								.unsubscribeOn(Schedulers.newThread())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(new DisposableObserver<View>() {
									private View view;

									@Override
									public void onNext(View view) {
										this.view = view;
									}

									@Override
									public void onError(Throwable e) {
										Logs.wtf("onError: ", e);
									}

									@Override
									public void onComplete() {
										new AlertDialog.Builder(MainActivity.this)
												.setTitle(" ")
												.setView(view)
												.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														Logs.i("onClick: ");
													}
												})
												.show();
									}
								});
						break;
					case R.id.action_support_us:
						Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
								.show();
						break;
					case R.id.action_settings:
						startActivity(new Intent(MainActivity.this, SettingsActivity.class));
						break;
				}
				drawer.closeDrawer(GravityCompat.START);
				return true;
			}
		});
	}

	private void reStartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	private void clearFiles() {
		switch (JanYoFileUtil.cleanFileDir()) {
			case JanYoFileUtil.MAKE_DIR_ERROR:
				Snackbar.make(coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
						.show();
				break;
			case JanYoFileUtil.DONE:
				Snackbar.make(coordinatorLayout, R.string.hint_clean_dir_done, Snackbar.LENGTH_LONG)
						.show();
				break;
			case JanYoFileUtil.ERROR:
				Snackbar.make(coordinatorLayout, R.string.hint_clean_dir_error, Snackbar.LENGTH_LONG)
						.show();
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (Calendar.getInstance().getTimeInMillis() - lastPressTime >= 3000) {
			lastPressTime = Calendar.getInstance().getTimeInMillis();
			Snackbar.make(coordinatorLayout, R.string.hint_twice_press_exit, Snackbar.LENGTH_LONG)
					.show();
		} else
			finish();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setIconified(true);
		menu.findItem(R.id.action_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				menu.findItem(R.id.action_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				menu.findItem(R.id.action_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				return true;
			}
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				currentFragment.search(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				currentFragment.search(newText);
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				break;
			case R.id.action_sort:
				final int current = Settings.getSortType();
				final int[] temp = {current};
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.title_dialog_select_sort_type)
						.setSingleChoiceItems(R.array.sortType, current, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								temp[0] = which;
							}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								int selected = temp[0];
								Settings.setSortType(selected);
								if (current != selected)
									currentFragment.loadCacheList();
							}
						})
						.show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_AUTO_CLEAN) {
			boolean result = true;
			for (int temp : grantResults)
				result = result & temp == PackageManager.PERMISSION_GRANTED;
			if (!result) {
				Snackbar.make(coordinatorLayout, R.string.hint_permission_write_external, Snackbar.LENGTH_LONG)
						.setAction(R.string.action_grant_permission, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_AUTO_CLEAN);
							}
						})
						.addCallback(new Snackbar.Callback() {
							@Override
							public void onDismissed(Snackbar transientBottomBar, int event) {
								if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
									Snackbar.make(coordinatorLayout, R.string.hint_permission_denied, Snackbar.LENGTH_LONG)
											.show();
							}
						})
						.show();
			}
		}
	}
}
