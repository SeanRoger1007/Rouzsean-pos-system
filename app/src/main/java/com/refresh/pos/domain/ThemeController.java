package com.refresh.pos.domain;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeController {

    private static final String PREFS_NAME = "ThemeSettings";
    private static final String KEY_THEME = "current_theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    private static ThemeController instance;
    private SharedPreferences sharedPreferences;

    private ThemeController(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static ThemeController getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeController(context.getApplicationContext());
        }
        return instance;
    }

    public int getTheme() {
        return sharedPreferences.getInt(KEY_THEME, THEME_LIGHT);
    }

    public void setTheme(int theme) {
        sharedPreferences.edit().putInt(KEY_THEME, theme).apply();
    }
}
