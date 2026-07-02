package com.refresh.pos.ui.loading;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.loading.LoadingCategory;
import com.refresh.pos.domain.loading.LoadingController;
import com.refresh.pos.domain.loading.LoadingSubCategory;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;

import java.util.List;
import java.util.Locale;

public class LoadingStationFragment extends UpdatableFragment {

    private LinearLayout containerProviders;
    private LoadingController loadingController;
    private TextView textTitle;
    private Button btnAddProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_loading_station, container, false);
        
        try {
            loadingController = LoadingController.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        containerProviders = view.findViewById(R.id.container_providers);
        textTitle = view.findViewById(R.id.text_title);
        btnAddProvider = view.findViewById(R.id.btn_add_provider);

        btnAddProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProviderDialog();
            }
        });

        applyDarkMode(view);
        update();
        return view;
    }

    private void applyDarkMode(View v) {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            v.findViewById(R.id.loading_root).setBackgroundColor(Color.parseColor("#121212"));
            textTitle.setTextColor(Color.WHITE);
        }
    }

    private void themeDialogView(View v) {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            v.setBackgroundColor(Color.parseColor("#121212"));
            int white = Color.WHITE;
            int gray = Color.parseColor("#999999");

            // Find all TextViews and EditTexts in the view hierarchy
            applyThemeRecursive(v, white, gray);
        }
    }

    private void applyThemeRecursive(View v, int white, int gray) {
        if (v instanceof TextView) {
            ((TextView) v).setTextColor(white);
            if (v instanceof EditText) {
                ((EditText) v).setHintTextColor(Color.parseColor("#444444"));
            }
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyThemeRecursive(vg.getChildAt(i), white, gray);
            }
        }
    }

    @Override
    public void update() {
        if (containerProviders == null || loadingController == null) return;
        containerProviders.removeAllViews();

        List<LoadingCategory> categories = loadingController.getAllCategories();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        int theme = com.refresh.pos.domain.ThemeController.getInstance(getActivity()).getTheme();
        boolean isDark = (theme == com.refresh.pos.domain.ThemeController.THEME_DARK);

        for (final LoadingCategory cat : categories) {
            View card = inflater.inflate(R.layout.item_loading_provider, containerProviders, false);
            TextView nameText = card.findViewById(R.id.text_provider_name);
            TextView balanceText = card.findViewById(R.id.text_balance);
            Button btnLoad = card.findViewById(R.id.btn_load);
            Button btnRestock = card.findViewById(R.id.btn_restock);
            Button btnDelete = card.findViewById(R.id.btn_delete_provider);

            nameText.setText(cat.getName());
            balanceText.setText(String.format(Locale.US, "₱%.2f", cat.getBalance()));

            if (cat.getBalance() <= 0) {
                balanceText.setTextColor(Color.RED);
                balanceText.setText("NO STOCK");
            } else if (cat.getBalance() < 100) {
                balanceText.setTextColor(Color.parseColor("#FFA000")); // Orange
            } else {
                balanceText.setTextColor(isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#2E7D32"));
            }

            if (isDark) {
                nameText.setTextColor(Color.parseColor("#BB86FC"));
                card.findViewById(R.id.provider_card_root).setBackgroundResource(R.drawable.product_card_bg);
                card.findViewById(R.id.provider_card_root).getBackground().setAlpha(30);
            }

            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoadDialog(cat);
                }
            });

            btnRestock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRestockDialog(cat);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Provider")
                        .setMessage("Are you sure you want to delete " + cat.getName() + "? This will also delete all its promos and transaction history.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingController.deleteCategory(cat.getId());
                                update();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            });

            containerProviders.addView(card);
        }
    }

    private void showAddProviderDialog() {
        final EditText input = new EditText(getActivity());
        input.setHint("Provider Name (e.g. Globe)");
        themeDialogView(input);
        new AlertDialog.Builder(getActivity())
            .setTitle("Add Provider")
            .setView(input)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        loadingController.addCategory(name);
                        update();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showRestockDialog(final LoadingCategory cat) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading_restock, null);
        themeDialogView(v);
        final EditText inputAmount = v.findViewById(R.id.edit_restock_amount);
        final EditText inputPaid = v.findViewById(R.id.edit_restock_paid);

        new AlertDialog.Builder(getActivity())
            .setTitle("Restock " + cat.getName())
            .setView(v)
            .setPositiveButton("Restock", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        double amount = Double.parseDouble(inputAmount.getText().toString());
                        double paid = Double.parseDouble(inputPaid.getText().toString());
                        loadingController.restock(cat, amount, paid);

                        // Log as financial expense
                        logLoadingRestockExpense(cat.getName(), paid);

                        update();
                        Toast.makeText(getActivity(), "Restocked successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void logLoadingRestockExpense(String providerName, double paid) {
        com.refresh.pos.domain.finance.FinanceController finance = com.refresh.pos.domain.finance.FinanceController.getInstance();
        int catId = finance.findOrCreateCategory("Inventory", "EXPENSE");

        if (catId != -1) {
            finance.addTransaction(
                    catId,
                    paid,
                    com.refresh.pos.domain.DateTimeStrategy.getSQLDateFormat(java.util.Calendar.getInstance()),
                    "CASH",
                    "Loading Restock: " + providerName,
                    "EXPENSE"
            );
        }
    }

    private void showLoadDialog(final LoadingCategory cat) {
        if (cat.getBalance() <= 0) {
            new AlertDialog.Builder(getActivity())
                .setTitle("No Stock")
                .setMessage("You cannot load because " + cat.getName() + " balance is 0.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading_sell, null);
        themeDialogView(v);
        final Spinner promoSpinner = v.findViewById(R.id.spinner_promos);
        final Button btnAddPromo = v.findViewById(R.id.btn_add_promo);
        final Button btnDeletePromo = v.findViewById(R.id.btn_delete_promo);
        final EditText inputCustomDeduct = v.findViewById(R.id.edit_custom_deduct);
        final EditText inputCustomSell = v.findViewById(R.id.edit_custom_sell);
        final Spinner paymentSpinner = v.findViewById(R.id.spinner_payment_method);

        final List<LoadingSubCategory> promos = loadingController.getSubCategories(cat.getId());
        final String[] promoNames = new String[promos.size() + 1];
        promoNames[0] = "-- Select Promo --";
        for (int i = 0; i < promos.size(); i++) {
            LoadingSubCategory p = promos.get(i);
            promoNames[i + 1] = p.getName() + " (₱" + p.getPrice() + ")";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, promoNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promoSpinner.setAdapter(adapter);

        String[] paymentMethods = {"CASH", "GCASH"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentSpinner.setAdapter(paymentAdapter);

        btnAddPromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPromoDialog(cat);
            }
        });

        btnDeletePromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = promoSpinner.getSelectedItemPosition();
                if (pos > 0) {
                    loadingController.deleteSubCategory(promos.get(pos - 1).getId());
                    showLoadDialog(cat); // Refresh dialog
                }
            }
        });

        new AlertDialog.Builder(getActivity())
            .setTitle("Sell Load - " + cat.getName())
            .setView(v)
            .setPositiveButton("Sell", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int pos = promoSpinner.getSelectedItemPosition();
                    double deduct, sell;
                    LoadingSubCategory sub = null;

                    if (pos > 0) {
                        sub = promos.get(pos - 1);
                        deduct = sub.getCost();
                        sell = sub.getPrice();
                    } else {
                        try {
                            deduct = Double.parseDouble(inputCustomDeduct.getText().toString());
                            sell = Double.parseDouble(inputCustomSell.getText().toString());
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid custom input", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String paymentMethod = paymentSpinner.getSelectedItem().toString();

                    if (loadingController.sell(cat, sub, deduct, sell, paymentMethod)) {
                        // Update Capital
                        com.refresh.pos.domain.capital.CapitalController capital = com.refresh.pos.domain.capital.CapitalController.getInstance();
                        if ("CASH".equals(paymentMethod)) {
                            capital.setStoreCash(capital.getStoreCash() + sell);
                        } else {
                            capital.setGcashBalance(capital.getGcashBalance() + sell);
                        }

                        update();
                        Toast.makeText(getActivity(), "Load sold successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Insufficient balance!", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddPromoDialog(final LoadingCategory cat) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading_add_promo, null);
        themeDialogView(v);
        final EditText inputName = v.findViewById(R.id.edit_promo_name);
        final EditText inputCost = v.findViewById(R.id.edit_promo_cost);
        final EditText inputPrice = v.findViewById(R.id.edit_promo_price);

        new AlertDialog.Builder(getActivity())
            .setTitle("Add Promo for " + cat.getName())
            .setView(v)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String name = inputName.getText().toString().trim();
                        double cost = Double.parseDouble(inputCost.getText().toString());
                        double price = Double.parseDouble(inputPrice.getText().toString());
                        if (!name.isEmpty()) {
                            loadingController.addSubCategory(cat.getId(), name, cost, price);
                            showLoadDialog(cat); // Re-open parent dialog
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLoadDialog(cat);
                }
            })
            .show();
    }
}
