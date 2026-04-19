package com.example.storageviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 历史记录列表适配器
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<StorageSnapshot> snapshotList;
    private Set<Integer> selectedPositions;
    private boolean isSelectionMode = false;
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public HistoryAdapter(Context context) {
        this.context = context;
        this.snapshotList = new ArrayList<>();
        this.selectedPositions = new HashSet<>();
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setSnapshotList(List<StorageSnapshot> list) {
        this.snapshotList.clear();
        if (list != null) {
            this.snapshotList.addAll(list);
        }
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedPositions.size());
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public List<StorageSnapshot> getSelectedSnapshots() {
        List<StorageSnapshot> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos < snapshotList.size()) {
                selected.add(snapshotList.get(pos));
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageSnapshot snapshot = snapshotList.get(position);

        holder.tvTime.setText(snapshot.getFormattedTimestamp());
        holder.tvUsed.setText("已用: " + snapshot.getFormattedUsedSize());
        holder.tvTotal.setText("总计: " + snapshot.getFormattedTotalSize());
        holder.tvPercent.setText(snapshot.getUsagePercent() + "%");

        // 设置百分比颜色
        int percent = snapshot.getUsagePercent();
        int colorRes;
        if (percent < 50) {
            colorRes = R.color.storage_safe;
        } else if (percent < 80) {
            colorRes = R.color.storage_warning;
        } else {
            colorRes = R.color.storage_danger;
        }
        holder.tvPercent.setTextColor(androidx.core.content.ContextCompat.getColor(context, colorRes));

        // 处理选择模式
        if (isSelectionMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(selectedPositions.contains(position));
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                setSelectionMode(true);
                toggleSelection(position);
                return true;
            }
            return false;
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedPositions.size());
        }
    }

    @Override
    public int getItemCount() {
        return snapshotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView tvTime;
        TextView tvUsed;
        TextView tvTotal;
        TextView tvPercent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_select);
            tvTime = itemView.findViewById(R.id.tv_history_time);
            tvUsed = itemView.findViewById(R.id.tv_history_used);
            tvTotal = itemView.findViewById(R.id.tv_history_total);
            tvPercent = itemView.findViewById(R.id.tv_history_percent);
        }
    }
}