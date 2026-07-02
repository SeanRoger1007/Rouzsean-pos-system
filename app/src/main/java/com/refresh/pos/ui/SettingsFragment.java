package com.refresh.pos.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.ThemeController;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.BackupController;
import com.refresh.pos.domain.SyncController;
import com.refresh.pos.domain.inventory.InventorySettingsController;
import com.refresh.pos.ui.component.UpdatableFragment;

public class SettingsFragment extends UpdatableFragment {

    private EditText inputThreshold;
    private EditText inputAddress;
    private EditText inputPhone;
    private android.widget.CheckBox checkboxHidePhone;
    private View themeToggleContainer, toggleTrack, toggleThumb, activeIconContainer;
    private View itemPrinters, itemGeneral, itemReceipt, itemDatabase, itemSync;
    private View containerGeneral, containerReceipt, containerDatabase, containerSync;
    private View layoutMainInfo, layoutSecondaryInfo;
    private ImageView iconSun, iconMoon, iconSunActive, iconMoonActive;
    private ImageView iconPrinters, iconGeneral, iconReceipt, iconDatabase, iconSync;
    private Spinner spinnerDateFormat, spinnerSyncMode;
    private TextView textLocalIp;
    private EditText inputServerIp;
    private Button btnSave;
    private Button btnExport, btnImport;

    private final String[] syncModes = {"OFF", "Main Terminal (Server)", "Secondary Terminal (Client)"};

    public void updateStatus(final String status) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textLocalIp != null) {
                    if (SyncController.getInstance(getActivity()).getMode() == SyncController.Mode.SECONDARY) {
                        textLocalIp.setText("Status: " + status);
                    }
                }
            }
        });
    }

    private static final int REQUEST_CREATE_BACKUP = 101;
    private static final int REQUEST_OPEN_BACKUP = 102;

    private final String[] formats = {"yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm", "MM/dd/yyyy hh:mm a"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_settings, container, false);

        inputThreshold = (EditText) view.findViewById(R.id.input_threshold);
        inputAddress = (EditText) view.findViewById(R.id.input_receipt_address);
        inputPhone = (EditText) view.findViewById(R.id.input_receipt_phone);
        checkboxHidePhone = (android.widget.CheckBox) view.findViewById(R.id.checkbox_hide_phone);
        themeToggleContainer = view.findViewById(R.id.theme_toggle_container);
        toggleTrack = view.findViewById(R.id.toggle_track);
        toggleThumb = view.findViewById(R.id.toggle_thumb);
        activeIconContainer = view.findViewById(R.id.active_icon_container);
        iconSun = (ImageView) view.findViewById(R.id.icon_sun);
        iconMoon = (ImageView) view.findViewById(R.id.icon_moon);
        iconSunActive = (ImageView) view.findViewById(R.id.icon_sun_active);
        iconMoonActive = (ImageView) view.findViewById(R.id.icon_moon_active);
        spinnerDateFormat = (Spinner) view.findViewById(R.id.spinner_date_format);
        btnSave = (Button) view.findViewById(R.id.btn_save);

        itemPrinters = view.findViewById(R.id.item_printers);
        itemGeneral = view.findViewById(R.id.item_general);
        itemReceipt = view.findViewById(R.id.item_receipt);
        itemDatabase = view.findViewById(R.id.item_database);
        itemSync = view.findViewById(R.id.item_sync);
        
        containerGeneral = view.findViewById(R.id.container_general);
        containerReceipt = view.findViewById(R.id.container_receipt);
        containerDatabase = view.findViewById(R.id.container_database);
        containerSync = view.findViewById(R.id.container_sync);

        layoutMainInfo = view.findViewById(R.id.layout_main_info);
        layoutSecondaryInfo = view.findViewById(R.id.layout_secondary_info);
        textLocalIp = (TextView) view.findViewById(R.id.text_local_ip);
        inputServerIp = (EditText) view.findViewById(R.id.input_server_ip);
        spinnerSyncMode = (Spinner) view.findViewById(R.id.spinner_sync_mode);

        iconPrinters = (ImageView) view.findViewById(R.id.icon_printers);
        iconGeneral = (ImageView) view.findViewById(R.id.icon_general);
        iconReceipt = (ImageView) view.findViewById(R.id.icon_receipt);
        iconDatabase = (ImageView) view.findViewById(R.id.icon_database);
        iconSync = (ImageView) view.findViewById(R.id.icon_sync);

        btnExport = (Button) view.findViewById(R.id.btn_export_db);
        btnImport = (Button) view.findViewById(R.id.btn_import_db);

        int theme = ThemeController.getInstance(getActivity()).getTheme();
        if (theme == ThemeController.THEME_DARK) {
            view.findViewById(R.id.settings_root).setBackgroundColor(Color.parseColor("#121212"));
            ((TextView) view.findViewById(R.id.label_stock_threshold)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.label_theme)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.label_date_format)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.label_receipt_address)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.label_receipt_phone)).setTextColor(Color.WHITE);
            textLocalIp.setTextColor(Color.WHITE);
            
            // List item titles
            if (itemPrinters instanceof ViewGroup) ((TextView) ((ViewGroup) itemPrinters).getChildAt(1)).setTextColor(Color.WHITE);
            if (itemGeneral instanceof ViewGroup) ((TextView) ((ViewGroup) itemGeneral).getChildAt(1)).setTextColor(Color.WHITE);
            if (itemReceipt instanceof ViewGroup) ((TextView) ((ViewGroup) itemReceipt).getChildAt(1)).setTextColor(Color.WHITE);
            if (itemDatabase instanceof ViewGroup) ((TextView) ((ViewGroup) itemDatabase).getChildAt(1)).setTextColor(Color.WHITE);
            if (itemSync instanceof ViewGroup) ((TextView) ((ViewGroup) itemSync).getChildAt(1)).setTextColor(Color.WHITE);

            if (iconPrinters != null) iconPrinters.setColorFilter(Color.WHITE);
            if (iconGeneral != null) iconGeneral.setColorFilter(Color.WHITE);
            if (iconReceipt != null) iconReceipt.setColorFilter(Color.WHITE);
            if (iconDatabase != null) iconDatabase.setColorFilter(Color.WHITE);
            if (iconSync != null) iconSync.setColorFilter(Color.WHITE);

            inputThreshold.setTextColor(Color.WHITE);
            inputThreshold.setHintTextColor(Color.parseColor("#444444"));
            inputAddress.setTextColor(Color.WHITE);
            inputAddress.setHintTextColor(Color.parseColor("#444444"));
            inputPhone.setTextColor(Color.WHITE);
            inputPhone.setHintTextColor(Color.parseColor("#444444"));
            checkboxHidePhone.setTextColor(Color.WHITE);
        }

        initUI();
        return view;
    }

    private void initUI() {
        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("ReceiptPrefs", android.content.Context.MODE_PRIVATE);
        inputAddress.setText(prefs.getString("address", "Pob 4 Calatagan Batangas"));
        inputPhone.setText(prefs.getString("phone", "09270727160"));
        checkboxHidePhone.setChecked(prefs.getBoolean("hide_phone", false));

        // Threshold
        int currentThreshold = InventorySettingsController.getInstance(getActivity()).getLowStockThreshold();
        inputThreshold.setText(String.valueOf(currentThreshold));

        // Theme
        final int theme = ThemeController.getInstance(getActivity()).getTheme();
        updateToggleUI(theme == ThemeController.THEME_DARK);

        themeToggleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentTheme = ThemeController.getInstance(getActivity()).getTheme();
                final int newTheme = (currentTheme == ThemeController.THEME_DARK) ? ThemeController.THEME_LIGHT : ThemeController.THEME_DARK;
                
                // 1. Move the slider visually first
                updateToggleUI(newTheme == ThemeController.THEME_DARK);
                
                // 2. Save the theme after a short delay to let the animation be seen
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ThemeController.getInstance(getActivity()).setTheme(newTheme);
                        
                        // 3. Restart Main to apply theme change globally
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }, 300);
            }
        });

        // Date Format
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, formats);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateFormat.setAdapter(dateAdapter);
        
        String currentFormat = getActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE).getString("date_format", "yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].equals(currentFormat)) {
                spinnerDateFormat.setSelection(i);
                break;
            }
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // Category Expand/Collapse
        itemPrinters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrinterActivity.class);
                startActivity(intent);
            }
        });

        itemGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContainer(containerGeneral);
            }
        });

        itemReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContainer(containerReceipt);
            }
        });

        itemDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContainer(containerDatabase);
            }
        });

        itemSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContainer(containerSync);
            }
        });

        // Sync Modes
        ArrayAdapter<String> syncAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, syncModes);
        syncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSyncMode.setAdapter(syncAdapter);
        
        SyncController sync = SyncController.getInstance(getActivity());
        spinnerSyncMode.setSelection(sync.getMode().ordinal());
        inputServerIp.setText(sync.getServerIp());
        
        if (sync.getMode() == SyncController.Mode.MAIN) {
            textLocalIp.setText(sync.getLocalIpAddress());
        } else if (sync.getMode() == SyncController.Mode.SECONDARY) {
            textLocalIp.setText("Status: Waiting for Main...");
        }

        spinnerSyncMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                layoutMainInfo.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                layoutSecondaryInfo.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/octet-stream");
                intent.putExtra(Intent.EXTRA_TITLE, "POS_Backup_" + System.currentTimeMillis() + ".db");
                startActivityForResult(intent, REQUEST_CREATE_BACKUP);
            }
        });

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("Restore Data")
                    .setMessage("This will DELETE all current data on this device and replace it with the backup. Proceed?")
                    .setPositiveButton("Yes, Restore", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            startActivityForResult(intent, REQUEST_OPEN_BACKUP);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            final android.net.Uri uri = data.getData();
            if (requestCode == REQUEST_CREATE_BACKUP) {
                if (BackupController.exportDatabase(getActivity(), uri)) {
                    Toast.makeText(getActivity(), "Backup saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to save backup", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_OPEN_BACKUP) {
                if (BackupController.importDatabase(getActivity(), uri)) {
                    Toast.makeText(getActivity(), "Restore successful! Restarting...", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, 1000);
                } else {
                    Toast.makeText(getActivity(), "Failed to restore backup", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toggleContainer(View container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

    private void updateToggleUI(boolean isDark) {
        float density = getResources().getDisplayMetrics().density;
        // Total width 120dp, thumb 48dp, padding 4dp on each side -> translation is (120 - 48 - 8) * density = 64dp
        float translationX = isDark ? (64 * density) : 0;
        
        // Animate thumb movement
        android.animation.ObjectAnimator thumbAnimator = android.animation.ObjectAnimator.ofFloat(toggleThumb, "translationX", translationX);
        thumbAnimator.setDuration(250);
        thumbAnimator.start();

        // Animate icons inside the thumb
        android.animation.ObjectAnimator iconAnimator = android.animation.ObjectAnimator.ofFloat(activeIconContainer, "translationX", translationX);
        iconAnimator.setDuration(250);
        iconAnimator.start();

        if (isDark) {
            // Dark track
            toggleTrack.setBackgroundColor(Color.parseColor("#1A1A1A"));
            iconSun.setColorFilter(Color.parseColor("#444444"));
            iconMoon.setVisibility(View.INVISIBLE); 
            iconSunActive.setVisibility(View.GONE);
            iconMoonActive.setVisibility(View.VISIBLE);
        } else {
            // White track
            toggleTrack.setBackgroundColor(Color.WHITE);
            iconSun.setVisibility(View.INVISIBLE);
            iconMoon.setColorFilter(Color.parseColor("#B0BEC5"));
            iconSunActive.setVisibility(View.VISIBLE);
            iconMoonActive.setVisibility(View.GONE);
        }
    }

    private void saveSettings() {
        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("ReceiptPrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit()
            .putString("address", inputAddress.getText().toString())
            .putString("phone", inputPhone.getText().toString())
            .putBoolean("hide_phone", checkboxHidePhone.isChecked())
            .apply();

        String thresholdValue = inputThreshold.getText().toString();
        if (!thresholdValue.isEmpty()) {
            int newThreshold = Integer.parseInt(thresholdValue);
            InventorySettingsController.getInstance(getActivity()).setLowStockThreshold(newThreshold);
        }

        String newDateFormat = formats[spinnerDateFormat.getSelectedItemPosition()];
        getActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE).edit().putString("date_format", newDateFormat).apply();
        DateTimeStrategy.setCustomFormat(newDateFormat);

        // Sync Settings
        SyncController sync = SyncController.getInstance(getActivity());
        SyncController.Mode oldMode = sync.getMode();
        String oldIp = sync.getServerIp();
        
        SyncController.Mode newMode = SyncController.Mode.values()[spinnerSyncMode.getSelectedItemPosition()];
        String newIp = inputServerIp.getText().toString();
        
        sync.setMode(newMode);
        sync.setServerIp(newIp);

        if (newMode != oldMode || !newIp.equals(oldIp)) {
            // Trigger a restart to apply sync changes and perform initial catch-up if needed
            Toast.makeText(getActivity(), "Sync settings updated. Restarting...", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }, 1000);
        } else {
            Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void update() {
    }
}
