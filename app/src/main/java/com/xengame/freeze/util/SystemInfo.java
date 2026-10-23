package com.xengame.freeze.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.Locale;

public final class SystemInfo {

    public static final String UNAVAILABLE = "Unavailable";

    private SystemInfo() { }

    /** Returns "X.X GB free of Y.Y GB" or UNAVAILABLE. */
    public static String getRamText(Context context) {
        try {
            ActivityManager am = (ActivityManager)
                    context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            double total = mi.totalMem / 1073741824.0;
            double avail = mi.availMem / 1073741824.0;
            return String.format(Locale.US, "%.1f GB free of %.1f GB", avail, total);
        } catch (Exception e) {
            return UNAVAILABLE;
        }
    }

    /**
     * Public Android APIs do not expose system-wide CPU usage percentage
     * to normal apps on Android 11+, so only the core count is reported.
     */
    public static String getCpuText() {
        int cores = Runtime.getRuntime().availableProcessors();
        return cores + " cores. Live CPU usage: " + UNAVAILABLE;
    }
}
