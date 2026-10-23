package com.xengame.freeze;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xengame.freeze.model.GameApp;
import com.xengame.freeze.util.AppPrefs;
import com.xengame.freeze.util.FreezeManager;
import com.xengame.freeze.util.PackageHelper;
import com.xengame.freeze.util.ShizukuBridge;
import com.xengame.freeze.util.SystemInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private AppPrefs prefs;
    private FreezeManager freezeManager;
    private TextView ramText;
    private TextView cpuText;
    private TextView shizukuText;
    private TextView selectedText;
    private TextView freezeText;
    private TextView recentText;

    private final Handler handler = new Handler();
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            ramText.setText("RAM: " + SystemInfo.getRamText(MainActivity.this));
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new AppPrefs(this);
        freezeManager = new FreezeManager(this);

        ramText = findViewById(R.id.ram_text);
        cpuText = findViewById(R.id.cpu_text);
        shizukuText = findViewById(R.id.shizuku_text);
        selectedText = findViewById(R.id.selected_text);
        freezeText = findViewById(R.id.freeze_text);
        recentText = findViewById(R.id.recent_text);

        Button pick = findViewById(R.id.pick_button);
        Button start = findViewById(R.id.start_button);
        Button stop = findViewById(R.id.stop_button);
        Button settings = findViewById(R.id.settings_button);

        pick.setOnClickListener(v -> startActivity(new Intent(this, AppSelectActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        start.setOnClickListener(v -> confirmStart());
        stop.setOnClickListener(v -> stopGameMode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private void refresh() {
        cpuText.setText("CPU: " + SystemInfo.getCpuText());
        shizukuText.setText(ShizukuBridge.describe(ShizukuBridge.getState(this)));
        selectedText.setText("Selected apps: " + prefs.getSelected().size()
                + " | Excluded: " + prefs.getExcluded().size());

        int frozen = freezeManager.getFrozen().size();
        freezeText.setText(frozen > 0
                ? "Freeze mode: ON, " + frozen + " app(s) frozen"
                : "Freeze mode: OFF, no app frozen");

        List<String> names = new ArrayList<>();
        for (String pkg : prefs.getRecent()) {
            if (!pkg.isEmpty()) names.add(labelOf(pkg));
        }
        recentText.setText(names.isEmpty()
                ? "Recent: none"
                : "Recent: " + String.join(", ", names));
    }

    private void confirmStart() {
        final Set<String> selected = prefs.getSelected();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Choose apps first", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Start Game Mode?")
                .setMessage("Selected apps will be frozen only where Shizuku allows it. "
                        + "Protected and excluded apps are always skipped.")
                .setPositiveButton("Start", (d, w) -> startGameMode(selected))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startGameMode(Set<String> selected) {
        Set<String> homes = PackageHelper.getHomePackages(getPackageManager());
        List<String> messages = new ArrayList<>();
        int ok = 0;

        for (String pkg : selected) {
            GameApp app = new GameApp(pkg, labelOf(pkg),
                    PackageHelper.isProtected(this, pkg, homes));
            FreezeManager.Result r = freezeManager.freeze(app);
            messages.add(r.message);
            if (r.success) ok++;
            prefs.addRecent(pkg);
        }

        StringBuilder sb = new StringBuilder();
        for (String m : messages) sb.append(m).append("\n");

        new AlertDialog.Builder(this)
                .setTitle(ok + " of " + selected.size() + " app(s) frozen")
                .setMessage(sb.toString().trim())
                .setPositiveButton("OK", null)
                .show();
        refresh();
    }

    private void stopGameMode() {
        Set<String> frozen = freezeManager.getFrozen();
        if (frozen.isEmpty()) {
            Toast.makeText(this, "Freeze mode is already OFF", Toast.LENGTH_SHORT).show();
            return;
        }
        for (String pkg : frozen) {
            freezeManager.unfreeze(new GameApp(pkg, labelOf(pkg), false));
        }
        Toast.makeText(this, "Freeze mode OFF. Android may restart apps as needed.",
                Toast.LENGTH_LONG).show();
        refresh();
    }

    private String labelOf(String pkg) {
        PackageManager pm = getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }
}
