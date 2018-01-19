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
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.activity.SettingsActivity;
import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import pw.janyo.janyoshare.util.Settings;
import vip.mystery0.tools.logs.Logs;

public class SettingsPreferenceFragment extends PreferenceFragment {
    private static final String TAG = "SettingsPreferenceFragment";
    private static final int PERMISSION_REQUEST_CODE = 233;

    private CoordinatorLayout coordinatorLayout;

    private SwitchPreference isAutoCleanPreference;
    private Preference exportDirPreference;
    private SwitchPreference isCustomFormatPreference;
    private Preference customRenameFormatPreference;

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
        isAutoCleanPreference = (SwitchPreference) findPreferenceById(R.string.key_auto_clean);
        exportDirPreference = findPreferenceById(R.string.key_export_dir);
        isCustomFormatPreference = (SwitchPreference) findPreferenceById(R.string.key_custom_format);
        customRenameFormatPreference = findPreferenceById(R.string.key_custom_rename_format);
    }

    private void initialization() {
        isAutoCleanPreference.setChecked(Settings.isAutoClean());
        if (Settings.isAutoClean())
            isAutoCleanPreference.setSummary(R.string.summary_auto_clean_on);
        else
            isAutoCleanPreference.setSummary(R.string.summary_auto_clean_off);
        exportDirPreference.setSummary(getString(R.string.summary_export_dir, JanYoFileUtil.getExportDirPath()));
        isCustomFormatPreference.setChecked(Settings.isCustomFormat());
        if (Settings.isCustomFormat()) {
            InstallAPP test = new InstallAPP();
            test.setName(getString(R.string.app_name));
            test.setVersionName(getString(R.string.app_version_name));
            test.setVersionCode(Integer.parseInt(getString(R.string.app_version_code)));
            test.setPackageName(getString(R.string.app_package_name));
            customRenameFormatPreference.setSummary(getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.getRenameFormat())));
            customRenameFormatPreference.setEnabled(true);
        } else {
            customRenameFormatPreference.setSummary(null);
            customRenameFormatPreference.setEnabled(false);
        }
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
        isCustomFormatPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isCustomFormat = !isCustomFormatPreference.isChecked();
                if (isCustomFormat) {
                    InstallAPP test = new InstallAPP();
                    test.setName(getString(R.string.app_name));
                    test.setVersionName(getString(R.string.app_version_name));
                    test.setVersionCode(Integer.parseInt(getString(R.string.app_version_code)));
                    test.setPackageName(getString(R.string.app_package_name));
                    customRenameFormatPreference.setSummary(getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.getRenameFormat())));
                    customRenameFormatPreference.setEnabled(true);
                } else {
                    customRenameFormatPreference.setSummary(null);
                    customRenameFormatPreference.setEnabled(false);
                }
                Settings.setCustomFormat(isCustomFormat);
                return true;
            }
        });
        customRenameFormatPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_rename_format, new LinearLayout(getActivity()), false);
                final TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);
                final TextView showText = view.findViewById(R.id.show);
                //noinspection ConstantConditions
                textInputLayout.getEditText().setText(Settings.getRenameFormat());
                final InstallAPP test = new InstallAPP();
                test.setName(getString(R.string.app_name));
                test.setVersionName(getString(R.string.app_version_name));
                test.setVersionCode(Integer.parseInt(getString(R.string.app_version_code)));
                test.setPackageName(getString(R.string.app_package_name));
                showText.setText(getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.getRenameFormat())));
                textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String format = s.toString();
                        showText.setText(getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, format)));
                    }
                });
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_custom_rename_format)
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String format = textInputLayout.getEditText().getText().toString();
                                Settings.setRenameFormat(format);
                                customRenameFormatPreference.setSummary(getString(R.string.summary_custom_format, JanYoFileUtil.formatName(test, Settings.getRenameFormat())));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(R.string.action_insert, null)
                        .create();
                dialog.show();
                if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String temp = textInputLayout.getEditText().getText().toString() + '%';
                            textInputLayout.getEditText().setText(temp);
                            textInputLayout.getEditText().setSelection(temp.length());
                        }
                    });
                }
                return true;
            }
        });
    }

    private Preference findPreferenceById(@StringRes int id) {
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
