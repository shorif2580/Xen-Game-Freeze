package com.xengame.freeze;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    static final String PREFS = "xen_prefs";
    static final String KEY_PKG = "selected_pkg";
    static final String KEY_LABEL = "selected_label";
    static final String KEY_GAME_MODE = "game_mode";
    static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";

    private TextView ramText;
    private TextView shizukuText;
    private TextView selectedText;
    private TextView modeText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ramText = findViewById(R.id.ram_text);
        shizukuText = findViewById(R.id.shizuku_text);
        selectedText = findViewById(R.id.selected_text);
        modeText = findViewById(R.id.mode_text);

        Button pickButton = findViewById(R.id.pick_button);
        Button startButton = findViewById(R.id.start_button);
        Button stopButton = findViewById(R.id.stop_button);

        pickButton.setOnClickListener(v ->
                startActivity(new Intent(this, AppSelectActivity.class)));

        startButton.setOnClickListener(v -> startGameMode());

        stopButton.setOnClickListener(v -> {
            prefs.edit().putBoolean(KEY_GAME_MODE, false).apply();
            refresh();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void startGameMode() {
        String pkg = prefs.getString(KEY_PKG, null);
        if (pkg == null) {
            Toast.makeText(this, "Choose a game first", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putBoolean(KEY_GAME_MODE, true).apply();
        refresh();

        Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) {
            startActivity(launch);
        } else {
            Toast.makeText(this, "Selected app cannot be opened", Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        double totalGb = mi.totalMem / 1073741824.0;
        double availGb = mi.availMem / 1073741824.0;
        ramText.setText(String.format(Locale.US,
                "RAM: %.1f GB free of %.1f GB", availGb, totalGb));

        boolean shizukuInstalled = isInstalled(SHIZUKU_PKG);
        shizukuText.setText(shizukuInstalled
                ? "Shizuku: installed. Freeze actions are not wired in this build."
                : "Shizuku: not installed. Advanced mode unavailable.");

        selectedText.setText("Selected game: " + prefs.getString(KEY_LABEL, "none"));

        boolean on = prefs.getBoolean(KEY_GAME_MODE, false);
        modeText.setText(on
                ? "Game Mode: ON. Game launched, no other apps frozen."
                : "Game Mode: OFF");
    }

    private boolean isInstalled(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
