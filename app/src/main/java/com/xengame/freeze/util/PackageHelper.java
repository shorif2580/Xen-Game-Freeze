package com.xengame.freeze.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.xengame.freeze.model.GameApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PackageHelper {

    private static final String[] PROTECTED_KEYWORDS = {
            "systemui", "launcher", "dialer", "phone", "telecom",
            "emergency", "settings", "accessibility", "inputmethod",
            "keyboard", "shizuku"
    };

    private PackageHelper() { }

    public static List<GameApp> getLaunchableApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        Set<String> homePackages = getHomePackages(pm);
        List<ResolveInfo> resolved = pm.queryIntentActivities(main, 0);
        Set<String> seen = new HashSet<>();
        List<GameApp> result = new ArrayList<>();

        for (ResolveInfo ri : resolved) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(context.getPackageName()) || !seen.add(pkg)) {
                continue;
            }
            String label = ri.loadLabel(pm).toString();
            boolean protectedApp = isProtected(context, pkg, homePackages);
            result.add(new GameApp(pkg, label, protectedApp));
        }

        Collections.sort(result,
                (a, b) -> a.label.compareToIgnoreCase(b.label));
        return result;
    }

    public static Set<String> getHomePackages(PackageManager pm) {
        Set<String> homes = new HashSet<>();
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        for (ResolveInfo ri : pm.queryIntentActivities(home, 0)) {
            homes.add(ri.activityInfo.packageName);
        }
        return homes;
    }

    public static boolean isProtected(Context context, String pkg, Set<String> homePackages) {
        if (pkg == null) return true;
        if (pkg.equals("android") || pkg.equals(context.getPackageName())) return true;
        if (homePackages.contains(pkg)) return true;

        String lower = pkg.toLowerCase(Locale.ROOT);
        for (String key : PROTECTED_KEYWORDS) {
            if (lower.contains(key)) return true;
        }

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(pkg, 0);
            return (info.flags & ApplicationInfo.FLAG_PERSISTENT) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
