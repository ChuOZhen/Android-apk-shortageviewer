package com.example.storageviewer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * 应用数据库类
 * 使用 Room 持久化存储快照数据
 */
@Database(entities = {StorageSnapshot.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "storage_history.db";
    private static AppDatabase instance;

    /**
     * 获取 SnapshotDao 实例
     */
    public abstract SnapshotDao snapshotDao();

    /**
     * 获取数据库单例
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}