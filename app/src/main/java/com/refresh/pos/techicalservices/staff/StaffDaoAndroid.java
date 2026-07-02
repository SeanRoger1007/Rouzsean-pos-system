package com.refresh.pos.techicalservices.staff;

import android.content.ContentValues;
import com.refresh.pos.domain.staff.Staff;
import com.refresh.pos.domain.staff.StaffWorkLog;
import com.refresh.pos.techicalservices.Database;
import com.refresh.pos.techicalservices.DatabaseContents;

import java.util.ArrayList;
import java.util.List;

public class StaffDaoAndroid implements StaffDao {

    private Database database;

    public StaffDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public List<Staff> getAllStaff() {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_STAFF;
        List<Object> objects = database.select(query);
        List<Staff> staffList = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                ContentValues row = (ContentValues) obj;
                Integer id = row.getAsInteger("_id");
                if (id == null) continue;
                
                Double salary = row.getAsDouble("daily_salary");
                if (salary == null) salary = 0.0;
                
                Double hours = row.getAsDouble("default_hours");
                if (hours == null) hours = 12.0;

                staffList.add(new Staff(
                        id,
                        row.getAsString("name"),
                        salary,
                        hours
                ));
            }
        }
        return staffList;
    }

    @Override
    public boolean updateStaff(Staff staff) {
        ContentValues values = new ContentValues();
        values.put("_id", staff.getId());
        values.put("name", staff.getName());
        values.put("daily_salary", staff.getDailySalary());
        values.put("default_hours", staff.getDefaultHours());
        return database.update(DatabaseContents.TABLE_STAFF.toString(), values);
    }

    @Override
    public List<StaffWorkLog> getWorkLogsForDate(String date) {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_STAFF_WORK_LOG + " WHERE date = '" + date + "'";
        List<Object> objects = database.select(query);
        List<StaffWorkLog> logs = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                ContentValues row = (ContentValues) obj;
                Integer id = row.getAsInteger("_id");
                Integer staffId = row.getAsInteger("staff_id");
                Double hours = row.getAsDouble("hours_worked");
                
                if (id == null || staffId == null) continue;
                if (hours == null) hours = 8.0;

                logs.add(new StaffWorkLog(
                        id,
                        staffId,
                        row.getAsString("date"),
                        hours
                ));
            }
        }
        return logs;
    }

    @Override
    public List<StaffWorkLog> getWorkLogsBetween(String startDate, String endDate) {
        String query = "SELECT * FROM " + DatabaseContents.TABLE_STAFF_WORK_LOG + " WHERE date BETWEEN '" + startDate + "' AND '" + endDate + "'";
        List<Object> objects = database.select(query);
        List<StaffWorkLog> logs = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                ContentValues row = (ContentValues) obj;
                Integer id = row.getAsInteger("_id");
                Integer staffId = row.getAsInteger("staff_id");
                Double hours = row.getAsDouble("hours_worked");
                
                if (id == null || staffId == null) continue;
                if (hours == null) hours = 8.0;

                logs.add(new StaffWorkLog(
                        id,
                        staffId,
                        row.getAsString("date"),
                        hours
                ));
            }
        }
        return logs;
    }

    @Override
    public void addWorkLog(int staffId, String date, double hoursWorked) {
        ContentValues values = new ContentValues();
        values.put("staff_id", staffId);
        values.put("date", date);
        values.put("hours_worked", hoursWorked);
        database.insert(DatabaseContents.TABLE_STAFF_WORK_LOG.toString(), values);
    }

    @Override
    public void removeWorkLog(int staffId, String date) {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_STAFF_WORK_LOG + " WHERE staff_id = " + staffId + " AND date = '" + date + "'");
    }

    @Override
    public int addStaff(String name, double dailySalary, double defaultHours) {
        ContentValues content = new ContentValues();
        content.put("name", name);
        content.put("daily_salary", dailySalary);
        content.put("default_hours", defaultHours);
        return database.insert(DatabaseContents.TABLE_STAFF.toString(), content);
    }

    @Override
    public void deleteStaff(int id) {
        database.delete(DatabaseContents.TABLE_STAFF.toString(), id);
        database.execute("DELETE FROM " + DatabaseContents.TABLE_STAFF_WORK_LOG + " WHERE staff_id = " + id);
    }
}
