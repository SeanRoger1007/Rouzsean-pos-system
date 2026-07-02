package com.refresh.pos.domain.staff;

public class Staff {
    private int id;
    private String name;
    private double dailySalary;
    private double defaultHours;

    public Staff(int id, String name, double dailySalary) {
        this(id, name, dailySalary, 12.0);
    }

    public Staff(int id, String name, double dailySalary, double defaultHours) {
        this.id = id;
        this.name = name;
        this.dailySalary = dailySalary;
        this.defaultHours = defaultHours;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getDailySalary() { return dailySalary; }
    public void setDailySalary(double dailySalary) { this.dailySalary = dailySalary; }
    public double getDefaultHours() { return defaultHours; }
    public void setDefaultHours(double defaultHours) { this.defaultHours = defaultHours; }
}
