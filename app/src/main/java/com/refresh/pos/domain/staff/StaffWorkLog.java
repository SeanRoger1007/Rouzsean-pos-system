package com.refresh.pos.domain.staff;

public class StaffWorkLog {
    private int id;
    private int staffId;
    private String date;
    private double hoursWorked;

    public StaffWorkLog(int id, int staffId, String date, double hoursWorked) {
        this.id = id;
        this.staffId = staffId;
        this.date = date;
        this.hoursWorked = hoursWorked;
    }

    public int getId() { return id; }
    public int getStaffId() { return staffId; }
    public String getDate() { return date; }
    public double getHoursWorked() { return hoursWorked; }
}
