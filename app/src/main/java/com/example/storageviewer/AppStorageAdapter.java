package com.example.storageviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppStorageAdapter extends RecyclerView.Adapter<AppStorageAdapter.ViewHolder> {

    private Context context;
    private List<AppStorageInfo> appList;

    public AppStorageAdapter(Context context) {
        this.context = context;
        this.appList = new ArrayList<>();
    }

    public void setAppList(List<AppStorageInfo> list) {
        this.appList.clear();
        if (list != null) {
            this.appList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_storage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppStorageInfo info = appList.get(position);
        holder.tvAppName.setText(info.getAppName());
        holder.tvAppSize.setText(info.getFormattedSize());
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        TextView tvAppSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAppSize = itemView.findViewById(R.id.tv_app_size);
        }
    }
}