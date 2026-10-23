package com.xengame.freeze;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xengame.freeze.model.GameApp;
import com.xengame.freeze.util.AppPrefs;
import com.xengame.freeze.util.PackageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppSelectActivity extends Activity {

    private AppPrefs prefs;
    private final List<GameApp> all = new ArrayList<>();
    private final List<GameApp> shown = new ArrayList<>();
    private Set<String> selected;
    private Set<String> excluded;
    private TextView summary;
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_select);

        prefs = new AppPrefs(this);
        selected = prefs.getSelected();
        excluded = prefs.getExcluded();

        all.addAll(PackageHelper.getLaunchableApps(this));
        shown.addAll(all);

        summary = findViewById(R.id.selected_summary);
        EditText search = findViewById(R.id.search_box);
        ListView list = findViewById(R.id.app_list);

        adapter = new AppAdapter();
        list.setAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        updateSummary();
    }

    private void filter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        shown.clear();
        for (GameApp app : all) {
            if (q.isEmpty() || app.label.toLowerCase(Locale.ROOT).contains(q)) {
                shown.add(app);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void save() {
        prefs.setSelected(selected);
        prefs.setExcluded(excluded);
        updateSummary();
    }

    private void updateSummary() {
        List<String> names = new ArrayList<>();
        for (GameApp app : all) {
            if (selected.contains(app.packageName)) names.add(app.label);
        }
        String list = names.isEmpty() ? "none" : String.join(", ", names);
        summary.setText("Freeze list (" + selected.size() + "): " + list
                + "\nExcluded: " + excluded.size());
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int position) { return shown.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(AppSelectActivity.this)
                        .inflate(R.layout.item_app, parent, false);
            }

            GameApp app = shown.get(position);
            final String pkg = app.packageName;

            ImageView icon = row.findViewById(R.id.app_icon);
            TextView name = row.findViewById(R.id.app_name);
            CheckBox cbFreeze = row.findViewById(R.id.check_freeze);
            CheckBox cbExclude = row.findViewById(R.id.check_exclude);

            try {
                icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg));
            } catch (PackageManager.NameNotFoundException e) {
                icon.setImageDrawable(null);
            }

            name.setText(app.isProtected ? app.label + " (protected)" : app.label);

            cbFreeze.setOnCheckedChangeListener(null);
            cbExclude.setOnCheckedChangeListener(null);
            cbFreeze.setChecked(selected.contains(pkg));
            cbExclude.setChecked(excluded.contains(pkg));
            cbFreeze.setEnabled(!app.isProtected);
            cbExclude.setEnabled(!app.isProtected);

            cbFreeze.setOnCheckedChangeListener((b, checked) -> {
                if (checked) selected.add(pkg); else selected.remove(pkg);
                save();
            });
            cbExclude.setOnCheckedChangeListener((b, checked) -> {
                if (checked) excluded.add(pkg); else excluded.remove(pkg);
                save();
            });

            return row;
        }
    }
}
