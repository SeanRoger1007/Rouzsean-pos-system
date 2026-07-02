package com.refresh.pos.techicalservices.capital;

import android.content.ContentValues;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;
import java.util.List;

public class CapitalDaoAndroid implements CapitalDao {

    private Database database;

    public CapitalDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public double getCapital(String key) {
        String query = "SELECT value FROM " + DatabaseContents.TABLE_CAPITAL + " WHERE key = '" + key + "'";
        List<Object> objects = database.select(query);
        if (objects != null && !objects.isEmpty()) {
            ContentValues row = (ContentValues) objects.get(0);
            Double val = row.getAsDouble("value");
            return val != null ? val : 0.0;
        }
        return 0.0;
    }

    @Override
    public void setCapital(String key, double value) {
        database.execute("UPDATE " + DatabaseContents.TABLE_CAPITAL + " SET value = " + value + " WHERE key = '" + key + "'");
    }
}
