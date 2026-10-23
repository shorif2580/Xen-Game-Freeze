package com.xengame.freeze.model;

public class GameApp {
    public final String packageName;
    public final String label;
    public final boolean isProtected;

    public GameApp(String packageName, String label, boolean isProtected) {
        this.packageName = packageName;
        this.label = label;
        this.isProtected = isProtected;
    }
}
