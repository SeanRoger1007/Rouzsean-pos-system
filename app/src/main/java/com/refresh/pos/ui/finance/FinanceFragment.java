package com.refresh.pos.ui.finance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.refresh.pos.ui.MainActivity;
import com.refresh.pos.ui.component.UpdatableFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FinanceFragment extends UpdatableFragment {

    private GridView gridView;
    private Button btnExpenseTab, btnIncomeTab;
    private String currentType = "EXPENSE";
    private FinanceController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_finance, container, false);

        controller = FinanceController.getInstance();

        gridView = view.findViewById(R.id.categoryGridView);
        btnExpenseTab = view.findViewById(R.id.btn_expense_tab);
        btnIncomeTab = view.findViewById(R.id.btn_income_tab);

        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            view.findViewById(R.id.finance_root).setBackgroundColor(Color.parseColor("#121212"));
        } else {
            view.findViewById(R.id.finance_root).setBackgroundColor(Color.WHITE);
        }

        btnExpenseTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentType = "EXPENSE";
                updateTabs();
                refreshGrid();
            }
        });

        btnIncomeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentType = "INCOME";
                updateTabs();
                refreshGrid();
            }
        });

        updateTabs();
        refreshGrid();
        return view;
    }

    private void updateTabs() {
        if (currentType.equals("EXPENSE")) {
            btnExpenseTab.setAlpha(1.0f);
            btnIncomeTab.setAlpha(0.6f);
            btnExpenseTab.setBackgroundColor(Color.parseColor("#F44336"));
            btnIncomeTab.setBackgroundColor(Color.TRANSPARENT);
        } else {
            btnExpenseTab.setAlpha(0.6f);
            btnIncomeTab.setAlpha(1.0f);
            btnIncomeTab.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnExpenseTab.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void refreshGrid() {
        List<FinanceCategory> categories = controller.getCategories(currentType);
        gridView.setAdapter(new CategoryAdapter(getActivity(), categories));
        
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

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final FinanceCategory category = (FinanceCategory) parent.getItemAtPosition(position);
                if (category.getId() != -2) {
                    showDeleteConfirmDialog(category);
                }
                return true;
            }
        });
    }

    private void showDeleteConfirmDialog(final FinanceCategory category) {
        new AlertDialog.Builder(getActivity())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '" + category.getName() + "' and all its history?")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    controller.deleteCategory(category.getId());
                    refreshGrid();
                    Toast.makeText(getActivity(), "Category deleted", Toast.LENGTH_SHORT).show();
                    
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateAllFragments();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("New Category");
        final EditText input = new EditText(getActivity());
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_finance_entry, null);
        final EditText amountInput = view.findViewById(R.id.amount_input);
        final Button btnAction = view.findViewById(R.id.btn_action);
        final TextView dateDisplay = view.findViewById(R.id.date_display);
        final View datePickerContainer = view.findViewById(R.id.date_picker_container);
        final Spinner accountSpinner = view.findViewById(R.id.account_spinner);
        final EditText notesInput = view.findViewById(R.id.notes_input);

        final Calendar calendar = Calendar.getInstance();
        final String[] accounts = {"Cash Register", "GCash Balance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accounts);
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
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendar.set(year, month, day);
                        dateDisplay.setText(new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    Toast.makeText(getActivity(), "Transaction saved", Toast.LENGTH_SHORT).show();
                    
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateAllFragments();
                    }

                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @Override
    public void update() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.expense_income));
        }
        refreshGrid();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.expense_income));
        }
        update();
        
        // Apply entrance animation
        View container = getView().findViewById(R.id.finance_main_container);
        if (container != null) {
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_center);
            container.startAnimation(anim);
        }
    }

    private class CategoryAdapter extends BaseAdapter {
        private List<FinanceCategory> items;
        private Context context;

        public CategoryAdapter(Context context, List<FinanceCategory> items) {
            this.context = context;
            this.items = new ArrayList<>(items);
            this.items.add(new FinanceCategory(-2, "Custom", currentType));
        }

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FinanceCategory cat = items.get(position);
            return getCustomView(cat);
        }

        private View getCustomView(FinanceCategory cat) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(android.view.Gravity.CENTER);
            layout.setPadding(10, 20, 10, 20);

            TextView icon = new TextView(context);
            icon.setLayoutParams(new LinearLayout.LayoutParams(140, 140));
            icon.setGravity(android.view.Gravity.CENTER);
            icon.setTextColor(Color.WHITE);
            icon.setTextSize(28);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon.setElevation(4f);
            }
            
            String labelText = cat.getName();
            if (cat.getId() == -2) {
                icon.setText("+");
            } else {
                icon.setText(labelText.substring(0, 1).toUpperCase());
            }

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (cat.getId() == -2) {
                shape.setColor(Color.parseColor("#3F51B5")); // Blue for custom
            } else {
                shape.setColor(currentType.equals("EXPENSE") ? Color.parseColor("#E53935") : Color.parseColor("#43A047"));
            }
            shape.setStroke(4, Color.argb(40, 255, 255, 255));
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                icon.setBackground(shape);
            } else {
                icon.setBackgroundDrawable(shape);
            }

            TextView label = new TextView(context);
            label.setText(labelText);
            label.setGravity(android.view.Gravity.CENTER);
            label.setTextSize(12);
            label.setPadding(0, 12, 0, 0);
            label.setSingleLine(true);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            
            int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
            if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                label.setTextColor(Color.WHITE);
            } else {
                label.setTextColor(Color.parseColor("#333333"));
            }

            layout.addView(icon);
            layout.addView(label);
            return layout;
        }
    }
}
