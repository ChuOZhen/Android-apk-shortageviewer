package com.example.storageviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * 主界面 Activity
 * 应用入口，显示存储空间信息
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StorageAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabHistory;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();

        // 应用保存的主题颜色
        applyThemeColor();

        // 设置 RecyclerView
        setupRecyclerView();

        // 设置下拉刷新
        setupSwipeRefresh();

        // 加载存储数据
        loadStorageData();

        // 设置历史记录按钮点击事件
        fabHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        // 设置工具栏
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 获取 RecyclerView 和 SwipeRefreshLayout
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        fabHistory = findViewById(R.id.fab_history);
    }

    /**
     * 应用主题颜色
     */
    private void applyThemeColor() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int themeColor = prefs.getInt("theme_color", android.graphics.Color.parseColor("#2196F3"));

        // 设置 Toolbar 颜色
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
        }

        // 设置 FAB 颜色
        if (fabHistory != null) {
            fabHistory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor));
        }

        // 设置状态栏颜色
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(darkenColor(themeColor));
        }
    }

    /**
     * 颜色变暗（用于状态栏）
     */
    private int darkenColor(int color) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return android.graphics.Color.HSVToColor(hsv);
    }

    /**
     * 设置 RecyclerView
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StorageAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 设置下拉刷新
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadStorageData();
            }
        });
    }

    /**
     * 加载存储数据
     */
    private void loadStorageData() {
        swipeRefresh.setRefreshing(true);

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<StorageInfo> storageList = StorageHelper.getAllStorageInfo(MainActivity.this);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 应用排序
                            applySorting(storageList);
                            adapter.setStorageList(storageList);
                            swipeRefresh.setRefreshing(false);

                            if (storageList.isEmpty()) {
                                Toast.makeText(MainActivity.this,
                                        "未检测到存储设备", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "已加载 " + storageList.size() + " 个存储卷", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).start();

        } catch (Exception e) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建选项菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * 菜单项点击处理
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showIntervalDialog();
            return true;
        } else if (id == R.id.action_record_now) {
            recordSnapshotNow();
            return true;
        } else if (id == R.id.action_theme) {
            showThemeDialog();
            return true;
        } else if (id == R.id.action_threshold) {
            showThresholdDialog();
            return true;
        } else if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示记录间隔选择对话框
     */
    private void showIntervalDialog() {
        String[] intervals = {"1小时", "6小时", "12小时", "24小时"};

        new AlertDialog.Builder(this)
                .setTitle("选择记录间隔")
                .setItems(intervals, (dialog, which) -> {
                    long intervalHours;
                    switch (which) {
                        case 0: intervalHours = 1; break;
                        case 1: intervalHours = 6; break;
                        case 2: intervalHours = 12; break;
                        default: intervalHours = 24; break;
                    }
                    scheduleSnapshotWorker(intervalHours);
                    Toast.makeText(this, "已设置每 " + intervalHours + " 小时记录一次", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 调度后台定时任务
     */
    private void scheduleSnapshotWorker(long intervalHours) {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        SnapshotWorker.class,
                        intervalHours,
                        java.util.concurrent.TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "storage_snapshot",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );

        Toast.makeText(this, "定时记录已启动", Toast.LENGTH_SHORT).show();
    }

    /**
     * 立即记录一次快照
     */
    private void recordSnapshotNow() {
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(SnapshotWorker.class)
                        .build();

        WorkManager.getInstance(this).enqueue(workRequest);

        Toast.makeText(this, "正在记录当前存储状态...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示主题颜色选择对话框
     */
    private void showThemeDialog() {
        String[] themes = {"蓝色（默认）", "绿色", "紫色", "橙色", "红色"};
        int[] colorValues = {
                android.graphics.Color.parseColor("#2196F3"),
                android.graphics.Color.parseColor("#4CAF50"),
                android.graphics.Color.parseColor("#9C27B0"),
                android.graphics.Color.parseColor("#FF9800"),
                android.graphics.Color.parseColor("#F44336")
        };

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int currentColor = prefs.getInt("theme_color", colorValues[0]);

        int checkedItem = 0;
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i] == currentColor) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择主题颜色")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedColor = colorValues[which];
                    saveThemeColor(selectedColor);
                    dialog.dismiss();

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("主题已更改")
                            .setMessage("需要重启应用才能完全应用新主题。是否立即重启？")
                            .setPositiveButton("重启", (d, w) -> {
                                Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                System.exit(0);
                            })
                            .setNegativeButton("稍后", null)
                            .show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveThemeColor(int color) {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putInt("theme_color", color).apply();
    }

    /**
     * 显示预警阈值设置对话框
     */
    private void showThresholdDialog() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int warningThreshold = prefs.getInt("warning_threshold", 50);
        int dangerThreshold = prefs.getInt("danger_threshold", 80);

        // 创建自定义布局
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        // 警告阈值标签
        android.widget.TextView tvWarning = new android.widget.TextView(this);
        tvWarning.setText("警告阈值（%）：超过此值显示橙色");
        tvWarning.setTextSize(14);
        tvWarning.setTextColor(getResources().getColor(R.color.text_secondary));
        layout.addView(tvWarning);

        // 警告阈值输入框
        android.widget.EditText etWarning = new android.widget.EditText(this);
        etWarning.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etWarning.setText(String.valueOf(warningThreshold));
        layout.addView(etWarning);

        // 危险阈值标签
        android.widget.TextView tvDanger = new android.widget.TextView(this);
        tvDanger.setText("危险阈值（%）：超过此值显示红色");
        tvDanger.setTextSize(14);
        tvDanger.setTextColor(getResources().getColor(R.color.text_secondary));
        tvDanger.setPadding(0, 32, 0, 0);
        layout.addView(tvDanger);

        // 危险阈值输入框
        android.widget.EditText etDanger = new android.widget.EditText(this);
        etDanger.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etDanger.setText(String.valueOf(dangerThreshold));
        layout.addView(etDanger);

        new AlertDialog.Builder(this)
                .setTitle("设置预警阈值")
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    try {
                        int warning = Integer.parseInt(etWarning.getText().toString());
                        int danger = Integer.parseInt(etDanger.getText().toString());

                        // 验证：警告值必须小于危险值
                        if (warning >= danger) {
                            Toast.makeText(this, "警告阈值必须小于危险阈值", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 验证：值必须在 0-100 之间
                        if (warning < 0 || warning > 100 || danger < 0 || danger > 100) {
                            Toast.makeText(this, "阈值必须在 0-100 之间", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 保存设置
                        prefs.edit()
                                .putInt("warning_threshold", warning)
                                .putInt("danger_threshold", danger)
                                .apply();

                        Toast.makeText(this, "预警阈值已保存", Toast.LENGTH_SHORT).show();

                        // 刷新数据显示新颜色
                        loadStorageData();

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示排序方式选择对话框
     */
    private void showSortDialog() {
        String[] sortOptions = {"按使用率（从高到低）", "按使用率（从低到高）", "按名称（A-Z）", "按总空间（从大到小）"};

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int currentSort = prefs.getInt("sort_type", 0);

        new AlertDialog.Builder(this)
                .setTitle("选择排序方式")
                .setSingleChoiceItems(sortOptions, currentSort, (dialog, which) -> {
                    prefs.edit().putInt("sort_type", which).apply();
                    dialog.dismiss();

                    // 重新加载并排序数据
                    loadStorageData();

                    String sortName = sortOptions[which];
                    Toast.makeText(this, "已设置：" + sortName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 应用排序
     */
    private void applySorting(List<StorageInfo> list) {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int sortType = prefs.getInt("sort_type", 0);

        switch (sortType) {
            case 0: // 使用率从高到低
                java.util.Collections.sort(list, (a, b) ->
                        Integer.compare(b.getUsagePercent(), a.getUsagePercent()));
                break;
            case 1: // 使用率从低到高
                java.util.Collections.sort(list, (a, b) ->
                        Integer.compare(a.getUsagePercent(), b.getUsagePercent()));
                break;
            case 2: // 按名称 A-Z
                java.util.Collections.sort(list, (a, b) ->
                        a.getName().compareTo(b.getName()));
                break;
            case 3: // 按总空间从大到小
                java.util.Collections.sort(list, (a, b) ->
                        Long.compare(b.getTotalSize(), a.getTotalSize()));
                break;
        }
    }

}