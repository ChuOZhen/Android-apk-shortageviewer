package com.example.storageviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

/**
 * 存储信息列表适配器
 * 负责将存储数据绑定到 RecyclerView 的每个卡片上
 */
public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {

    private Context context;
    private List<StorageInfo> storageList;

    // 构造函数
    public StorageAdapter(Context context) {
        this.context = context;
        this.storageList = new ArrayList<>();
    }

    /**
     * 更新数据列表
     */
    public void setStorageList(List<StorageInfo> list) {
        this.storageList.clear();
        if (list != null) {
            this.storageList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_storage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageInfo info = storageList.get(position);

        // 设置存储名称
        holder.tvStorageName.setText(info.getName());

        // 设置存储路径
        holder.tvStoragePath.setText(context.getString(R.string.path_label) + " " + info.getPath());

        // 设置使用百分比
        int percent = info.getUsagePercent();
        holder.tvUsagePercent.setText(percent + "%");

        // 设置进度条
        holder.progressUsage.setProgress(percent);
        setProgressColor(holder.progressUsage, percent);

        // 设置空间数据
        holder.tvUsedSpace.setText(info.getFormattedUsedSize());
        holder.tvAvailableSpace.setText(info.getFormattedAvailableSize());
        holder.tvTotalSpace.setText(info.getFormattedTotalSize());

        // 根据存储类型设置图标
        setStorageIcon(holder.ivStorageIcon, info.getType());

        // 设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(context, StorageDetailActivity.class);
                intent.putExtra("storage_info", info);
                context.startActivity(intent);
            }
        });
    }

    /**
     * 根据使用百分比和用户设置的阈值设置进度条颜色
     */
    private void setProgressColor(ProgressBar progressBar, int percent) {
        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        int warningThreshold = prefs.getInt("warning_threshold", 50);
        int dangerThreshold = prefs.getInt("danger_threshold", 80);

        int colorRes;
        if (percent < warningThreshold) {
            colorRes = R.color.storage_safe;      // 绿色
        } else if (percent < dangerThreshold) {
            colorRes = R.color.storage_warning;   // 橙色
        } else {
            colorRes = R.color.storage_danger;    // 红色
        }
        progressBar.setProgressTintList(androidx.core.content.ContextCompat.getColorStateList(context, colorRes));
    }

    /**
     * 根据存储类型设置图标
     */
    private void setStorageIcon(ImageView imageView, int type) {
        int iconRes;
        switch (type) {
            case StorageInfo.TYPE_EXTERNAL:
                // SD卡使用专用图标
                iconRes = android.R.drawable.ic_menu_save;
                break;
            case StorageInfo.TYPE_APP:
                // 应用存储使用管理图标
                iconRes = android.R.drawable.ic_menu_manage;
                break;
            case StorageInfo.TYPE_INTERNAL:
            default:
                // 内部存储使用信息图标
                iconRes = android.R.drawable.ic_menu_info_details;
                break;
        }
        imageView.setImageResource(iconRes);

        // 根据类型设置不同的着色
        int colorRes;
        switch (type) {
            case StorageInfo.TYPE_EXTERNAL:
                colorRes = R.color.storage_warning;  // SD卡用橙色
                break;
            case StorageInfo.TYPE_APP:
                colorRes = R.color.storage_danger;    // 应用存储用红色
                break;
            case StorageInfo.TYPE_INTERNAL:
            default:
                colorRes = R.color.primary;           // 内部存储用蓝色
                break;
        }
        imageView.setColorFilter(ContextCompat.getColor(context, colorRes));
    }

    @Override
    public int getItemCount() {
        return storageList.size();
    }

    /**
     * ViewHolder 内部类 - 持有每个卡片的视图引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivStorageIcon;
        TextView tvStorageName;
        TextView tvStoragePath;
        TextView tvUsagePercent;
        ProgressBar progressUsage;
        TextView tvUsedSpace;
        TextView tvAvailableSpace;
        TextView tvTotalSpace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivStorageIcon = itemView.findViewById(R.id.iv_storage_icon);
            tvStorageName = itemView.findViewById(R.id.tv_storage_name);
            tvStoragePath = itemView.findViewById(R.id.tv_storage_path);
            tvUsagePercent = itemView.findViewById(R.id.tv_usage_percent);
            progressUsage = itemView.findViewById(R.id.progress_usage);
            tvUsedSpace = itemView.findViewById(R.id.tv_used_space);
            tvAvailableSpace = itemView.findViewById(R.id.tv_available_space);
            tvTotalSpace = itemView.findViewById(R.id.tv_total_space);
        }
    }
}