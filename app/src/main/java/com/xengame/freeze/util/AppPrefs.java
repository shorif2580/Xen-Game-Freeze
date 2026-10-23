package com.xengame.freeze.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class AppPrefs {

    private static final String FILE = "xen_prefs";

    public static final String KEY_SELECTED = "selected_games";
    public static final String KEY_EXCLUDED = "excluded_apps";
    public static final String KEY_RECENT = "recent_games";
    public static final String KEY_FREEZE_ACTIVE = "freeze_active";
    public static final String KEY_THEME_DARK = "theme_dark";
    public static final String KEY_ACCENT = "accent_color";
    public static final String KEY_ANIMATIONS = "animations_on";
    public static final String KEY_MONITORING = "monitoring_on";

    private static final int MAX_RECENT = 5;

    private final SharedPreferences sp;

    public AppPrefs(Context context) {
        sp = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public Set<String> getSelected() {
        return new HashSet<>(sp.getStringSet(KEY_SELECTED, new HashSet<String>()));
    }

    public void setSelected(Set<String> pkgs) {
        sp.edit().putStringSet(KEY_SELECTED, new HashSet<>(pkgs)).apply();
    }

    public Set<String> getExcluded() {
        return new HashSet<>(sp.getStringSet(KEY_EXCLUDED, new HashSet<String>()));
    }

    public void setExcluded(Set<String> pkgs) {
        sp.edit().putStringSet(KEY_EXCLUDED, new HashSet<>(pkgs)).apply();
    }

    public void addRecent(String pkg) {
        String current = sp.getString(KEY_RECENT, "");
        StringBuilder out = new StringBuilder(pkg);
        int count = 1;
        for (String p : current.split(",")) {
            if (p.isEmpty() || p.equals(pkg)) continue;
            if (count >= MAX_RECENT) break;
            out.append(',').append(p);
            count++;
        }
        sp.edit().putString(KEY_RECENT, out.toString()).apply();
    }

    public String[] getRecent() {
        String current = sp.getString(KEY_RECENT, "");
        if (current.isEmpty()) return new String[0];
        return current.split(",");
    }

    public boolean isFreezeActive() {
        return sp.getBoolean(KEY_FREEZE_ACTIVE, false);
    }

    public void setFreezeActive(boolean active) {
        sp.edit().putBoolean(KEY_FREEZE_ACTIVE, active).apply();
    }

    public boolean isDarkTheme() {
        return sp.getBoolean(KEY_THEME_DARK, true);
    }

    public void setDarkTheme(boolean dark) {
        sp.edit().putBoolean(KEY_THEME_DARK, dark).apply();
    }

    public String getAccent() {
        return sp.getString(KEY_ACCENT, "#39FF14");
    }

    public void setAccent(String hex) {
        sp.edit().putString(KEY_ACCENT, hex).apply();
    }

    public boolean isAnimationsOn() {
        return sp.getBoolean(KEY_ANIMATIONS, true);
    }

    public void setAnimationsOn(boolean on) {
        sp.edit().putBoolean(KEY_ANIMATIONS, on).apply();
    }

    public boolean isMonitoringOn() {
        return sp.getBoolean(KEY_MONITORING, true);
    }

    public void setMonitoringOn(boolean on) {
        sp.edit().putBoolean(KEY_MONITORING, on).apply();
    }

    public void resetAll() {
        sp.edit().clear().apply();
    }
}
