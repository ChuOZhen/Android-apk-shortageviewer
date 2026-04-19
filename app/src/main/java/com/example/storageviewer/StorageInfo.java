package com.example.storageviewer;

import java.text.DecimalFormat;

/**
 * 存储信息数据类
 * 用于封装单个存储分区的信息
 */
public class StorageInfo implements java.io.Serializable {

    // 存储类型常量
    public static final int TYPE_INTERNAL = 0;   // 内部存储
    public static final int TYPE_EXTERNAL = 1;   // 外部SD卡
    public static final int TYPE_APP = 2;        // 应用存储分区

    private String name;           // 存储名称（如"内部存储"）
    private String path;           // 存储路径
    private long totalSize;        // 总空间（字节）
    private long usedSize;         // 已用空间（字节）
    private long availableSize;    // 可用空间（字节）
    private int type;              // 存储类型

    // 构造函数
    public StorageInfo(String name, String path, long totalSize, long usedSize, int type) {
        this.name = name;
        this.path = path;
        this.totalSize = totalSize;
        this.usedSize = usedSize;
        this.availableSize = totalSize - usedSize;
        this.type = type;
    }

    // Getter 方法
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public long getAvailableSize() {
        return availableSize;
    }

    public int getType() {
        return type;
    }

    /**
     * 计算使用百分比
     * @return 使用百分比（0-100）
     */
    public int getUsagePercent() {
        if (totalSize == 0) {
            return 0;
        }
        return (int) ((usedSize * 100) / totalSize);
    }

    /**
     * 格式化字节大小，转换为合适的单位（GB/MB/KB）
     * @param size 字节数
     * @return 格式化后的字符串，如 "128 GB"
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }

        double value = size / Math.pow(1024, digitGroups);
        DecimalFormat df;

        if (value < 10) {
            df = new DecimalFormat("#.##");  // 小于10时显示两位小数
        } else if (value < 100) {
            df = new DecimalFormat("#.#");   // 小于100时显示一位小数
        } else {
            df = new DecimalFormat("#");      // 大于等于100时不显示小数
        }

        return df.format(value) + " " + units[digitGroups];
    }

    /**
     * 获取格式化的总空间字符串
     */
    public String getFormattedTotalSize() {
        return formatSize(totalSize);
    }

    /**
     * 获取格式化的已用空间字符串
     */
    public String getFormattedUsedSize() {
        return formatSize(usedSize);
    }

    /**
     * 获取格式化的可用空间字符串
     */
    public String getFormattedAvailableSize() {
        return formatSize(availableSize);
    }
}