package com.example.storageviewer;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * 快照数据访问接口
 * 定义数据库操作
 */
@Dao
public interface SnapshotDao {

    /**
     * 插入一条快照记录
     */
    @Insert
    long insert(StorageSnapshot snapshot);

    /**
     * 插入多条快照记录
     */
    @Insert
    void insertAll(StorageSnapshot... snapshots);

    /**
     * 删除一条快照记录
     */
    @Delete
    void delete(StorageSnapshot snapshot);

    /**
     * 获取指定存储路径的所有快照，按时间倒序排列
     */
    @Query("SELECT * FROM storage_snapshots WHERE storagePath = :path ORDER BY timestamp DESC")
    List<StorageSnapshot> getSnapshotsByPath(String path);

    /**
     * 获取所有快照，按时间倒序排列
     */
    @Query("SELECT * FROM storage_snapshots ORDER BY timestamp DESC")
    List<StorageSnapshot> getAllSnapshots();

    /**
     * 获取最新的快照
     */
    @Query("SELECT * FROM storage_snapshots WHERE storagePath = :path ORDER BY timestamp DESC LIMIT 1")
    StorageSnapshot getLatestSnapshot(String path);

    /**
     * 删除指定存储路径的旧快照（保留最近 N 条）
     */
    @Query("DELETE FROM storage_snapshots WHERE storagePath = :path AND id NOT IN " +
            "(SELECT id FROM storage_snapshots WHERE storagePath = :path " +
            "ORDER BY timestamp DESC LIMIT :keepCount)")
    void deleteOldSnapshots(String path, int keepCount);

    /**
     * 删除所有快照
     */
    @Query("DELETE FROM storage_snapshots")
    void deleteAll();
}