package com.refresh.pos.domain.staff;

import com.refresh.pos.techicalservices.staff.StaffDao;
import java.util.List;

public class StaffController {
    private static StaffController instance;
    private StaffDao staffDao;

    private StaffController(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    public static void init(StaffDao staffDao) {
        instance = new StaffController(staffDao);
    }

    public static StaffController getInstance() {
        return instance;
    }

    public List<Staff> getAllStaff() {
        return staffDao.getAllStaff();
    }

    public boolean updateStaff(Staff staff) {
        return staffDao.updateStaff(staff);
    }

    public boolean hasWorkedToday(int staffId, String date) {
        List<StaffWorkLog> logs = staffDao.getWorkLogsForDate(date);
        for (StaffWorkLog log : logs) {
            if (log.getStaffId() == staffId) return true;
        }
        return false;
    }

    public List<StaffWorkLog> getWorkLogsForDate(String date) {
        return staffDao.getWorkLogsForDate(date);
    }

    public void toggleWork(int staffId, String date, boolean worked) {
        if (worked) {
            if (!hasWorkedToday(staffId, date)) {
                // Find staff to get their default hours
                double defaultHrs = 12.0;
                for (Staff s : getAllStaff()) {
                    if (s.getId() == staffId) {
                        defaultHrs = s.getDefaultHours();
                        break;
                    }
                }
                staffDao.addWorkLog(staffId, date, defaultHrs);
            }
        } else {
            staffDao.removeWorkLog(staffId, date);
        }
    }

    public void toggleWork(int staffId, String date, boolean worked, double hoursWorked) {
        if (worked) {
            if (!hasWorkedToday(staffId, date)) {
                staffDao.addWorkLog(staffId, date, hoursWorked);
            }
        } else {
            staffDao.removeWorkLog(staffId, date);
        }
    }

    public int getStaffWorkedCount(String date) {
        return staffDao.getWorkLogsForDate(date).size();
    }

    public String getStaffNamesWorkedOn(String date) {
        List<StaffWorkLog> logs = staffDao.getWorkLogsForDate(date);
        if (logs.isEmpty()) return "None";

        List<Staff> allStaff = getAllStaff();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logs.size(); i++) {
            int staffId = logs.get(i).getStaffId();
            for (Staff s : allStaff) {
                if (s.getId() == staffId) {
                    sb.append(s.getName());
                    if (i < logs.size() - 1) sb.append(", ");
                    break;
                }
            }
        }
        return sb.toString();
    }

    public double getTotalSalaryForDate(String date) {
        List<StaffWorkLog> logs = staffDao.getWorkLogsForDate(date);
        if (logs.isEmpty()) return 0;

        List<Staff> allStaff = getAllStaff();
        double total = 0;
        for (StaffWorkLog log : logs) {
            for (Staff s : allStaff) {
                if (s.getId() == log.getStaffId()) {
                    // Salary = (Daily Salary / Default Hours) * Hours Worked
                    double divisor = s.getDefaultHours() > 0 ? s.getDefaultHours() : 12.0;
                    total += (s.getDailySalary() / divisor) * log.getHoursWorked();
                    break;
                }
            }
        }
        return total;
    }

    public double getTotalSalaryBetween(String startDate, String endDate) {
        List<StaffWorkLog> logs = staffDao.getWorkLogsBetween(startDate, endDate);
        if (logs.isEmpty()) return 0;

        List<Staff> allStaff = getAllStaff();
        double total = 0;
        for (StaffWorkLog log : logs) {
            for (Staff s : allStaff) {
                if (s.getId() == log.getStaffId()) {
                    // Salary = (Daily Salary / Default Hours) * Hours Worked
                    double divisor = s.getDefaultHours() > 0 ? s.getDefaultHours() : 12.0;
                    total += (s.getDailySalary() / divisor) * log.getHoursWorked();
                    break;
                }
            }
        }
        return total;
    }

    public List<StaffWorkLog> getWorkLogsBetween(String start, String end) {
        return staffDao.getWorkLogsBetween(start, end);
    }

    public int addStaff(String name, double dailySalary, double defaultHours) {
        return staffDao.addStaff(name, dailySalary, defaultHours);
    }

    public void deleteStaff(int id) {
        staffDao.deleteStaff(id);
    }
}
