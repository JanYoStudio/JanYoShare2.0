package pw.janyo.janyoshare.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import pw.janyo.janyoshare.util.Settings;
import vip.mystery0.tools.fileUtil.FileUtil;
import vip.mystery0.tools.logs.Logs;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private static final String TAG = "AppAdapter";
    private Context context;
    private CoordinatorLayout coordinatorLayout;
    private List<InstallAPP> installAPPList;
    private RequestOptions options = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE);

    public AppAdapter(Context context, List<InstallAPP> installAPPList) {
        this.context = context;
        this.coordinatorLayout = ((Activity) context).findViewById(R.id.coordinatorLayout);
        this.installAPPList = installAPPList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final InstallAPP installAPP = installAPPList.get(position);
        holder.textViewName.setText(installAPP.getName());
        holder.textViewPackageName.setText(installAPP.getPackageName());
        holder.textViewVersionName.setText(installAPP.getVersionName());
        holder.textViewSize.setText(FileUtil.INSTANCE.FormatFileSize(installAPP.getSize()));
        if (installAPP.getIconPath() != null)
            Glide.with(context).load(installAPP.getIconPath()).apply(options).into(holder.imageView);
        else
            holder.imageView.setImageDrawable(installAPP.getIcon());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.imageView.setVisibility(View.GONE);
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(true);
            }
        });
//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            }
//        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_dialog_select_operation)
                        .setItems(R.array.copyOperation, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exportThen(installAPP, which);
                            }
                        })
                        .show();
            }
        });
    }

    private void exportThen(final InstallAPP installAPP, final int choose) {
        if (choose == 6) {
            copyInfoToClipboard(installAPP);
            return;
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> subscriber) throws Exception {
                subscriber.onNext(JanYoFileUtil.exportAPK(installAPP));
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    private int code;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Logs.i(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        code = integer;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.wtf(TAG, "exportThen: onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        switch (code) {
                            case JanYoFileUtil.DIR_NOT_EXIST:
                                Snackbar.make(coordinatorLayout, R.string.hint_export_dir_create_failed, Snackbar.LENGTH_LONG)
                                        .show();
                                break;
                            case JanYoFileUtil.FILE_NOT_EXIST:
                                Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
                                        .show();
                                break;
                            case JanYoFileUtil.ERROR:
                                Snackbar.make(coordinatorLayout, R.string.hint_export_failed, Snackbar.LENGTH_LONG)
                                        .show();
                                break;
                            case JanYoFileUtil.DONE:
                                doSomething(installAPP, choose);
                                break;
                        }
                    }
                });
    }

    private void doSomething(final InstallAPP installAPP, int whatToDo) {
        switch (whatToDo) {
            case 0://仅提取
                Snackbar.make(coordinatorLayout, context.getString(R.string.hint_export_done, JanYoFileUtil.getExportDirPath()), Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case 1://提取并分享
                JanYoFileUtil.share(context, JanYoFileUtil.getExportFile(installAPP));
                break;
            case 2://重命名后分享
                String oldFileName = JanYoFileUtil.formatName(installAPP, Settings.getRenameFormat());
                View renameFileNameView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, new TextInputLayout((context)), false);
                final TextInputLayout renameFileNameTextInputLayout = renameFileNameView.findViewById(R.id.layout);
                renameFileNameTextInputLayout.setHint(oldFileName);
                //noinspection ConstantConditions
                renameFileNameTextInputLayout.getEditText().setText(oldFileName);
                new AlertDialog.Builder(context)
                        .setTitle(" ")
                        .setView(renameFileNameView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String newName = renameFileNameTextInputLayout.getEditText().getText().toString();
                                int code = JanYoFileUtil.renameFile(installAPP, newName, false);
                                switch (code) {
                                    case JanYoFileUtil.FILE_NOT_EXIST:
                                        Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
                                                .show();
                                        break;
                                    case JanYoFileUtil.FILE_EXIST:
                                        Snackbar.make(coordinatorLayout, R.string.hint_file_exist, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.action_redo, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (JanYoFileUtil.renameFile(installAPP, newName, true) == JanYoFileUtil.DONE)
                                                            JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.getSourceDir())));
                                                        else
                                                            Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
                                                                    .show();
                                                    }
                                                })
                                                .addCallback(new Snackbar.Callback() {
                                                    @Override
                                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                                        if (event != DISMISS_EVENT_ACTION)
                                                            JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.getSourceDir())));
                                                    }
                                                })
                                                .show();
                                        break;
                                    case JanYoFileUtil.ERROR:
                                        Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
                                                .show();
                                        break;
                                    case JanYoFileUtil.DONE:
                                        JanYoFileUtil.share(context, JanYoFileUtil.getFile(newName + JanYoFileUtil.appendExtensionFileName(installAPP.getSourceDir())));
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case 3://重命名扩展名
                String oldExtensionName = JanYoFileUtil.getExtensionFileName(installAPP.getSourceDir());
                View renameExtensionView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, new TextInputLayout((context)), false);
                final TextInputLayout renameExtensionTextInputLayout = renameExtensionView.findViewById(R.id.layout);
                renameExtensionTextInputLayout.setHint(oldExtensionName);
                //noinspection ConstantConditions
                renameExtensionTextInputLayout.getEditText().setText(oldExtensionName);
                new AlertDialog.Builder(context)
                        .setTitle(" ")
                        .setView(renameExtensionView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String newExtensionName = renameExtensionTextInputLayout.getEditText().getText().toString();
                                int code = JanYoFileUtil.renameExtension(installAPP, newExtensionName, false);
                                switch (code) {
                                    case JanYoFileUtil.FILE_NOT_EXIST:
                                        Snackbar.make(coordinatorLayout, R.string.hint_source_file_not_exist, Snackbar.LENGTH_LONG)
                                                .show();
                                        break;
                                    case JanYoFileUtil.FILE_EXIST:
                                        Snackbar.make(coordinatorLayout, R.string.hint_file_exist, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.action_redo, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (JanYoFileUtil.renameExtension(installAPP, newExtensionName, true) == JanYoFileUtil.DONE)
                                                            JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.getRenameFormat()) + (newExtensionName.length() == 0 ? "" : '.' + newExtensionName)));
                                                        else
                                                            Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
                                                                    .show();
                                                    }
                                                })
                                                .addCallback(new Snackbar.Callback() {
                                                    @Override
                                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                                        if (event != DISMISS_EVENT_ACTION)
                                                            JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.getRenameFormat()) + (newExtensionName.length() == 0 ? "" : '.' + newExtensionName)));
                                                    }
                                                })
                                                .show();
                                        break;
                                    case JanYoFileUtil.ERROR:
                                        Snackbar.make(coordinatorLayout, R.string.hint_rename_failed, Snackbar.LENGTH_LONG)
                                                .show();
                                        break;
                                    case JanYoFileUtil.DONE:
                                        JanYoFileUtil.share(context, JanYoFileUtil.getFile(JanYoFileUtil.formatName(installAPP, Settings.getRenameFormat()) + (newExtensionName.length() == 0 ? "" : '.' + newExtensionName)));
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case 4://和数据包一起提取分享
                final List<File> shareList = new ArrayList<>();
                shareList.add(JanYoFileUtil.getExportFile(installAPP));
                List<File> obbList = JanYoFileUtil.checkObb(installAPP.getPackageName());
                shareList.addAll(obbList);
                if (obbList.size() == 0)
                    Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb_no), Snackbar.LENGTH_SHORT)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    JanYoFileUtil.doShareFile(context, shareList);
                                }
                            })
                            .show();
                else if (obbList.size() == 1)
                    Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb, obbList.size()), Snackbar.LENGTH_SHORT)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    JanYoFileUtil.doShareFile(context, shareList);
                                }
                            })
                            .show();
                else
                    Snackbar.make(coordinatorLayout, context.getString(R.string.hint_warning_check_obb_s, obbList.size()), Snackbar.LENGTH_SHORT)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    JanYoFileUtil.doShareFile(context, shareList);
                                }
                            })
                            .show();
                break;
            case 5://面对面分享
                Snackbar.make(coordinatorLayout, R.string.hint_service_unavailable, Snackbar.LENGTH_LONG)
                        .show();
                break;
        }
    }

    private void copyInfoToClipboard(final InstallAPP installAPP) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_dialog_select_copy_info)
                .setItems(R.array.copyInfo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                copyToClipboard(installAPP.getName(), installAPP.getName());
                                break;
                            case 1:
                                copyToClipboard(installAPP.getName(), installAPP.getPackageName());
                                break;
                            case 2:
                                copyToClipboard(installAPP.getName(), installAPP.getVersionName());
                                break;
                            case 3:
                                copyToClipboard(installAPP.getName(), String.valueOf(installAPP.getVersionCode()));
                                break;
                        }
                    }
                })
                .show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
            Snackbar.make(coordinatorLayout, R.string.hint_copy_info_done, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return installAPPList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CheckBox checkBox;
        ImageView imageView;
        TextView textViewName;
        TextView textViewPackageName;
        TextView textViewVersionName;
        TextView textViewSize;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.checkBox = itemView.findViewById(R.id.checkBox);
            this.imageView = itemView.findViewById(R.id.app_icon);
            this.textViewName = itemView.findViewById(R.id.app_name);
            this.textViewPackageName = itemView.findViewById(R.id.app_package_name);
            this.textViewVersionName = itemView.findViewById(R.id.app_version_name);
            this.textViewSize = itemView.findViewById(R.id.app_size);
        }
    }
}
