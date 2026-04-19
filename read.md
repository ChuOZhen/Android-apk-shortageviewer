# 📱 Android 存储空间查看器

一款功能丰富的 Android 存储空间管理工具，帮助您实时监控设备存储状态，查看各应用占用详情，并记录存储变化历史。

[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)](https://android-arsenal.com/api?level=29)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://www.java.com)

---

## ✨ 主要功能

### 📊 存储总览
- 实时显示内部存储、SD 卡空间使用情况
- 进度条直观展示使用率（绿/橙/红三色预警）
- 显示各存储分区的挂载路径
- 下拉刷新即时更新数据

### 📈 详情分析
- 点击存储卡片进入详情页
- 饼图可视化展示已用/可用空间比例
- 各应用占用空间排行列表
- 支持按大小、名称多种方式排序

### 📅 历史记录
- 自动/手动记录存储快照
- 可自定义记录间隔（1/6/12/24小时）
- 选择两条记录对比存储变化
- WorkManager 实现，不常驻后台

### 🎨 个性化设置
- 主题颜色自定义（蓝/绿/紫/橙/红）
- 预警阈值自定义（设置黄/红预警线）
- 深色模式自动适配

---

## 📸 应用截图

| 主界面 | 详情页 | 历史记录 |
|:---:|:---:|:---:|
| ![主界面](screenshots/main.png) | ![详情页](screenshots/detail.png) | ![历史记录](screenshots/history.png) |

---

## 🛠️ 技术栈

| 技术 | 用途 |
|-----|------|
| **Java** | 开发语言 |
| **Material Design 3** | UI 设计规范 |
| **Room Database** | 本地数据持久化 |
| **WorkManager** | 后台定时任务 |
| **MPAndroidChart** | 饼图绘制 |
| **StatFs / StorageStatsManager** | 存储信息获取 |

---

## 📱 系统要求

- **最低版本**：Android 10 (API 29)
- **目标版本**：Android 14 (API 34)
- **权限要求**：
  - `PACKAGE_USAGE_STATS`（可选，用于查看应用详情）

---

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 8 或更高版本
- Gradle 8.0+

### 构建步骤

1. 克隆仓库
```bash
git clone https://github.com/您的用户名/StorageViewer.git
