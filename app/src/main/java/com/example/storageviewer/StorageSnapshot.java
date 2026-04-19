package com.example.storageviewer;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 存储快照实体类
 * 用于保存某个时间点的存储空间信息
 */
@Entity(tableName = "storage_snapshots")
public class StorageSnapshot {

    @PrimaryKey(autoGenerate = true)
    private long id;                    // 自增主键

    private String storageName;         // 存储名称
    private String storagePath;         // 存储路径
    private long totalSize;             // 总空间
    private long usedSize;              // 已用空间
    private long availableSize;         // 可用空间
    private int usagePercent;           // 使用百分比
    private Date timestamp;             // 记录时间

    // 构造函数
    public StorageSnapshot(String storageName, String storagePath,
                           long totalSize, long usedSize, long availableSize,
                           int usagePercent) {
        this.storageName = storageName;
        this.storagePath = storagePath;
        this.totalSize = totalSize;
        this.usedSize = usedSize;
        this.availableSize = availableSize;
        this.usagePercent = usagePercent;
        this.timestamp = new Date();
    }

    // Getter 和 Setter 方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    public long getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(long availableSize) {
        this.availableSize = availableSize;
    }

    public int getUsagePercent() {
        return usagePercent;
    }

    public void setUsagePercent(int usagePercent) {
        this.usagePercent = usagePercent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // 格式化方法
    public String getFormattedTotalSize() {
        return StorageInfo.formatSize(totalSize);
    }

    public String getFormattedUsedSize() {
        return StorageInfo.formatSize(usedSize);
    }

    public String getFormattedAvailableSize() {
        return StorageInfo.formatSize(availableSize);
    }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(timestamp);
    }
}