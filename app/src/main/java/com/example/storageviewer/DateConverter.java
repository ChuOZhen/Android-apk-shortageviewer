package com.example.storageviewer;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Room 数据库类型转换器
 * 用于将 Date 类型转换为 Long 时间戳存储
 */
public class DateConverter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}