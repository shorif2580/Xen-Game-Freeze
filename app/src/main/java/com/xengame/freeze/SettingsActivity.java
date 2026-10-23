package com.xengame.freeze;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xengame.freeze.util.AppPrefs;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final AppPrefs prefs = new AppPrefs(this);

        Switch anim = findViewById(R.id.switch_animations);
        anim.setChecked(prefs.isAnimationsOn());
        anim.setOnCheckedChangeListener((b, on) -> prefs.setAnimationsOn(on));

        Switch monitoring = findViewById(R.id.switch_monitoring);
        monitoring.setChecked(prefs.isMonitoringOn());
        monitoring.setOnCheckedChangeListener((b, on) -> prefs.setMonitoringOn(on));

        Button reset = findViewById(R.id.reset_button);
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Reset all settings?")
                .setMessage("This clears selected apps, exclusions, recent games and freeze records.")
                .setPositiveButton("Reset", (d, w) -> {
                    prefs.resetAll();
                    getSharedPreferences("xen_freeze_state", Context.MODE_PRIVATE)
                            .edit().clear().apply();
                    Toast.makeText(this, "Settings reset", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show());

        TextView about = findViewById(R.id.about_text);
        about.setText(
                "Xen Game Freeze v1.0.0\n"
                + "Developer: shorif2580\n\n"
                + "Permissions: none requested. Installed apps are read locally to build the list. "
                + "No internet, account, analytics or tracking.\n\n"
                + "Freeze and force-stop need Shizuku with its connector. "
                + "This build has no connector yet, so no app is frozen. "
                + "Failures are shown honestly.\n\n"
                + "Light/dark theme and accent colour are not in this build.");
    }
}
