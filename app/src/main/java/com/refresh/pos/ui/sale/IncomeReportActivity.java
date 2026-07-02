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
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.sale.SaleLedger;
import com.refresh.pos.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomeReportActivity extends Activity {

    private LinearLayout incomeContainer, financeIncomeContainer, loadingIncomeContainer;
    private TextView totalIncomeText, totalProfitsText;
    private Calendar currentStart, currentEnd;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_income_report);

        incomeContainer = findViewById(R.id.incomeContainer);
        financeIncomeContainer = findViewById(R.id.financeIncomeContainer);
        loadingIncomeContainer = findViewById(R.id.loadingIncomeContainer);
        totalIncomeText = findViewById(R.id.text_total_income);
        totalProfitsText = findViewById(R.id.text_total_profits);

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
            findViewById(R.id.income_report_root).setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            findViewById(R.id.income_header).setBackgroundColor(android.graphics.Color.parseColor("#1B5E20")); 
            
            // Refined Table Header and Footer
            int darkCardColor = android.graphics.Color.parseColor("#1A1A1A");
            int slateColor = android.graphics.Color.parseColor("#2C3E50");
            int tableHeaderColor = android.graphics.Color.parseColor("#263238"); // Darker gray for table column headers
            int white = android.graphics.Color.WHITE;
            int mutedGray = android.graphics.Color.parseColor("#AAAAAA");

            findViewById(R.id.income_footer).setBackgroundColor(darkCardColor);
            
            // Table Header Rows (Column Labels)
            int[] headerIds = {R.id.header_store_sales, R.id.header_finance_income, R.id.header_loading_profits};
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
            TextView labelSales = findViewById(R.id.label_store_sales);
            if (labelSales != null) {
                labelSales.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
                labelSales.setTextColor(white);
            }
            TextView labelFinance = findViewById(R.id.label_finance_income);
            if (labelFinance != null) {
                labelFinance.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
                labelFinance.setTextColor(white);
            }
            TextView labelLoading = findViewById(R.id.label_loading_profits);
            if (labelLoading != null) {
                labelLoading.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
                labelLoading.setTextColor(white);
            }

            if (totalProfitsText != null) totalProfitsText.setTextColor(white);
            totalIncomeText.setTextColor(white);
            ((TextView) findViewById(R.id.text_income_label)).setTextColor(white);
            
            incomeContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            financeIncomeContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            loadingIncomeContainer.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                performLoadData();
            }
        });
    }

    private void performLoadData() {
        final List<SaleItem> saleItems = new ArrayList<>();
        final List<IncomeItem> financeItems = new ArrayList<>();
        final List<LoadingProfitItem> loadingItems = new ArrayList<>();
        double grandTotal = 0;
        double grandProfit = 0;

        String startStr = DateTimeStrategy.getSQLDateFormat(currentStart);
        String endStr = DateTimeStrategy.getSQLDateFormat(currentEnd);

        // 1. Sales
        try {
            com.refresh.pos.domain.inventory.Inventory inventory = com.refresh.pos.domain.inventory.Inventory.getInstance();
            List<Sale> sales = SaleLedger.getInstance().getAllSaleDuring(currentStart, currentEnd);
            
            // OPTIMIZATION: Fetch all product costs once to avoid N+1 query problem
            Map<Integer, Double> productCosts = new HashMap<>();
            List<com.refresh.pos.domain.inventory.Product> allProducts = inventory.getProductCatalog().getAllProduct();
            for (com.refresh.pos.domain.inventory.Product p : allProducts) {
                List<com.refresh.pos.domain.inventory.ProductLot> lots = inventory.getStock().getProductLotByProductId(p.getId());
                double unitCost = (lots != null && !lots.isEmpty()) ? lots.get(lots.size() - 1).unitCost() : 0.0;
                productCosts.put(p.getId(), unitCost);
            }

            for (Sale s : sales) {
                Sale fullSale = SaleLedger.getInstance().getSaleById(s.getId());
                String seller = fullSale.getSellerName() != null ? fullSale.getSellerName() : "Owner";
                
                for (com.refresh.pos.domain.inventory.LineItem item : fullSale.getAllLineItem()) {
                    Double unitCost = productCosts.get(item.getProduct().getId());
                    if (unitCost == null) unitCost = 0.0;
                    
                    double totalCost = unitCost * item.getQuantity();

                    String qtyStr;
                    if (item.getProduct().isPack()) {
                        if (item.isPieceSale()) {
                            int pieces = (int) Math.round(item.getQuantity() * item.getProduct().getPiecesPerPack());
                            qtyStr = pieces + " pcs";
                        } else {
                            qtyStr = (item.getQuantity() == (int)item.getQuantity() ? String.valueOf((int)item.getQuantity()) : String.valueOf(item.getQuantity())) + " pk";
                        }
                    } else {
                        qtyStr = (item.getQuantity() == (int)item.getQuantity() ? String.valueOf((int)item.getQuantity()) : String.valueOf(item.getQuantity()));
                    }
                    
                    saleItems.add(new SaleItem(
                        seller,
                        item.getProduct().getName(),
                        qtyStr,
                        totalCost,
                        item.getTotalPriceAtSale(),
                        fullSale.getEndTime()
                    ));
                    grandTotal += item.getTotalPriceAtSale();
                    grandProfit += (item.getTotalPriceAtSale() - totalCost);
                }
            }
        } catch (NoDaoSetException e) {}

        // 2. Finance Transactions (Incomes)
        List<com.refresh.pos.domain.finance.FinanceTransaction> transactions = FinanceController.getInstance().getTransactionsBetween("INCOME", startStr, endStr);
        for (com.refresh.pos.domain.finance.FinanceTransaction t : transactions) {
            String note = (t.getNotes() != null && !t.getNotes().isEmpty()) ? t.getNotes() : "";
            financeItems.add(new IncomeItem(t.getCategoryName(), t.getDate(), t.getAmount(), note));
            grandTotal += t.getAmount();
            // For general income, we assume profit = income (no cost associated)
            grandProfit += t.getAmount();
        }

        // 3. Loading Profits
        try {
            List<com.refresh.pos.domain.loading.LoadingTransaction> loadingSales = com.refresh.pos.domain.loading.LoadingController.getInstance().getLoadingProfits(startStr, endStr);
            List<com.refresh.pos.domain.loading.LoadingCategory> cats = com.refresh.pos.domain.loading.LoadingController.getInstance().getAllCategories();
            
            for (com.refresh.pos.domain.loading.LoadingTransaction t : loadingSales) {
                String catName = "Unknown";
                for (com.refresh.pos.domain.loading.LoadingCategory c : cats) {
                    if (c.getId() == t.getCategoryId()) {
                        catName = c.getName();
                        break;
                    }
                }
                
                String subName = "Custom Load";
                if (t.getSubCategoryId() != -1) {
                    List<com.refresh.pos.domain.loading.LoadingSubCategory> subs = com.refresh.pos.domain.loading.LoadingController.getInstance().getSubCategories(t.getCategoryId());
                    for (com.refresh.pos.domain.loading.LoadingSubCategory s : subs) {
                        if (s.getId() == t.getSubCategoryId()) {
                            subName = s.getName();
                            break;
                        }
                    }
                }
                
                loadingItems.add(new LoadingProfitItem(catName, subName, t.getDate(), t.getAmountSold()));
                grandTotal += t.getAmountSold();
                grandProfit += (t.getAmountSold() - t.getAmountDeducted());
            }
        } catch (NoDaoSetException e) {}

        final double finalGrandTotal = grandTotal;
        final double finalGrandProfit = grandProfit;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                populateContainer(incomeContainer, new SaleAdapter(saleItems));
                populateContainer(financeIncomeContainer, new IncomeAdapter(financeItems));
                populateContainer(loadingIncomeContainer, new LoadingProfitAdapter(loadingItems));
                totalIncomeText.setText(String.format(Locale.US, "₱%.2f", finalGrandTotal));
                if (totalProfitsText != null) totalProfitsText.setText(String.format(Locale.US, "₱%.2f", finalGrandProfit));
            }
        });
    }

    private static class LoadingProfitItem {
        String category, subCategory, dateTime;
        double amount;
        LoadingProfitItem(String c, String s, String dt, double a) { category = c; subCategory = s; dateTime = dt; amount = a; }
    }

    private class LoadingProfitAdapter extends BaseAdapter {
        private List<LoadingProfitItem> items;
        LoadingProfitAdapter(List<LoadingProfitItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_loading_profit_item, parent, false);
            }
            LoadingProfitItem item = items.get(position);
            TextView tvCat = (TextView) convertView.findViewById(R.id.text_loading_cat);
            TextView tvSub = (TextView) convertView.findViewById(R.id.text_loading_sub);
            TextView tvDate = (TextView) convertView.findViewById(R.id.text_loading_date);
            TextView tvAmount = (TextView) convertView.findViewById(R.id.text_loading_amount);

            tvCat.setText(item.category);
            tvSub.setText(item.subCategory);
            tvDate.setText(item.dateTime);
            tvAmount.setText(String.format(Locale.US, "₱%.2f", item.amount));

            int theme = com.refresh.pos.domain.ThemeController.getInstance(IncomeReportActivity.this).getTheme();
            if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                int white = android.graphics.Color.WHITE;
                tvCat.setTextColor(white);
                tvSub.setTextColor(white);
                tvDate.setTextColor(white);
                tvAmount.setTextColor(white);
            }

            return convertView;
        }
    }

    private static class SaleItem {
        String seller, product, qty, dateTime;
        double cost, price;
        SaleItem(String s, String p, String q, double co, double pr, String dt) { 
            seller = s; product = p; qty = q; cost = co; price = pr; dateTime = dt; 
        }
    }

    private static class IncomeItem {
        String description;
        String dateTime;
        double amount;
        String note;
        IncomeItem(String d, String dt, double a, String n) { description = d; dateTime = dt; amount = a; note = n; }
    }

    private class SaleAdapter extends BaseAdapter {
        private List<SaleItem> items;
        SaleAdapter(List<SaleItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_store_sale_item, parent, false);
            }
            SaleItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_sale_seller)).setText(item.seller);
            ((TextView) convertView.findViewById(R.id.text_sale_product)).setText(item.product);
            ((TextView) convertView.findViewById(R.id.text_sale_qty)).setText(item.qty);
            ((TextView) convertView.findViewById(R.id.text_sale_datetime)).setText(item.dateTime);
            ((TextView) convertView.findViewById(R.id.text_sale_cost)).setText(String.format(Locale.US, "₱%.2f", item.cost));
            ((TextView) convertView.findViewById(R.id.text_sale_price)).setText(String.format(Locale.US, "₱%.2f", item.price));

            int theme = com.refresh.pos.domain.ThemeController.getInstance(IncomeReportActivity.this).getTheme();
            if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                int white = android.graphics.Color.WHITE;
                ((TextView) convertView.findViewById(R.id.text_sale_seller)).setTextColor(white);
                ((TextView) convertView.findViewById(R.id.text_sale_product)).setTextColor(white);
                ((TextView) convertView.findViewById(R.id.text_sale_qty)).setTextColor(white);
                ((TextView) convertView.findViewById(R.id.text_sale_datetime)).setTextColor(white);
                ((TextView) convertView.findViewById(R.id.text_sale_cost)).setTextColor(white);
            }

            return convertView;
        }
    }

    private class IncomeAdapter extends BaseAdapter {
        private List<IncomeItem> items;
        IncomeAdapter(List<IncomeItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_income_item, parent, false);
            }
            final IncomeItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.text_income_desc)).setText(item.description);
            ((TextView) convertView.findViewById(R.id.text_income_date)).setText(item.dateTime);
            ((TextView) convertView.findViewById(R.id.text_income_amount)).setText(String.format(Locale.US, "₱%.2f", item.amount));
            
            View btnNote = convertView.findViewById(R.id.btn_note);
            View spacer = convertView.findViewById(R.id.spacer_note);
            if (item.note != null && !item.note.isEmpty()) {
                btnNote.setVisibility(View.VISIBLE);
                if (spacer != null) spacer.setVisibility(View.GONE);
                btnNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new android.app.AlertDialog.Builder(IncomeReportActivity.this)
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
