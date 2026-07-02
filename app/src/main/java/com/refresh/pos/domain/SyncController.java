package com.refresh.pos.domain;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.refresh.pos.techicalservices.DatabaseExecutor;
import com.refresh.pos.ui.MainActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncController {
    public static final int SYNC_PORT = 8080;
    private static SyncController instance;
    private Context context;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private boolean isRunning = false;
    private List<PrintWriter> clients = new ArrayList<>();
    private DatabaseExecutor syncedDb;

    public enum Mode { OFF, MAIN, SECONDARY }

    private SyncController(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("SyncPrefs", Context.MODE_PRIVATE);
    }

    public static synchronized SyncController getInstance(Context context) {
        if (instance == null) instance = new SyncController(context);
        return instance;
    }

    public void setDatabaseExecutor(DatabaseExecutor db) {
        this.syncedDb = db;
    }

    public Mode getMode() {
        return Mode.valueOf(prefs.getString("mode", Mode.OFF.name()));
    }

    public void setMode(Mode mode) {
        prefs.edit().putString("mode", mode.name()).apply();
    }

    public String getServerIp() {
        return prefs.getString("server_ip", "");
    }

    public void setServerIp(String ip) {
        prefs.edit().putString("server_ip", ip).apply();
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        if (getMode() == Mode.MAIN) {
            startServer();
        } else if (getMode() == Mode.SECONDARY) {
            if (!getServerIp().isEmpty()) {
                startClientListener();
            }
        }
    }

    private void startServer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(SYNC_PORT)) {
                    while (isRunning) {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    }
                } catch (Exception e) {
                    Log.e("SyncController", "Server Error", e);
                }
            }
        });
    }

    private void handleClient(final Socket socket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    
                    synchronized (clients) { clients.add(out); }
                    
                    // Send initial database to new client
                    sendInitialDatabase(out);
                    
                    String line;
                    while ((line = in.readLine()) != null) {
                        processIncomingMessage(line);
                        broadcast(line, out); 
                    }
                } catch (Exception e) {
                    Log.e("SyncController", "Client Error", e);
                } finally {
                    // Cleanup client if needed
                }
            }
        });
    }

    private void startClientListener() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (isRunning && getMode() == Mode.SECONDARY) {
                    try (Socket socket = new Socket(getServerIp(), SYNC_PORT);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        
                        String line;
                        while ((line = in.readLine()) != null) {
                            processIncomingMessage(line);
                        }
                    } catch (Exception e) {
                        Log.e("SyncController", "Connection lost, retrying...", e);
                        try { Thread.sleep(5000); } catch (InterruptedException ie) {}
                    }
                }
            }
        });
    }

    private void sendInitialDatabase(PrintWriter out) {
        try {
            File dbFile = context.getDatabasePath("POS.db");
            if (!dbFile.exists()) return;

            FileInputStream fis = new FileInputStream(dbFile);
            byte[] bytes = new byte[(int) dbFile.length()];
            int read = fis.read(bytes);
            fis.close();

            if (read > 0) {
                String base64Db = Base64.encodeToString(bytes, 0, read, Base64.NO_WRAP);
                JSONObject json = new JSONObject();
                json.put("type", "INITIAL_DB");
                json.put("data", base64Db);
                out.println(json.toString());
                Log.d("SyncController", "Initial DB sent to client. Size: " + read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message, PrintWriter exclude) {
        synchronized (clients) {
            for (PrintWriter out : clients) {
                if (out != exclude) out.println(message);
            }
        }
    }

    public void sendMessage(final String message) {
        if (getMode() == Mode.SECONDARY) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try (Socket socket = new Socket(getServerIp(), SYNC_PORT);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        out.println(message);
                    } catch (Exception e) {
                        Log.e("SyncController", "Send Error", e);
                    }
                }
            });
        } else if (getMode() == Mode.MAIN) {
            broadcast(message, null);
        }
    }

    private void processIncomingMessage(final String jsonStr) {
        try {
            final JSONObject json = new JSONObject(jsonStr);
            String type = json.getString("type");

            if ("INITIAL_DB".equals(type)) {
                Log.d("SyncController", "Received INITIAL_DB message");
                String base64Data = json.getString("data");
                byte[] bytes = Base64.decode(base64Data, Base64.NO_WRAP);
                
                File dbPath = context.getDatabasePath("POS.db");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(dbPath);
                fos.write(bytes);
                fos.close();
                Log.d("SyncController", "Database overwritten successfully. Size: " + bytes.length);
                
                restartApp();
                return;
            }

            if (syncedDb == null) return;

            syncedDb.setSyncEnabled(false); 
            
            if ("INSERT".equals(type)) {
                syncedDb.insert(json.getString("table"), parseContentValues(json.getJSONObject("data")));
            } else if ("UPDATE".equals(type)) {
                syncedDb.update(json.getString("table"), parseContentValues(json.getJSONObject("data")));
            } else if ("DELETE".equals(type)) {
                syncedDb.delete(json.getString("table"), json.getInt("id"));
            } else if ("EXECUTE".equals(type)) {
                syncedDb.execute(json.getString("query"));
            }

            syncedDb.setSyncEnabled(true);
            refreshUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContentValues parseContentValues(JSONObject data) throws org.json.JSONException {
        ContentValues cv = new ContentValues();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = data.get(key);
            if (val instanceof Integer) cv.put(key, (Integer) val);
            else if (val instanceof Double) cv.put(key, (Double) val);
            else if (val instanceof Long) cv.put(key, (Long) val);
            else if (val instanceof Boolean) cv.put(key, (Boolean) val);
            else cv.put(key, val.toString());
        }
        return cv;
    }

    private void refreshUI() {
        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.instance != null) {
                    MainActivity.instance.updateAllFragments();
                }
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().toLowerCase().contains("wlan") || intf.getName().toLowerCase().contains("eth")) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress();
                            boolean isIPv4 = sAddr.indexOf(':') < 0;
                            if (isIPv4) return sAddr;
                        }
                    }
                }
            }
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4) return sAddr;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("SyncController", "Error getting IP", ex);
        }
        return "Unknown (Check Wi-Fi)";
    }
}
