package com.refresh.pos.ui;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.techicalservices.BluetoothPrinterService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrinterActivity extends Activity {

    private ListView printerListView;
    private View emptyState;
    private Button btnAddPrinter;
    private BluetoothAdapter bluetoothAdapter;
    private List<String> discoveredNames = new ArrayList<String>();
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private ArrayAdapter<String> adapter;
    private SharedPreferences printerPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initiateActionBar();
        setContentView(R.layout.layout_printer);

        printerPrefs = getSharedPreferences("PrinterPrefs", MODE_PRIVATE);
        printerListView = (ListView) findViewById(R.id.printer_list);
        emptyState = findViewById(R.id.empty_state);
        btnAddPrinter = (Button) findViewById(R.id.btn_add_printer);

        applyDarkMode();

        TextView hintText = (TextView) findViewById(R.id.text_connect_hint);
        if (hintText != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hintText.setText(Html.fromHtml(getString(R.string.connect_printer_hint), Html.FROM_HTML_MODE_LEGACY));
            } else {
                hintText.setText(Html.fromHtml(getString(R.string.connect_printer_hint)));
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnAddPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDiscovery();
            }
        });

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, discoveredNames) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int theme = com.refresh.pos.domain.ThemeController.getInstance(PrinterActivity.this).getTheme();
                if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        printerListView.setAdapter(adapter);

        printerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceList.get(position);
                showPrinterOptions(device);
            }
        });

        loadSavedPrinter();
    }

    private void applyDarkMode() {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            findViewById(R.id.printer_root).setBackgroundColor(Color.parseColor("#121212"));
            
            // Text colors
            int white = Color.WHITE;
            int gray = Color.parseColor("#999999");
            
            ((TextView) findViewById(R.id.text_connect_hint)).setTextColor(gray);
            // The empty state message "No printers" is anonymous, need to target it
            // By finding all TextViews in the empty_state container
            LinearLayout es = (LinearLayout) findViewById(R.id.empty_state);
            for (int i = 0; i < es.getChildCount(); i++) {
                View v = es.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(white);
                }
            }
            // Re-apply specific gray to hint
            ((TextView) findViewById(R.id.text_connect_hint)).setTextColor(gray);
            
            printerListView.setBackgroundColor(Color.parseColor("#121212"));
            printerListView.setDivider(new ColorDrawable(Color.parseColor("#333333")));
            printerListView.setDividerHeight(1);
        }
    }

    private void initiateActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
            } else {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
            }

            View customView = getLayoutInflater().inflate(R.layout.layout_custom_actionbar, null);
            actionBar.setCustomView(customView);

            TextView titleText = (TextView) customView.findViewById(R.id.action_bar_title);
            titleText.setText(getString(R.string.printers));

            customView.findViewById(R.id.btn_hamburger).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void loadSavedPrinter() {
        String address = printerPrefs.getString("address", null);
        String name = printerPrefs.getString("name", null);
        if (address != null) {
            discoveredNames.clear();
            deviceList.clear();
            discoveredNames.add("Saved: " + name + "\n" + address);
            deviceList.add(bluetoothAdapter.getRemoteDevice(address));
            adapter.notifyDataSetChanged();
            emptyState.setVisibility(View.GONE);
            printerListView.setVisibility(View.VISIBLE);
        }
    }

    private void showPrinterOptions(final BluetoothDevice device) {
        String[] options = {"Connect & Test Print", "Set as Default", "Forget"};
        new AlertDialog.Builder(this)
            .setTitle(device.getName())
            .setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) testPrint(device);
                    else if (which == 1) savePrinter(device);
                    else if (which == 2) forgetPrinter();
                }
            })
            .show();
    }

    private void savePrinter(BluetoothDevice device) {
        printerPrefs.edit()
            .putString("address", device.getAddress())
            .putString("name", device.getName())
            .apply();
        Toast.makeText(this, "Printer saved as default", Toast.LENGTH_SHORT).show();
        loadSavedPrinter();
    }

    private void forgetPrinter() {
        printerPrefs.edit().clear().apply();
        discoveredNames.clear();
        deviceList.clear();
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(View.VISIBLE);
        printerListView.setVisibility(View.GONE);
    }

    private void testPrint(final BluetoothDevice device) {
        final BluetoothPrinterService service = BluetoothPrinterService.getInstance();
        Toast.makeText(this, "Connecting to " + device.getName() + "...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = service.connect(device.getAddress());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            service.print(BluetoothPrinterService.RESET);
                            service.print(BluetoothPrinterService.ALIGN_CENTER);
                            service.print(BluetoothPrinterService.BOLD_ON);
                            service.printText("Rouzsean Variety Store\n");
                            service.print(BluetoothPrinterService.BOLD_OFF);
                            service.printText("--------------------------------\n");
                            service.printText("Bluetooth Printer Test\n");
                            service.printText("Status: WORKING\n");
                            service.printText("--------------------------------\n");
                            service.printNewLine();
                            service.printNewLine();
                            service.printNewLine();
                            Toast.makeText(PrinterActivity.this, "Test print sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PrinterActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void startBluetoothDiscovery() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return;
        }

        discoveredNames.clear();
        deviceList.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            discoveredNames.add(device.getName() + " (Paired)\n" + device.getAddress());
            deviceList.add(device);
        }

        adapter.notifyDataSetChanged();
        emptyState.setVisibility(View.GONE);
        printerListView.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    String info = device.getName() + "\n" + device.getAddress();
                    if (!discoveredNames.contains(info)) {
                        discoveredNames.add(info);
                        deviceList.add(device);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {}
        if (bluetoothAdapter != null) bluetoothAdapter.cancelDiscovery();
    }
}
