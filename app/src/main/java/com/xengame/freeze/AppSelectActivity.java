package com.xengame.freeze;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppSelectActivity extends Activity {

    static class AppItem {
        final String label;
        final String pkg;
        final Drawable icon;

        AppItem(String label, String pkg, Drawable icon) {
            this.label = label;
            this.pkg = pkg;
            this.icon = icon;
        }
    }

    private final List<AppItem> allApps = new ArrayList<>();
    private final List<AppItem> shownApps = new ArrayList<>();
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_select);

        loadLaunchableApps();
        shownApps.addAll(allApps);

        adapter = new AppAdapter();
        ListView listView = findViewById(R.id.app_list);
        listView.setAdapter(adapter);

        EditText search = findViewById(R.id.search_box);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            AppItem item = shownApps.get(position);
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
            prefs.edit()
                    .putString(MainActivity.KEY_PKG, item.pkg)
                    .putString(MainActivity.KEY_LABEL, item.label)
                    .putBoolean(MainActivity.KEY_GAME_MODE, false)
                    .apply();
            finish();
        });
    }

    private void loadLaunchableApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved = pm.queryIntentActivities(main, 0);
        Set<String> seen = new HashSet<>();

        for (ResolveInfo ri : resolved) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) {
                continue;
            }
            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            allApps.add(new AppItem(label, pkg, icon));
        }

        Collections.sort(allApps, (a, b) -> a.label.compareToIgnoreCase(b.label));
    }

    private void filter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        shownApps.clear();
        for (AppItem item : allApps) {
            if (q.isEmpty() || item.label.toLowerCase(Locale.ROOT).contains(q)) {
                shownApps.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return shownApps.size(); }
        @Override public Object getItem(int position) { return shownApps.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(AppSelectActivity.this)
                        .inflate(R.layout.item_app, parent, false);
            }
            AppItem item = shownApps.get(position);
            ((ImageView) row.findViewById(R.id.app_icon)).setImageDrawable(item.icon);
            ((TextView) row.findViewById(R.id.app_name)).setText(item.label);
            return row;
        }
    }
}
