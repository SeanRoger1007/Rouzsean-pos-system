package com.refresh.pos.techicalservices;

import android.content.ContentValues;
import com.refresh.pos.domain.SyncController;
import org.json.JSONObject;

import java.util.List;

/**
 * A synchronized wrapper for the Database.
 * It intercepts all write operations and broadcasts them via SyncController.
 */
public class DatabaseExecutor implements Database {
    private Database database;
    private SyncController syncController;
    private boolean syncEnabled = true;

    public DatabaseExecutor(Database database) {
        this.database = database;
    }

    public void setSyncController(SyncController sync) {
        this.syncController = sync;
    }

    public void setSyncEnabled(boolean enabled) {
        this.syncEnabled = enabled;
    }

    @Override
    public int insert(String table, Object content) {
        int id = database.insert(table, content);
        if (id != -1 && syncController != null && syncEnabled && content instanceof ContentValues) {
            broadcastChange("INSERT", table, (ContentValues) content);
        }
        return id;
    }

    @Override
    public boolean update(String table, Object content) {
        boolean success = database.update(table, content);
        if (success && syncController != null && syncEnabled && content instanceof ContentValues) {
            broadcastChange("UPDATE", table, (ContentValues) content);
        }
        return success;
    }

    @Override
    public boolean delete(String table, int id) {
        boolean success = database.delete(table, id);
        if (success && syncController != null && syncEnabled) {
            try {
                JSONObject json = new JSONObject();
                json.put("type", "DELETE");
                json.put("table", table);
                json.put("id", id);
                syncController.sendMessage(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    @Override
    public boolean execute(String query) {
        boolean success = database.execute(query);
        if (success && syncController != null && syncEnabled) {
            try {
                JSONObject json = new JSONObject();
                json.put("type", "EXECUTE");
                json.put("query", query);
                syncController.sendMessage(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    @Override
    public List<Object> select(String query) {
        return database.select(query);
    }

    private void broadcastChange(String type, String table, ContentValues content) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("table", table);
            
            JSONObject data = new JSONObject();
            for (String key : content.keySet()) {
                data.put(key, content.get(key));
            }
            json.put("data", data);
            
            syncController.sendMessage(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
