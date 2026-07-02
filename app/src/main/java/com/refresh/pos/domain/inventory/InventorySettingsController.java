package com.refresh.pos.domain.inventory;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Controller for inventory settings.
 */
public class InventorySettingsController {

    private static final String PREFS_NAME = "InventorySettings";
    private static final String KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold";
    private static final int DEFAULT_THRESHOLD = 10;

    private static InventorySettingsController instance;
    private SharedPreferences sharedPreferences;

    private InventorySettingsController(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static InventorySettingsController getInstance(Context context) {
        if (instance == null) {
            instance = new InventorySettingsController(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Returns the global low stock threshold.
     * @return low stock threshold.
     */
    public int getLowStockThreshold() {
        return sharedPreferences.getInt(KEY_LOW_STOCK_THRESHOLD, DEFAULT_THRESHOLD);
    }

    /**
     * Sets the global low stock threshold.
     * @param threshold low stock threshold.
     */
    public void setLowStockThreshold(int threshold) {
        sharedPreferences.edit().putInt(KEY_LOW_STOCK_THRESHOLD, threshold).apply();
    }
}
