package com.refresh.pos.techicalservices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPrinterService {

    private static final String TAG = "BluetoothPrinterService";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothPrinterService instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    private BluetoothPrinterService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothPrinterService getInstance() {
        if (instance == null) {
            instance = new BluetoothPrinterService();
        }
        return instance;
    }

    public boolean connect(String address) {
        if (bluetoothAdapter == null || address == null) return false;

        disconnect();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            outputStream = socket.getOutputStream();
            Log.d(TAG, "Connected to " + address);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Disconnection error", e);
        }
        outputStream = null;
        socket = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void print(byte[] data) {
        if (outputStream != null && isConnected()) {
            try {
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Print failed", e);
                disconnect();
            }
        }
    }

    public void printText(String text) {
        if (text == null) return;
        try {
            print(text.getBytes("GBK")); // Most thermal printers use GBK for special chars/currency
        } catch (Exception e) {
            print(text.getBytes());
        }
    }

    public void printNewLine() {
        printText("\n");
    }

    // ESC/POS Commands
    public static final byte[] RESET = {0x1B, 0x40};
    public static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    public static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    public static final byte[] ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    public static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    public static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    public static final byte[] SIZE_NORMAL = {0x1D, 0x21, 0x00};
    public static final byte[] SIZE_LARGE = {0x1D, 0x21, 0x11};
}
