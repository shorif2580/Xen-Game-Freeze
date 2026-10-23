package com.xengame.freeze.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xengame.freeze.model.GameApp;

import java.util.HashSet;
import java.util.Set;

public final class FreezeManager {

    private static final String FILE = "xen_freeze_state";
    private static final String KEY_FROZEN = "frozen_pkgs";

    public static final class Result {
        public final boolean success;
        public final String message;

        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final Context context;
    private final SharedPreferences sp;

    public FreezeManager(Context context) {
        this.context = context.getApplicationContext();
        this.sp = this.context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Only apps that were successfully force-stopped are recorded as frozen. */
    public Result freeze(GameApp app) {
        AppPrefs prefs = new AppPrefs(context);
        if (app.isProtected) {
            return new Result(false, app.label + " is protected and will not be touched.");
        }
        if (prefs.getExcluded().contains(app.packageName)) {
            return new Result(false, app.label + " is in the exclude list.");
        }

        String error = ShizukuBridge.forceStop(context, app.packageName);
        if (error != null) {
            return new Result(false, "Not frozen: " + error);
        }

        Set<String> frozen = getFrozen();
        frozen.add(app.packageName);
        sp.edit().putStringSet(KEY_FROZEN, frozen).apply();
        return new Result(true, app.label + " frozen.");
    }

    public Result unfreeze(GameApp app) {
        Set<String> frozen = getFrozen();
        if (!frozen.contains(app.packageName)) {
            return new Result(false, app.label + " is not frozen.");
        }
        frozen.remove(app.packageName);
        sp.edit().putStringSet(KEY_FROZEN, frozen).apply();
        return new Result(true, app.label + " unfrozen.");
    }

    public boolean isFrozen(String pkg) {
        return getFrozen().contains(pkg);
    }

    public Set<String> getFrozen() {
        return new HashSet<>(sp.getStringSet(KEY_FROZEN, new HashSet<String>()));
    }
}
