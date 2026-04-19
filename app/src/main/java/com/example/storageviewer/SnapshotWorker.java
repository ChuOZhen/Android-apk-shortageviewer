package com.example.storageviewer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

/**
 * 后台快照记录任务
 * 由 WorkManager 定期调度执行
 */
public class SnapshotWorker extends Worker {

    public SnapshotWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            // 获取所有存储信息
            List<StorageInfo> storageList = StorageHelper.getAllStorageInfo(context);

            // 获取数据库实例
            AppDatabase db = AppDatabase.getInstance(context);
            SnapshotDao dao = db.snapshotDao();

            // 为每个存储卷保存快照
            for (StorageInfo info : storageList) {
                StorageSnapshot snapshot = new StorageSnapshot(
                        info.getName(),
                        info.getPath(),
                        info.getTotalSize(),
                        info.getUsedSize(),
                        info.getAvailableSize(),
                        info.getUsagePercent()
                );

                dao.insert(snapshot);

                // 每个存储路径只保留最近 100 条记录，防止数据库过大
                dao.deleteOldSnapshots(info.getPath(), 100);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}