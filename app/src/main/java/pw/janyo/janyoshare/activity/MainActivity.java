package pw.janyo.janyoshare.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.adapter.ViewPagerAdapter;
import pw.janyo.janyoshare.fragment.AppFragment;
import pw.janyo.janyoshare.util.AppManager;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import pw.janyo.janyoshare.util.Settings;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final static int PERMISSION_AUTO_CLEAN = 233;
    private long lastPressTime = 0;
    private AppFragment currentFragment;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialization();
        monitor();
    }

    private void initialization() {
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.title_tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);

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
                if (fragment.isEmpty())
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
            if (Settings.getExportDir() == JanYoFileUtil.EXPORT_DIR_SDCARD && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_AUTO_CLEAN);
            else
                clearFiles();
    }

    private void monitor() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_face_to_face_share:
                        startActivity(new Intent(MainActivity.this, FaceToFaceActivity.class));
//                        APUtil.openAP(MainActivity.this, "test", "testtest");
                        break;
                    case R.id.action_clear_temp_dir:
                        clearFiles();
                        break;
                    case R.id.action_night_mode:
                        break;
                    case R.id.action_license:
                        break;
                    case R.id.action_support_us:
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_AUTO_CLEAN) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
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
}
