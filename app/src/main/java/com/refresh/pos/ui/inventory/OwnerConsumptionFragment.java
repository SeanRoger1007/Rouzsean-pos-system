package com.refresh.pos.ui.inventory;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.inventory.ProductLot;
import com.refresh.pos.domain.loading.LoadingController;
import com.refresh.pos.domain.loading.LoadingCategory;
import com.refresh.pos.domain.finance.FinanceController;
import com.refresh.pos.domain.finance.FinanceCategory;
import com.refresh.pos.domain.capital.CapitalController;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OwnerConsumptionFragment extends UpdatableFragment {

    private EditText searchBox;
    private LinearLayout productListContainer;
    private EditText inputWithdrawAmount;
    private Spinner spinnerAccount;
    private Spinner spinnerLoadingProvider;
    private EditText inputLoadingAmount;
    private Button btnConfirm;
    
    private List<Product> allProducts;
    private List<LoadingCategory> loadingCategories;
    private Map<Integer, EditText> qtyInputs = new HashMap<>();
    private Inventory inventory;
    private LoadingController loadingController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_owner_consumption, container, false);

        try {
            inventory = Inventory.getInstance();
            allProducts = inventory.getProductCatalog().getAllProduct();
            loadingController = LoadingController.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        searchBox = view.findViewById(R.id.searchBox);
        productListContainer = view.findViewById(R.id.product_list_container);
        inputWithdrawAmount = view.findViewById(R.id.input_withdraw_amount);
        spinnerAccount = view.findViewById(R.id.spinner_account);
        spinnerLoadingProvider = view.findViewById(R.id.spinner_loading_provider);
        inputLoadingAmount = view.findViewById(R.id.input_loading_amount);
        btnConfirm = view.findViewById(R.id.btn_confirm);

        String[] accounts = {"Cash Register", "GCash Balance"};
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accounts);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountAdapter);

        updateLoadingProviders();

        applyDarkMode(view);
        initUI();
        return view;
    }

    private void updateLoadingProviders() {
        if (loadingController == null) return;
        loadingCategories = loadingController.getAllCategories();
        List<String> names = new ArrayList<>();
        names.add("-- Select Provider --");
        for (LoadingCategory c : loadingCategories) {
            names.add(c.getName() + " (₱" + String.format("%.2f", c.getBalance()) + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoadingProvider.setAdapter(adapter);
    }

    private void applyDarkMode(View v) {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);
        
        if (isDark) {
            v.findViewById(R.id.owner_consumption_root).setBackgroundColor(Color.parseColor("#121212"));
            v.findViewById(R.id.header_container).setBackgroundColor(Color.parseColor("#1A1A1A"));
            v.findViewById(R.id.section_product).setBackgroundColor(Color.parseColor("#1A1A1A"));
            v.findViewById(R.id.section_loading).setBackgroundColor(Color.parseColor("#1A1A1A"));
            v.findViewById(R.id.section_money).setBackgroundColor(Color.parseColor("#1A1A1A"));
            v.findViewById(R.id.btn_confirm).setBackgroundColor(Color.parseColor("#1A1A1A"));
            
            searchBox.setBackgroundColor(Color.parseColor("#2C2C2C"));
            searchBox.setTextColor(Color.WHITE);
            searchBox.setHintTextColor(Color.parseColor("#666666"));
            
            inputWithdrawAmount.setBackgroundColor(Color.parseColor("#2C2C2C"));
            inputWithdrawAmount.setTextColor(Color.WHITE);
            inputWithdrawAmount.setHintTextColor(Color.parseColor("#444444"));

            inputLoadingAmount.setBackgroundColor(Color.parseColor("#2C2C2C"));
            inputLoadingAmount.setTextColor(Color.WHITE);
            inputLoadingAmount.setHintTextColor(Color.parseColor("#444444"));
            
            ((TextView) v.findViewById(R.id.text_label_product)).setTextColor(Color.parseColor("#BB86FC"));
            ((TextView) v.findViewById(R.id.text_label_loading)).setTextColor(Color.parseColor("#BB86FC"));
            ((TextView) v.findViewById(R.id.text_label_money)).setTextColor(Color.parseColor("#BB86FC"));
            ((TextView) v.findViewById(R.id.header_title)).setTextColor(Color.parseColor("#BB86FC"));
        }
    }

    private void initUI() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { filterList(s.toString()); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        filterList(""); // Initial load

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processAllSelections();
            }
        });
    }

    private void filterList(String keyword) {
        productListContainer.removeAllViews();
        qtyInputs.clear();
        
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

        for (Product p : allProducts) {
            if (keyword.isEmpty() || p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                View row = LayoutInflater.from(getActivity()).inflate(R.layout.item_owner_product_select, productListContainer, false);
                
                TextView nameText = row.findViewById(R.id.text_product_name);
                TextView stockText = row.findViewById(R.id.text_stock_info);
                EditText qtyInput = row.findViewById(R.id.input_qty);
                View qtyContainer = qtyInput.getParent() instanceof View ? (View) qtyInput.getParent() : null;

                nameText.setText(p.getName());
                int stock = inventory.getStock().getStockSumById(p.getId());
                stockText.setText("Stock: " + stock);

                if (isDark) {
                    nameText.setTextColor(Color.WHITE);
                    stockText.setTextColor(Color.parseColor("#999999"));
                    qtyInput.setTextColor(Color.WHITE);
                    qtyInput.setHintTextColor(Color.parseColor("#444444"));
                    if (qtyContainer != null) qtyContainer.setBackgroundColor(Color.parseColor("#2C2C2C"));
                }

                qtyInputs.put(p.getId(), qtyInput);
                productListContainer.addView(row);
            }
        }
    }

    private void processAllSelections() {
        boolean somethingProcessed = false;
        StringBuilder report = new StringBuilder();

        // 1. Process Products
        for (Map.Entry<Integer, EditText> entry : qtyInputs.entrySet()) {
            String qtyStr = entry.getValue().getText().toString();
            if (qtyStr.isEmpty()) continue;

            int productId = entry.getKey();
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) continue;

            Product product = null;
            for (Product p : allProducts) if (p.getId() == productId) product = p;
            if (product == null) continue;

            int currentStock = inventory.getStock().getStockSumById(productId);
            if (qty > currentStock) {
                Toast.makeText(getActivity(), product.getName() + " has insufficient stock", Toast.LENGTH_SHORT).show();
                continue;
            }

            // Deduct Stock
            inventory.getStock().updateStockSum(productId, -qty);
            
            // Log as OWNER_USE expense
            double totalCost = getAverageCost(productId) * qty;
            logOwnerTransaction(totalCost, product.getName(), "CASH", "PRODUCT_USE", qty);
            
            somethingProcessed = true;
            report.append("Took ").append(qty).append("x ").append(product.getName()).append("\n");
        }

        // 2. Process Loading
        int loadingPos = spinnerLoadingProvider.getSelectedItemPosition();
        String loadingAmountStr = inputLoadingAmount.getText().toString();
        if (loadingPos > 0 && !loadingAmountStr.isEmpty()) {
            double amount = Double.parseDouble(loadingAmountStr);
            if (amount > 0) {
                LoadingCategory cat = loadingCategories.get(loadingPos - 1);
                if (cat.getBalance() >= amount) {
                    // Logic to deduct from balance for personal use
                    // We can reuse LoadingController.sell but with null subcategory
                    if (loadingController.sell(cat, null, amount, 0.0, "n/a")) {
                        // Log as OWNER_USE with special note
                        logOwnerTransaction(amount, cat.getName() + " Load", "GCASH", "LOADING_USE", 0);
                        somethingProcessed = true;
                        report.append("Used ₱").append(String.format("%.2f", amount)).append(" from ").append(cat.getName()).append(" balance\n");
                    }
                } else {
                    Toast.makeText(getActivity(), "Insufficient balance in " + cat.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 3. Process Money
        String withdrawStr = inputWithdrawAmount.getText().toString();
        if (!withdrawStr.isEmpty()) {
            double amount = Double.parseDouble(withdrawStr);
            if (amount > 0) {
                String account = spinnerAccount.getSelectedItemPosition() == 0 ? "CASH" : "GCASH";
                CapitalController capital = CapitalController.getInstance();
                
                boolean success = false;
                if (account.equals("CASH")) {
                    if (capital.getStoreCash() >= amount) {
                        capital.setStoreCash(capital.getStoreCash() - amount);
                        success = true;
                    }
                } else {
                    if (capital.getGcashBalance() >= amount) {
                        capital.setGcashBalance(capital.getGcashBalance() - amount);
                        success = true;
                    }
                }

                if (success) {
                    logOwnerTransaction(amount, "Withdrawal", account, "MONEY_USE", 0);
                    somethingProcessed = true;
                    report.append("Withdrew ₱").append(String.format("%.2f", amount)).append(" from ").append(account).append("\n");
                } else {
                    Toast.makeText(getActivity(), "Insufficient funds in " + account, Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (somethingProcessed) {
            Toast.makeText(getActivity(), "Owner use recorded:\n" + report.toString(), Toast.LENGTH_LONG).show();
            filterList(searchBox.getText().toString()); // Refresh
            inputWithdrawAmount.setText("");
            inputLoadingAmount.setText("");
            spinnerLoadingProvider.setSelection(0);
            updateLoadingProviders();
            
            if (getActivity() instanceof com.refresh.pos.ui.MainActivity) {
                ((com.refresh.pos.ui.MainActivity) getActivity()).updateAllFragments();
            }
        } else {
            Toast.makeText(getActivity(), "Nothing to record", Toast.LENGTH_SHORT).show();
        }
    }

    private void logOwnerTransaction(double amount, String description, String account, String useType, int qty) {
        FinanceController finance = FinanceController.getInstance();
        int catId = finance.findOrCreateCategory("OWNER_USE", "EXPENSE");

        // Notes format: [TYPE] [QTY] | Description
        String note = "[" + useType + "] [" + qty + "] | " + description;
        finance.addTransaction(
            catId,
            amount,
            DateTimeStrategy.getSQLDateFormat(Calendar.getInstance()),
            account,
            note,
            "EXPENSE"
        );
    }

    private double getAverageCost(int productId) {
        List<ProductLot> lots = inventory.getStock().getProductLotByProductId(productId);
        if (lots.isEmpty()) return 0;
        double totalCost = 0;
        int totalQty = 0;
        for (ProductLot lot : lots) {
            totalCost += lot.unitCost() * lot.getQuantity();
            totalQty += lot.getQuantity();
        }
        return totalQty > 0 ? totalCost / totalQty : 0;
    }

    @Override
    public void update() {
        if (isAdded()) {
            updateLoadingProviders();
        }
    }
}
