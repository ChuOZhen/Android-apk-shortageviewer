package com.example.storageviewer;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 存储信息获取工具类
 * 用于获取设备的各类存储空间信息
 * 兼容 Android 10+ (API 29+)，无需任何权限
 */
public class StorageHelper {

    /**
     * 获取内部存储信息
     * @param context 上下文
     * @return 内部存储信息对象
     */
    public static StorageInfo getInternalStorage(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long totalSize = totalBlocks * blockSize;
        long availableSize = availableBlocks * blockSize;
        long usedSize = totalSize - availableSize;

        return new StorageInfo(
                "内部存储分区",
                path.getAbsolutePath(),
                totalSize,
                usedSize,
                StorageInfo.TYPE_INTERNAL
        );
    }

    /**
     * 获取共享存储空间（用户可访问的存储区域）
     * @param context 上下文
     * @return 共享存储信息对象
     */
    public static StorageInfo getSharedStorage(Context context) {
        File path = Environment.getExternalStorageDirectory();

        try {
            StatFs stat = new StatFs(path.getPath());

            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            long totalSize = totalBlocks * blockSize;
            long availableSize = availableBlocks * blockSize;
            long usedSize = totalSize - availableSize;

            return new StorageInfo(
                    "内部共享存储",
                    path.getAbsolutePath(),
                    totalSize,
                    usedSize,
                    StorageInfo.TYPE_INTERNAL
            );
        } catch (Exception e) {
            // 如果获取失败，返回空对象
            return new StorageInfo(
                    "内部共享存储",
                    path.getAbsolutePath(),
                    0,
                    0,
                    StorageInfo.TYPE_INTERNAL
            );
        }
    }

    /**
     * 获取外部SD卡信息（如果有）
     * @param context 上下文
     * @return 外部SD卡信息对象，如果没有则返回null
     */
    public static StorageInfo getExternalSdCard(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            List<StorageVolume> volumes = storageManager.getStorageVolumes();

            for (StorageVolume volume : volumes) {
                // 判断是否为可移动存储（SD卡）
                if (volume.isRemovable()) {
                    File sdCardPath = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+
                        sdCardPath = volume.getDirectory();
                    } else {
                        // Android 7-10，通过反射或遍历获取路径
                        String volumePath = getVolumePath(volume);
                        if (volumePath != null) {
                            sdCardPath = new File(volumePath);
                        }
                    }

                    if (sdCardPath != null && sdCardPath.exists()) {
                        try {
                            StatFs stat = new StatFs(sdCardPath.getPath());

                            long blockSize = stat.getBlockSizeLong();
                            long totalBlocks = stat.getBlockCountLong();
                            long availableBlocks = stat.getAvailableBlocksLong();

                            long totalSize = totalBlocks * blockSize;
                            long availableSize = availableBlocks * blockSize;
                            long usedSize = totalSize - availableSize;

                            String volumeName = "SD 卡";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                String desc = volume.getDescription(context);
                                if (desc != null && !desc.isEmpty()) {
                                    volumeName = desc;
                                }
                            }

                            return new StorageInfo(
                                    volumeName,
                                    sdCardPath.getAbsolutePath(),
                                    totalSize,
                                    usedSize,
                                    StorageInfo.TYPE_EXTERNAL
                            );
                        } catch (Exception e) {
                            // 忽略错误
                        }
                    }
                }
            }
        }

        // 兼容旧版本：检查传统的SD卡路径
        File externalSdCard = new File("/storage/extSdCard");
        if (!externalSdCard.exists()) {
            externalSdCard = new File("/storage/sdcard1");
        }

        if (externalSdCard.exists()) {
            try {
                StatFs stat = new StatFs(externalSdCard.getPath());

                long blockSize = stat.getBlockSizeLong();
                long totalBlocks = stat.getBlockCountLong();
                long availableBlocks = stat.getAvailableBlocksLong();

                long totalSize = totalBlocks * blockSize;
                long availableSize = availableBlocks * blockSize;
                long usedSize = totalSize - availableSize;

                return new StorageInfo(
                        "SD 卡",
                        externalSdCard.getAbsolutePath(),
                        totalSize,
                        usedSize,
                        StorageInfo.TYPE_EXTERNAL
                );
            } catch (Exception e) {
                // 忽略错误
            }
        }

        return null; // 没有SD卡
    }

    /**
     * 获取所有存储卷信息
     * @param context 上下文
     * @return 存储信息列表
     */
    public static List<StorageInfo> getAllStorageInfo(Context context) {
        List<StorageInfo> storageList = new ArrayList<>();

        // 1. 添加内部存储分区
        storageList.add(getInternalStorage(context));

        // 2. 添加共享存储
        StorageInfo sharedStorage = getSharedStorage(context);
        if (sharedStorage.getTotalSize() > 0) {
            storageList.add(sharedStorage);
        }

        // 3. 添加外部SD卡（如果有）
        StorageInfo externalSd = getExternalSdCard(context);
        if (externalSd != null) {
            storageList.add(externalSd);
        }

        return storageList;
    }

    /**
     * 通过反射获取存储卷路径（用于 Android 10 及以下）
     */
    private static String getVolumePath(StorageVolume volume) {
        try {
            java.lang.reflect.Method method = volume.getClass().getMethod("getPath");
            return (String) method.invoke(volume);
        } catch (Exception e) {
            return null;
        }
    }
}