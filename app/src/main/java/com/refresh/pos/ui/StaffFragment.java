package com.refresh.pos.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.staff.Staff;
import com.refresh.pos.domain.staff.StaffController;
import com.refresh.pos.domain.staff.StaffWorkLog;
import com.refresh.pos.ui.component.UpdatableFragment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaffFragment extends UpdatableFragment {

    private LinearLayout staffContainer;
    private TextView textDate;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_staff, container, false);

        staffContainer = view.findViewById(R.id.staff_container);
        textDate = view.findViewById(R.id.text_date);

        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            view.findViewById(R.id.staff_root).setBackgroundColor(Color.parseColor("#121212"));
            textDate.setBackgroundColor(Color.parseColor("#1A1A1A"));
            textDate.setTextColor(Color.WHITE);
        } else {
            view.findViewById(R.id.staff_root).setBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        view.findViewById(R.id.btn_add_staff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStaffDialog();
            }
        });

        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        
        refreshList();
        return view;
    }

    private void showDatePicker() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                refreshList();
            }
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showAddStaffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Register New Staff");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView labelName = new TextView(getActivity());
        labelName.setText("Staff Name:");
        layout.addView(labelName);
        final EditText nameInput = new EditText(getActivity());
        nameInput.setHint(R.string.name);
        layout.addView(nameInput);

        TextView labelSalary = new TextView(getActivity());
        labelSalary.setText("Daily Salary (₱):");
        layout.addView(labelSalary);
        final EditText salaryInput = new EditText(getActivity());
        salaryInput.setHint(R.string.salary);
        salaryInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        salaryInput.setText("300");
        layout.addView(salaryInput);

        TextView labelHours = new TextView(getActivity());
        labelHours.setText("Baseline Work Hours:");
        layout.addView(labelHours);
        final EditText hoursInput = new EditText(getActivity());
        hoursInput.setHint("Default Hours (e.g. 12)");
        hoursInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        hoursInput.setText("12.0");
        layout.addView(hoursInput);

        builder.setView(layout);

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                String salaryStr = salaryInput.getText().toString();
                String hoursStr = hoursInput.getText().toString();
                if (!name.isEmpty() && !salaryStr.isEmpty() && !hoursStr.isEmpty()) {
                    StaffController.getInstance().addStaff(name, Double.parseDouble(salaryStr), Double.parseDouble(hoursStr));
                    notifyUpdate();
                } else {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void refreshList() {
        if (staffContainer == null) return;
        staffContainer.removeAllViews();
        String dateStr = DateTimeStrategy.getSQLDateFormat(selectedDate);
        textDate.setText(dateStr);
        
        List<Staff> staffList = StaffController.getInstance().getAllStaff();
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

        for (final Staff staff : staffList) {
            View card = LayoutInflater.from(getActivity()).inflate(R.layout.list_staff_item, staffContainer, false);
            
            TextView nameText = card.findViewById(R.id.staff_name);
            TextView salaryText = card.findViewById(R.id.staff_salary);
            TextView hoursText = card.findViewById(R.id.text_hours);
            CheckBox workedCheckbox = card.findViewById(R.id.worked_today_checkbox);
            View editBtn = card.findViewById(R.id.btn_edit_staff);

            if (isDark) {
                card.setBackgroundColor(Color.parseColor("#1A1A1A"));
                nameText.setTextColor(Color.WHITE);
                salaryText.setTextColor(Color.parseColor("#999999"));
                workedCheckbox.setTextColor(Color.parseColor("#CCCCCC"));
                hoursText.setTextColor(Color.parseColor("#81C784")); // Better green for dark
            }

            nameText.setText(staff.getName());
            salaryText.setText(String.format(Locale.US, "₱%.2f / %.1fh", staff.getDailySalary(), staff.getDefaultHours()));
            
            boolean worked = StaffController.getInstance().hasWorkedToday(staff.getId(), dateStr);
            workedCheckbox.setChecked(worked);
            
            if (worked) {
                List<StaffWorkLog> logs = StaffController.getInstance().getWorkLogsForDate(dateStr);
                for (StaffWorkLog log : logs) {
                    if (log.getStaffId() == staff.getId()) {
                        double earned = (staff.getDailySalary() / staff.getDefaultHours()) * log.getHoursWorked();
                        hoursText.setText(String.format(Locale.US, "Worked: %.1f hrs (Earned: ₱%.2f)", log.getHoursWorked(), earned));
                        hoursText.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            } else {
                hoursText.setVisibility(View.GONE);
            }

            View logHoursBtn = card.findViewById(R.id.btn_log_hours);
            View deleteBtn = card.findViewById(R.id.btn_delete_staff);

            logHoursBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHoursDialog(staff);
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDialog(staff);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmDialog(staff);
                }
            });

            staffContainer.addView(card);
        }
    }

    private void showDeleteConfirmDialog(final Staff staff) {
        new AlertDialog.Builder(getActivity())
            .setTitle("Delete Staff")
            .setMessage("Are you sure you want to delete " + staff.getName() + "? All their work logs will also be removed.")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StaffController.getInstance().deleteStaff(staff.getId());
                    notifyUpdate();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showHoursDialog(final Staff staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Log Hours for: " + staff.getName());
        
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView labelActual = new TextView(getActivity());
        labelActual.setText("Hours Worked Today:");
        layout.addView(labelActual);

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        // Find existing hours if any
        String dateStr = DateTimeStrategy.getSQLDateFormat(selectedDate);
        List<StaffWorkLog> logs = StaffController.getInstance().getWorkLogsForDate(dateStr);
        double currentHours = staff.getDefaultHours();
        for (StaffWorkLog log : logs) {
            if (log.getStaffId() == staff.getId()) {
                currentHours = log.getHoursWorked();
                break;
            }
        }
        input.setText(String.valueOf(currentHours));
        layout.addView(input);

        builder.setView(layout);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    double hours = Double.parseDouble(input.getText().toString());
                    StaffController.getInstance().toggleWork(staff.getId(), DateTimeStrategy.getSQLDateFormat(selectedDate), true, hours);
                    notifyUpdate();
                } catch (Exception e) {}
            }
        });
        builder.setNegativeButton("Remove Work Log", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StaffController.getInstance().toggleWork(staff.getId(), DateTimeStrategy.getSQLDateFormat(selectedDate), false);
                notifyUpdate();
            }
        });
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showEditDialog(final Staff staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Staff: " + staff.getName());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView labelName = new TextView(getActivity());
        labelName.setText("Staff Name:");
        layout.addView(labelName);
        final EditText nameInput = new EditText(getActivity());
        nameInput.setHint(R.string.name);
        nameInput.setText(staff.getName());
        layout.addView(nameInput);

        TextView labelSalary = new TextView(getActivity());
        labelSalary.setText("Daily Salary (₱):");
        layout.addView(labelSalary);
        final EditText salaryInput = new EditText(getActivity());
        salaryInput.setHint(R.string.salary);
        salaryInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        salaryInput.setText(String.valueOf(staff.getDailySalary()));
        layout.addView(salaryInput);

        TextView labelHours = new TextView(getActivity());
        labelHours.setText("Baseline Work Hours:");
        layout.addView(labelHours);
        final EditText hoursInput = new EditText(getActivity());
        hoursInput.setHint("Default Hours");
        hoursInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        hoursInput.setText(String.valueOf(staff.getDefaultHours()));
        layout.addView(hoursInput);

        builder.setView(layout);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = nameInput.getText().toString();
                String salaryStr = salaryInput.getText().toString();
                String hoursStr = hoursInput.getText().toString();
                if (!newName.isEmpty() && !salaryStr.isEmpty() && !hoursStr.isEmpty()) {
                    staff.setName(newName);
                    staff.setDailySalary(Double.parseDouble(salaryStr));
                    staff.setDefaultHours(Double.parseDouble(hoursStr));
                    StaffController.getInstance().updateStaff(staff);
                    notifyUpdate();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void update() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.staff));
        }
        refreshList();
    }

    private void notifyUpdate() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateAllFragments();
        } else {
            refreshList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.staff));
        }
        update();
        
        // Apply entrance animation
        View container = getView().findViewById(R.id.staff_main_container);
        if (container != null) {
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_center);
            container.startAnimation(anim);
        }
    }
}
