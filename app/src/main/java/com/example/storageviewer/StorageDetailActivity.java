package com.example.storageviewer;

import android.app.AppOpsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储详情页 Activity
 * 用于显示单个存储分区的详细信息和图表
 */
public class StorageDetailActivity extends AppCompatActivity {

    // UI 组件
    private MaterialToolbar toolbar;
    private TextView tvPath;
    private TextView tvUsage;
    private ProgressBar progressBar;
    private TextView tvUsed;
    private TextView tvAvailable;
    private TextView tvTotal;
    private PieChart pieChart;

    // 存储信息
    private StorageInfo storageInfo;

    private RecyclerView recyclerAppList;
    private AppStorageAdapter appAdapter;
    private static final int REQUEST_USAGE_STATS = 1001;

    // 排序相关
    private int appSortType = 0; // 0:按大小降序, 1:按大小升序, 2:按名称A-Z, 3:按名称Z-A

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_detail);

        // 获取传递过来的存储信息
        storageInfo = (StorageInfo) getIntent().getSerializableExtra("storage_info");

        // 初始化视图
        initViews();

        // 设置工具栏
        setupToolbar();

        // 显示存储信息
        displayStorageInfo();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_detail);
        tvPath = findViewById(R.id.tv_detail_path);
        tvUsage = findViewById(R.id.tv_detail_usage);
        progressBar = findViewById(R.id.progress_detail);
        tvUsed = findViewById(R.id.tv_detail_used);
        tvAvailable = findViewById(R.id.tv_detail_available);
        tvTotal = findViewById(R.id.tv_detail_total);
        pieChart = findViewById(R.id.pie_chart);
        recyclerAppList = findViewById(R.id.recycler_app_list);
    }

    /**
     * 设置工具栏
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(storageInfo != null ? storageInfo.getName() : "存储详情");
        }
    }

    /**
     * 显示存储信息
     */
    private void displayStorageInfo() {
        if (storageInfo == null) {
            return;
        }

        // 设置路径
        tvPath.setText(getString(R.string.path_label) + " " + storageInfo.getPath());

        // 设置使用率
        int percent = storageInfo.getUsagePercent();
        tvUsage.setText("使用率：" + percent + "%");

        // 设置进度条
        progressBar.setProgress(percent);
        setProgressColor(progressBar, percent);

        // 设置空间数据
        tvUsed.setText(storageInfo.getFormattedUsedSize());
        tvAvailable.setText(storageInfo.getFormattedAvailableSize());
        tvTotal.setText(storageInfo.getFormattedTotalSize());

        // 设置饼图
        setupPieChart();

        // 初始化应用列表
        setupAppList();
    }

    /**
     * 初始化应用列表
     */
    private void setupAppList() {
        recyclerAppList.setLayoutManager(new LinearLayoutManager(this));
        appAdapter = new AppStorageAdapter(this);
        recyclerAppList.setAdapter(appAdapter);

        // 检查权限并加载应用数据
        if (hasUsageStatsPermission()) {
            loadAppStorageInfo();
        } else {
            showPermissionDialog();
        }
    }

    /**
     * 检查是否有使用情况访问权限
     */
    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    getPackageName()
            );
            return mode == AppOpsManager.MODE_ALLOWED;
        }
        return true;
    }

    /**
     * 显示权限请求对话框
     */
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要权限")
                .setMessage("为了显示各应用的存储占用情况，需要您授权使用情况访问权限。")
                .setPositiveButton("去授权", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivityForResult(intent, REQUEST_USAGE_STATS);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    Toast.makeText(this, "无法获取应用存储信息", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USAGE_STATS) {
            if (hasUsageStatsPermission()) {
                loadAppStorageInfo();
                Toast.makeText(this, "权限已获取", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未获取权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 加载应用存储信息
     */
    private void loadAppStorageInfo() {
        new Thread(() -> {
            List<AppStorageInfo> appList = new ArrayList<>();

            if (storageInfo == null) {
                sortAppList(appList);
                runOnUiThread(() -> appAdapter.setAppList(appList));
                return;
            }

            try {
                StorageStatsManager storageStatsManager =
                        (StorageStatsManager) getSystemService(STORAGE_STATS_SERVICE);
                PackageManager packageManager = getPackageManager();
                List<ApplicationInfo> installedApps =
                        packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo appInfo : installedApps) {
                    try {
                        StorageStats stats = storageStatsManager.queryStatsForUid(
                                StorageManager.UUID_DEFAULT,
                                appInfo.uid
                        );

                        long appSize = stats.getAppBytes() + stats.getDataBytes() + stats.getCacheBytes();

                        if (appSize > 0) {
                            String appName = packageManager.getApplicationLabel(appInfo).toString();
                            appList.add(new AppStorageInfo(appName, appInfo.packageName, appSize));
                        }
                    } catch (Exception e) {
                        // 忽略单个应用的错误
                    }
                }

                // 应用用户选择的排序方式
                sortAppList(appList);

            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                appAdapter.setAppList(appList);
                if (appList.isEmpty()) {
                    Toast.makeText(StorageDetailActivity.this, "暂无应用数据", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * 根据使用百分比和用户设置的阈值设置进度条颜色
     */
    private void setProgressColor(ProgressBar progressBar, int percent) {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int warningThreshold = prefs.getInt("warning_threshold", 50);
        int dangerThreshold = prefs.getInt("danger_threshold", 80);

        int colorRes;
        if (percent < warningThreshold) {
            colorRes = R.color.storage_safe;
        } else if (percent < dangerThreshold) {
            colorRes = R.color.storage_warning;
        } else {
            colorRes = R.color.storage_danger;
        }
        progressBar.setProgressTintList(androidx.core.content.ContextCompat.getColorStateList(this, colorRes));
    }

    /**
     * 设置饼图
     */
    private void setupPieChart() {
        if (storageInfo == null) return;

        long usedSize = storageInfo.getUsedSize();
        long availableSize = storageInfo.getAvailableSize();

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(usedSize, "已用空间"));
        entries.add(new PieEntry(availableSize, "可用空间"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.storage_danger));
        colors.add(ContextCompat.getColor(this, R.color.storage_safe));
        dataSet.setColors(colors);

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_primary));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return StorageInfo.formatSize((long) value);
            }
        });

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(storageInfo.getName() + "\n" + storageInfo.getUsagePercent() + "% 已用");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.text_primary));
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.text_primary));
        pieChart.setEntryLabelTextSize(12f);

        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(14f);
        pieChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        pieChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setDrawInside(false);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_app_sort) {
            showAppSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示应用排序对话框
     */
    private void showAppSortDialog() {
        String[] sortOptions = {"按大小（从大到小）", "按大小（从小到大）", "按名称（A-Z）", "按名称（Z-A）"};

        new AlertDialog.Builder(this)
                .setTitle("应用排序方式")
                .setSingleChoiceItems(sortOptions, appSortType, (dialog, which) -> {
                    appSortType = which;
                    dialog.dismiss();
                    loadAppStorageInfo();
                    Toast.makeText(this, "已设置：" + sortOptions[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 对应用列表进行排序
     */
    private void sortAppList(List<AppStorageInfo> appList) {
        switch (appSortType) {
            case 0:
                java.util.Collections.sort(appList, (a, b) ->
                        Long.compare(b.getAppSize(), a.getAppSize()));
                break;
            case 1:
                java.util.Collections.sort(appList, (a, b) ->
                        Long.compare(a.getAppSize(), b.getAppSize()));
                break;
            case 2:
                java.util.Collections.sort(appList, (a, b) ->
                        a.getAppName().compareTo(b.getAppName()));
                break;
            case 3:
                java.util.Collections.sort(appList, (a, b) ->
                        b.getAppName().compareTo(a.getAppName()));
                break;
        }
    }
}