package pw.janyo.janyoshare.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.activity.SettingsActivity;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import pw.janyo.janyoshare.util.Settings;
import vip.mystery0.tools.logs.Logs;

public class SettingsPreferenceFragment extends PreferenceFragment {
    private static final String TAG = "SettingsPreferenceFragment";
    private static final int PERMISSION_REQUEST_CODE = 233;

    private CoordinatorLayout coordinatorLayout;

    private SwitchPreference isAutoCleanPreference;
    private Preference exportDirPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        coordinatorLayout = ((SettingsActivity) getActivity()).coordinatorLayout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bindView();
        initialization();
        monitor();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void bindView() {
        isAutoCleanPreference = (SwitchPreference) findPreference(R.string.key_auto_clean);
        exportDirPreference = findPreference(R.string.key_export_dir);
    }

    private void initialization() {
        isAutoCleanPreference.setChecked(Settings.isAutoClean());
        if (Settings.isAutoClean())
            isAutoCleanPreference.setSummary(R.string.summary_auto_clean_on);
        else
            isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off);
        exportDirPreference.setSummary(getString(R.string.summary_export_dir, JanYoFileUtil.getExportDirPath()));
    }

    private void monitor() {
        isAutoCleanPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean isAutoClean = !isAutoCleanPreference.isChecked();
                if (isAutoClean)
                    new AlertDialog.Builder(getActivity())
                            .setTitle(" ")
                            .setMessage(R.string.hint_warning_auto_clean)
                            .setPositiveButton(R.string.action_open, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Settings.setAutoClean(true);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isAutoCleanPreference.setChecked(false);
                                    Settings.setAutoClean(false);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    isAutoCleanPreference.setChecked(Settings.isAutoClean());
                                    if (Settings.isAutoClean())
                                        isAutoCleanPreference.setSummary(R.string.summary_auto_clean_on);
                                    else
                                        isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off);
                                }
                            })
                            .show();
                else {
                    Settings.setAutoClean(false);
                    isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off);
                }
                return true;
            }
        });
        exportDirPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_export_dir)
                        .setSingleChoiceItems(R.array.exportDir, Settings.getExportDir(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Logs.i(TAG, "onClick: " + which);
                                if (which != 2 || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                    Settings.setExportDir(which);
                                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                } else
                                    Snackbar.make(coordinatorLayout, R.string.hint_permission, Snackbar.LENGTH_LONG)
                                            .show();
                                exportDirPreference.setSummary(getString(R.string.summary_export_dir, JanYoFileUtil.getExportDirPath()));
                            }
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;
            }
        });
    }

    private Preference findPreference(@StringRes int id) {
        return findPreference(getString(id));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Settings.setExportDir(2);
            else
                Snackbar.make(coordinatorLayout, R.string.hint_permission_denied, Snackbar.LENGTH_LONG)
                        .show();
        exportDirPreference.setSummary(getString(R.string.summary_export_dir, JanYoFileUtil.getExportDirPath()));
    }
}
