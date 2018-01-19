package pw.janyo.janyoshare.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.classes.InstallAPP;
import vip.mystery0.tools.fileUtil.FileUtil;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private Context context;
    private List<InstallAPP> installAPPList;
    private RequestOptions options = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE);

    public AppAdapter(Context context, List<InstallAPP> installAPPList) {
        this.context = context;
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
                                doSelect(installAPP, which);
                            }
                        })
                        .show();
            }
        });
    }

    private void doSelect(InstallAPP installAPP, int choose) {

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

        public ViewHolder(View itemView) {
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
