package com.refresh.pos.ui.finance;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.finance.FinanceCategory;
import com.refresh.pos.domain.finance.FinanceController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FinanceActivity extends Activity {

    private GridView gridView;
    private Button btnExpense, btnIncome;
    private String currentType = "EXPENSE";
    private FinanceController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_finance);

        controller = FinanceController.getInstance();

        gridView = findViewById(R.id.categoryGridView);
        btnExpense = findViewById(R.id.btn_expense_tab);
        btnIncome = findViewById(R.id.btn_income_tab);

        initActionBar();
        initTabs();
        refreshGrid();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getString(R.string.expense_income));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        }
    }

    private void initTabs() {
        btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentType = "EXPENSE";
                btnExpense.setAlpha(1.0f);
                btnIncome.setAlpha(0.7f);
                refreshGrid();
            }
        });

        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentType = "INCOME";
                btnExpense.setAlpha(0.7f);
                btnIncome.setAlpha(1.0f);
                refreshGrid();
            }
        });
    }

    private void refreshGrid() {
        List<FinanceCategory> categories = controller.getCategories(currentType);
        gridView.setAdapter(new CategoryAdapter(this, categories));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FinanceCategory category = (FinanceCategory) parent.getItemAtPosition(position);
                if (category.getId() == -2) {
                    showAddCategoryDialog();
                } else {
                    showEntryDialog(category);
                }
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Category");
        final EditText input = new EditText(this);
        input.setHint("Category Name");
        builder.setView(input);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    controller.addCategory(name, currentType);
                    refreshGrid();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEntryDialog(final FinanceCategory category) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_finance_entry, null);
        final EditText amountInput = view.findViewById(R.id.amount_input);
        final Button btnAction = view.findViewById(R.id.btn_action);
        final TextView dateDisplay = view.findViewById(R.id.date_display);
        final View datePickerContainer = view.findViewById(R.id.date_picker_container);
        final Spinner accountSpinner = view.findViewById(R.id.account_spinner);
        final EditText notesInput = view.findViewById(R.id.notes_input);

        final Calendar calendar = Calendar.getInstance();
        final String[] accounts = {"Cash Register", "GCash Balance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(adapter);

        dateDisplay.setText(new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.getTime()));
        
        if (currentType.equals("EXPENSE")) {
            btnAction.setText(getString(R.string.add_expense));
            btnAction.setBackgroundColor(Color.parseColor("#F44336"));
        } else {
            btnAction.setText(getString(R.string.add_income));
            btnAction.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        datePickerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(FinanceActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendar.set(year, month, day);
                        dateDisplay.setText(new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double amount = Double.parseDouble(amountInput.getText().toString());
                    String date = DateTimeStrategy.getSQLDateFormat(calendar);
                    String account = accountSpinner.getSelectedItemPosition() == 0 ? "CASH" : "GCASH";
                    String notes = notesInput.getText().toString();
                    
                    controller.addTransaction(category.getId(), amount, date, account, notes, currentType);
                    Toast.makeText(FinanceActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(FinanceActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CategoryAdapter extends BaseAdapter {
        private List<FinanceCategory> items;
        private Context context;

        public CategoryAdapter(Context context, List<FinanceCategory> items) {
            this.context = context;
            this.items = new ArrayList<>(items);
            this.items.add(new FinanceCategory(-2, "Custom", currentType));
        }

        @Override
        public int getCount() { return items.size(); }
        @Override
        public Object getItem(int position) { return items.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FinanceCategory cat = items.get(position);
            return getCustomView(cat);
        }

        private View getCustomView(FinanceCategory cat) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(android.view.Gravity.CENTER);
            layout.setPadding(0, 10, 0, 10);

            TextView icon = new TextView(context);
            icon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            icon.setGravity(android.view.Gravity.CENTER);
            icon.setTextColor(Color.WHITE);
            icon.setTextSize(24);
            icon.setText(cat.getName().substring(0, 1).toUpperCase());
            if (cat.getId() == -2) icon.setText("+");

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (cat.getId() == -2) {
                shape.setColor(Color.parseColor("#3F51B5")); // Blue for custom
            } else {
                shape.setColor(currentType.equals("EXPENSE") ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                icon.setBackground(shape);
            } else {
                icon.setBackgroundDrawable(shape);
            }

            TextView label = new TextView(context);
            label.setText(cat.getName());
            label.setGravity(android.view.Gravity.CENTER);
            label.setTextSize(10);
            label.setPadding(0, 8, 0, 0);

            layout.addView(icon);
            layout.addView(label);
            return layout;
        }
    }
}
