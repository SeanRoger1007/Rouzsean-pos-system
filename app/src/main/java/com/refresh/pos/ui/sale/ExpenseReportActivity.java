package com.refresh.pos.ui.sale;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.finance.FinanceController;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.ProductLot;
import com.refresh.pos.domain.staff.StaffController;
import com.refresh.pos.domain.staff.StaffWorkLog;
import com.refresh.pos.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseReportActivity extends Activity {

    private LinearLayout staffWorkContainer, restockContainer, financeExpenseContainer, loadingRestockContainer, ownerUseContainer;
    private TextView totalExpenseText, totalOwnerUseText;
    private Calendar currentStart, currentEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_expense_report);

        staffWorkContainer = findViewById(R.id.staffWorkContainer);
        restockContainer = findViewById(R.id.restockContainer);
        financeExpenseContainer = findViewById(R.id.financeExpenseContainer);
        loadingRestockContainer = findViewById(R.id.loadingRestockContainer);
        ownerUseContainer = findViewById(R.id.ownerUseContainer);
        totalExpenseText = findViewById(R.id.text_total_expense);
        totalOwnerUseText = findViewById(R.id.text_total_owner_use);

        long startMillis = getIntent().getLongExtra("start", 0);
        long endMillis = getIntent().getLongExtra("end", 0);
        
        currentStart = Calendar.getInstance();
        currentStart.setTimeInMillis(startMillis);
        currentEnd = Calendar.getInstance();
        currentEnd.setTimeInMillis(endMillis);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadData();
        applyDarkMode();
    }

    private void applyDarkMode() {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            findViewById(R.id.expense_report_root).setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            findViewById(R.id.expense_header).setBackgroundColor(android.graphics.Color.parseColor("#B71C1C")); 
            
            // Refined Table Header and Footer
            int darkCardColor = android.graphics.Color.parseColor("#1A1A1A");
            int slateColor = android.graphics.Color.parseColor("#2C3E50");
            int tableHeaderColor = android.graphics.Color.parseColor("#263238");
            int white = android.graphics.Color.WHITE;

            findViewById(R.id.expense_footer).setBackgroundColor(darkCardColor);

            // Table Header Rows (Column Labels)
            int[] headerIds = {R.id.header_staff_salary, R.id.header_inventory_restock, 
                              R.id.header_rouzsean_use, R.id.header_finance_expense, R.id.header_loading_restocks};
            for (int id : headerIds) {
                View header = findViewById(id);
                if (header != null) {
                    header.setBackgroundColor(tableHeaderColor);
                    if (header instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) header;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            if (child instanceof TextView) {
                                ((TextView) child).setTextColor(white);
                            }
                        }
                    }
                }
            }

            // Section Labels
            int[] labelIds = {R.id.label_staff_salary, R.id.label_inventory_restock, 
                              R.id.label_rouzsean_use, R.id.label_finance_expense, R.id.label_loading_restocks};
            for (int id : labelIds) {
                TextView label = findViewById(id);
                if (label != null) {
                    label.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
                    label.setTextColor(white);
                }
            }

            if (totalOwnerUseText != null) totalOwnerUseText.setTextColor(white);
            totalExpenseText.setTextColor(white);
            ((TextView) findViewById(R.id.text_expense_label)).setTextColor(white);

            staffWorkContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            restockContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            financeExpenseContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            loadingRestockContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            ownerUseContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
    }

    private void populateContainer(LinearLayout container, BaseAdapter adapter) {
        container.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, container);
            container.addView(view);
            if (i < adapter.getCount() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
                container.addView(divider);
            }
        }
    }

    private void loadData() {
        List<StaffItem> staffItems = new ArrayList<>();
        List<RestockItem> restockItems = new ArrayList<>();
        List<FinanceItem> financeItems = new ArrayList<>();
        List<OwnerItem> ownerItems = new ArrayList<>();
        List<LoadingRestockItem> loadingRestockItems = new ArrayList<>();
        double grandTotal = 0;
        double ownerTotal = 0;

        String startStr = DateTimeStrategy.getSQLDateFormat(currentStart);
        String endStr = DateTimeStrategy.getSQLDateFormat(currentEnd);

        // 1. Staff Salaries
        List<StaffWorkLog> logs = StaffController.getInstance().getWorkLogsBetween(startStr, endStr);
        List<com.refresh.pos.domain.staff.Staff> staffList = StaffController.getInstance().getAllStaff();
        for (StaffWorkLog log : logs) {
            for (com.refresh.pos.domain.staff.Staff s : staffList) {
                if (s.getId() == log.getStaffId()) {
                    double amount = (s.getDailySalary() / s.getDefaultHours()) * log.getHoursWorked();
                    staffItems.add(new StaffItem(s.getName(), log.getHoursWorked(), log.getDate(), amount));
                    grandTotal += amount;
                    break;
                }
            }
        }

        // 2. Product Lots (Restock)
        try {
            List<ProductLot> lots = Inventory.getInstance().getStock().getAllProductLotDuring(currentStart, currentEnd);
            for (ProductLot lot : lots) {
                double amount = lot.unitCost() * lot.getQuantity();
                restockItems.add(new RestockItem(lot.getProduct().getName(), lot.getQuantity(), lot.getDateAdded(), amount));
                grandTotal += amount;
            }
        } catch (NoDaoSetException e) {}

        // 3. Finance Transactions
        List<com.refresh.pos.domain.finance.FinanceTransaction> transactions = FinanceController.getInstance().getTransactionsBetween("EXPENSE", startStr, endStr);
        for (com.refresh.pos.domain.finance.FinanceTransaction t : transactions) {
            String catName = t.getCategoryName();
            if (catName == null) catName = "";

            if ("OWNER_USE".equalsIgnoreCase(catName)) {
                // Notes format: [TYPE] [QTY] | Description
                String note = t.getNotes();
                String desc = (note != null && !note.isEmpty()) ? note : "Owner Use";
                String qtyStr = "0";
                
                if (note != null && note.contains("|")) {
                    String[] parts = note.split("\\|");
                    desc = parts[1].trim();
                    String meta = parts[0].trim(); // [TYPE] [QTY]
                    
                    if (meta.contains("[PRODUCT_USE]")) {
                        qtyStr = meta.substring(meta.lastIndexOf("[") + 1, meta.lastIndexOf("]"));
                    } else if (meta.contains("[MONEY_USE]") || meta.contains("[LOADING_USE]")) {
                        qtyStr = "-";
                    }
                }
                
                ownerItems.add(new OwnerItem(desc, qtyStr, t.getDate(), t.getAmount()));
                ownerTotal += t.getAmount();

            } else if (!"Inventory".equalsIgnoreCase(catName)) {
                // Skip "Inventory" to avoid double counting with the Restock section
                String note = (t.getNotes() != null && !t.getNotes().isEmpty()) ? t.getNotes() : "";
                financeItems.add(new FinanceItem(catName, t.getDate(), t.getAmount(), note));
                grandTotal += t.getAmount();
            }
        }

        // 4. Loading Restocks
        try {
            List<com.refresh.pos.domain.loading.LoadingTransaction> loadingRestocks = com.refresh.pos.domain.loading.LoadingController.getInstance().getLoadingRestocks(startStr, endStr);
            List<com.refresh.pos.domain.loading.LoadingCategory> cats = com.refresh.pos.domain.loading.LoadingController.getInstance().getAllCategories();
            
            for (com.refresh.pos.domain.loading.LoadingTransaction t : loadingRestocks) {
                String catName = "Unknown";
                for (com.refresh.pos.domain.loading.LoadingCategory c : cats) {
                    if (c.getId() == t.getCategoryId()) {
                        catName = c.getName();
                        break;
                    }
                }
                loadingRestockItems.add(new LoadingRestockItem(catName, t.getDate(), t.getAmountPaid()));
                grandTotal += t.getAmountPaid();
            }
        } catch (NoDaoSetException e) {}

        populateContainer(staffWorkContainer, new StaffAdapter(staffItems));
        populateContainer(restockContainer, new RestockAdapter(restockItems));
        populateContainer(financeExpenseContainer, new FinanceAdapter(financeItems));
        populateContainer(loadingRestockContainer, new LoadingRestockAdapter(loadingRestockItems));
        populateContainer(ownerUseContainer, new OwnerAdapter(ownerItems));

        totalExpenseText.setText(String.format(Locale.US, "₱%.2f", grandTotal));
        if (totalOwnerUseText != null) {
            totalOwnerUseText.setText(String.format(Locale.US, "₱%.2f", ownerTotal));
        }
    }

    private static class LoadingRestockItem {
        String category, dateTime;
        double amount;
        LoadingRestockItem(String c, String dt, double a) { category = c; dateTime = dt; amount = a; }
    }

    private class LoadingRestockAdapter extends BaseAdapter {
        private List<LoadingRestockItem> items;
        LoadingRestockAdapter(List<LoadingRestockItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_loading_restock_item, parent, false);
            }
            LoadingRestockItem item = items.get(position);
            TextView tvCat = (TextView) convertView.findViewById(R.id.text_loading_cat);
            TextView tvDate = (TextView) convertView.findViewById(R.id.text_loading_date);
            TextView tvAmount = (TextView) convertView.findViewById(R.id.text_loading_amount);

            tvCat.setText(item.category);
            tvDate.setText(item.dateTime);
            tvAmount.setText(String.format(Locale.US, "₱%.2f", item.amount));

            int theme = com.refresh.pos.domain.ThemeController.getInstance(ExpenseReportActivity.this).getTheme();
            if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                int white = android.graphics.Color.WHITE;
                tvCat.setTextColor(white);
                tvDate.setTextColor(white);
                tvAmount.setTextColor(white);
            }

            return convertView;
        }
    }

    private static class OwnerItem {
        String description, qty, dateTime;
        double amount;
        OwnerItem(String d, String q, String dt, double a) { description = d; qty = q; dateTime = dt; amount = a; }
    }

    private class OwnerAdapter extends BaseAdapter {
        private List<OwnerItem> items;
        OwnerAdapter(List<OwnerItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_expense_item, parent, false);
            }
            OwnerItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_expense_name)).setText(item.description);
            ((TextView) convertView.findViewById(R.id.text_expense_qty)).setText(item.qty);
            ((TextView) convertView.findViewById(R.id.text_expense_date)).setText(item.dateTime);
            ((TextView) convertView.findViewById(R.id.text_expense_amount)).setText(String.format(Locale.US, "₱%.2f", item.amount));
            return convertView;
        }
    }

    private static class StaffItem {
        String name, date;
        double hrs, amount;
        StaffItem(String n, double h, String d, double a) { name = n; hrs = h; date = d; amount = a; }
    }

    private static class RestockItem {
        String name, date;
        double qty, amount;
        RestockItem(String n, double q, String d, double a) { name = n; qty = q; date = d; amount = a; }
    }

    private static class FinanceItem {
        String name, date, note;
        double amount;
        FinanceItem(String n, String d, double a, String nt) { name = n; date = d; amount = a; note = nt; }
    }

    private class StaffAdapter extends BaseAdapter {
        private List<StaffItem> items;
        StaffAdapter(List<StaffItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_staff_work_item, parent, false);
            }
            StaffItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_staff_name)).setText(item.name);
            ((TextView) convertView.findViewById(R.id.text_staff_hrs)).setText(String.format(Locale.US, "%.1f hrs", item.hrs));
            ((TextView) convertView.findViewById(R.id.text_staff_date)).setText(item.date);
            ((TextView) convertView.findViewById(R.id.text_staff_amount)).setText(String.format(Locale.US, "₱%.2f", item.amount));
            return convertView;
        }
    }

    private class RestockAdapter extends BaseAdapter {
        private List<RestockItem> items;
        RestockAdapter(List<RestockItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_expense_item, parent, false);
            }
            RestockItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_expense_name)).setText(item.name);
            ((TextView) convertView.findViewById(R.id.text_expense_qty)).setText(String.valueOf((int)item.qty));
            ((TextView) convertView.findViewById(R.id.text_expense_date)).setText(item.date);
            ((TextView) convertView.findViewById(R.id.text_expense_amount)).setText(String.format(Locale.US, "₱%.2f", item.amount));
            return convertView;
        }
    }

    private class FinanceAdapter extends BaseAdapter {
        private List<FinanceItem> items;
        FinanceAdapter(List<FinanceItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_expense_item, parent, false);
            }
            final FinanceItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_expense_name)).setText(item.name);
            ((TextView) convertView.findViewById(R.id.text_expense_date)).setText(item.date);
            ((TextView) convertView.findViewById(R.id.text_expense_amount)).setText(String.format(Locale.US, "₱%.2f", item.amount));
            
            View btnNote = convertView.findViewById(R.id.btn_note);
            View spacer = convertView.findViewById(R.id.spacer_note);
            if (item.note != null && !item.note.isEmpty()) {
                btnNote.setVisibility(View.VISIBLE);
                if (spacer != null) spacer.setVisibility(View.GONE);
                btnNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new android.app.AlertDialog.Builder(ExpenseReportActivity.this)
                            .setTitle("Note")
                            .setMessage(item.note)
                            .setPositiveButton("Close", null)
                            .show();
                    }
                });
            } else {
                btnNote.setVisibility(View.GONE);
                if (spacer != null) spacer.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}
