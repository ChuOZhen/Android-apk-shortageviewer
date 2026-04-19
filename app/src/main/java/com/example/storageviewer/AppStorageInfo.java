package com.example.storageviewer;

/**
 * 应用存储信息数据类
 * 用于封装单个应用的存储占用信息
 */
public class AppStorageInfo {

    private String appName;      // 应用名称
    private String packageName;  // 包名
    private long appSize;        // 应用占用空间（字节）

    public AppStorageInfo(String appName, String packageName, long appSize) {
        this.appName = appName;
        this.packageName = packageName;
        this.appSize = appSize;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getAppSize() {
        return appSize;
    }

    public String getFormattedSize() {
        return StorageInfo.formatSize(appSize);
    }
}