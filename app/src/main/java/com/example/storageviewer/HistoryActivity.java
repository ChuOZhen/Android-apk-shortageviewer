package com.example.storageviewer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 历史记录页面
 * 用于查看和对比存储快照
 */
public class HistoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyHint;
    private Button btnCompare;

    private HistoryAdapter adapter;
    private AppDatabase db;
    private SnapshotDao snapshotDao;

    private List<StorageInfo> storageList;
    private String currentStoragePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupToolbar();
        initDatabase();
        loadStorageTabs();
        setupRecyclerView();
        setupCompareButton();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_history);
        tabLayout = findViewById(R.id.tab_layout);
        recyclerView = findViewById(R.id.recycler_history);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        btnCompare = findViewById(R.id.btn_compare);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDatabase() {
        db = AppDatabase.getInstance(this);
        snapshotDao = db.snapshotDao();
    }

    private void loadStorageTabs() {
        new Thread(() -> {
            storageList = StorageHelper.getAllStorageInfo(this);

            runOnUiThread(() -> {
                if (storageList != null && !storageList.isEmpty()) {
                    for (StorageInfo info : storageList) {
                        TabLayout.Tab tab = tabLayout.newTab();
                        tab.setText(info.getName());
                        tab.setTag(info.getPath());
                        tabLayout.addTab(tab);
                    }

                    if (storageList.size() > 0) {
                        currentStoragePath = storageList.get(0).getPath();
                        loadHistoryData(currentStoragePath);
                    }
                }

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        currentStoragePath = (String) tab.getTag();
                        loadHistoryData(currentStoragePath);
                        adapter.setSelectionMode(false);
                        updateCompareButton();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
            });
        }).start();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this);
        adapter.setOnSelectionChangeListener(count -> updateCompareButton());
        recyclerView.setAdapter(adapter);
    }

    private void setupCompareButton() {
        btnCompare.setOnClickListener(v -> {
            List<StorageSnapshot> selected = adapter.getSelectedSnapshots();
            if (selected.size() == 2) {
                showCompareDialog(selected.get(0), selected.get(1));
            }
        });
    }

    private void loadHistoryData(String storagePath) {
        new Thread(() -> {
            List<StorageSnapshot> snapshots = snapshotDao.getSnapshotsByPath(storagePath);

            runOnUiThread(() -> {
                adapter.setSnapshotList(snapshots);
                if (snapshots.isEmpty()) {
                    tvEmptyHint.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyHint.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void updateCompareButton() {
        int count = adapter.getSelectedSnapshots().size();
        if (adapter.isSelectionMode()) {
            btnCompare.setEnabled(count == 2);
            btnCompare.setText(count == 2 ? "对比选中的记录 (2)" : "请选择两条记录 (" + count + "/2)");
        } else {
            btnCompare.setEnabled(false);
            btnCompare.setText("对比选中的记录");
        }
    }

    private void showCompareDialog(StorageSnapshot snapshot1, StorageSnapshot snapshot2) {
        // 确保 snapshot1 是较旧的记录
        if (snapshot1.getTimestamp().after(snapshot2.getTimestamp())) {
            StorageSnapshot temp = snapshot1;
            snapshot1 = snapshot2;
            snapshot2 = temp;
        }

        long usedDiff = snapshot2.getUsedSize() - snapshot1.getUsedSize();
        String diffStr = (usedDiff >= 0 ? "+" : "") + StorageInfo.formatSize(usedDiff);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        String time1 = sdf.format(snapshot1.getTimestamp());
        String time2 = sdf.format(snapshot2.getTimestamp());

        String message = "【" + time1 + "】\n"
                + "已用空间: " + snapshot1.getFormattedUsedSize() + "\n"
                + "使用率: " + snapshot1.getUsagePercent() + "%\n\n"
                + "【" + time2 + "】\n"
                + "已用空间: " + snapshot2.getFormattedUsedSize() + "\n"
                + "使用率: " + snapshot2.getUsagePercent() + "%\n\n"
                + "变化量: " + diffStr;

        new AlertDialog.Builder(this)
                .setTitle("存储变化对比")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void manualRecordSnapshot() {
        new Thread(() -> {
            if (currentStoragePath == null) return;

            for (StorageInfo info : storageList) {
                if (info.getPath().equals(currentStoragePath)) {
                    StorageSnapshot snapshot = new StorageSnapshot(
                            info.getName(),
                            info.getPath(),
                            info.getTotalSize(),
                            info.getUsedSize(),
                            info.getAvailableSize(),
                            info.getUsagePercent()
                    );
                    snapshotDao.insert(snapshot);
                    break;
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "已记录当前快照", Toast.LENGTH_SHORT).show();
                loadHistoryData(currentStoragePath);
                adapter.setSelectionMode(false);
                updateCompareButton();
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_record) {
            manualRecordSnapshot();
            return true;
        } else if (item.getItemId() == R.id.action_cancel_select) {
            adapter.setSelectionMode(false);
            updateCompareButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}