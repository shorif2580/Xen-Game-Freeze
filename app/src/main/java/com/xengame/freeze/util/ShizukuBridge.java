package com.xengame.freeze.util;

import android.content.Context;
import android.content.pm.PackageManager;

public final class ShizukuBridge {

    public static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";

    public enum State {
        NOT_INSTALLED,
        LIBRARY_MISSING,
        NOT_CONNECTED,
        READY
    }

    private ShizukuBridge() { }

    public static boolean isInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(SHIZUKU_PKG, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static State getState(Context context) {
        if (!isInstalled(context)) return State.NOT_INSTALLED;
        if (!isLibraryPresent()) return State.LIBRARY_MISSING;
        return State.NOT_CONNECTED;
    }

    public static String describe(State state) {
        switch (state) {
            case NOT_INSTALLED:
                return "Shizuku: not installed. Advanced mode unavailable.";
            case LIBRARY_MISSING:
                return "Shizuku: installed, but this build has no Shizuku connector. Freeze unavailable.";
            case NOT_CONNECTED:
                return "Shizuku: installed, not connected. Start Shizuku and grant permission.";
            case READY:
                return "Shizuku: connected.";
            default:
                return "Shizuku: unknown state.";
        }
    }

    /**
     * Runs "am force-stop" through Shizuku.
     * Returns null on success, or an error message on failure.
     * Without the Shizuku connector in this build, it always fails honestly.
     */
    public static String forceStop(Context context, String pkg) {
        State state = getState(context);
        if (state != State.READY) {
            return describe(state);
        }
        return "Shizuku connector is not implemented in this build.";
    }

    private static boolean isLibraryPresent() {
        try {
            Class.forName("rikka.shizuku.Shizuku");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
