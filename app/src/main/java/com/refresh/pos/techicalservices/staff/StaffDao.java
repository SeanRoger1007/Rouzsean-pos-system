package com.refresh.pos.techicalservices.staff;

import com.refresh.pos.domain.staff.Staff;
import com.refresh.pos.domain.staff.StaffWorkLog;

import java.util.List;

public interface StaffDao {
	List<Staff> getAllStaff();
	boolean updateStaff(Staff staff);
	List<StaffWorkLog> getWorkLogsForDate(String date);
	List<StaffWorkLog> getWorkLogsBetween(String start, String end);
	void addWorkLog(int staffId, String date, double hoursWorked);
	void removeWorkLog(int staffId, String date);
	int addStaff(String name, double dailySalary, double defaultHours);
	void deleteStaff(int id);
}
