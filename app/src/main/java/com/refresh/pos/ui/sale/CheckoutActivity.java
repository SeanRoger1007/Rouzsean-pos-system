package com.refresh.pos.ui.sale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.domain.inventory.LineItem;
import com.refresh.pos.domain.sale.Register;
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.staff.Staff;
import com.refresh.pos.domain.staff.StaffController;
import com.refresh.pos.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends Activity {

    private TextView subtotalText, grandTotalText, changeText;
    private EditText discountInput, cashReceivedInput;
    private ListView itemsListView;
    private Button btnCash, btnGCash, btnCharge;
    private LinearLayout cashInputContainer;
    private Spinner sellerSpinner;

    private Register register;
    private Sale currentSale;
    private double subtotal, discount, grandTotal;
    private String paymentMethod = "CASH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_checkout);

        try {
            register = Register.getInstance();
            currentSale = register.getCurrentSale();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initUI();
        applyDarkMode();
        initiateActionBar();
        updateTotals();
        showItems();
    }

    private void applyDarkMode() {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            findViewById(R.id.checkout_root).setBackgroundColor(Color.parseColor("#121212"));
            findViewById(R.id.section_order_summary).setBackgroundColor(Color.parseColor("#1A1A1A"));
            findViewById(R.id.section_breakdown).setBackgroundColor(Color.parseColor("#1A1A1A"));
            findViewById(R.id.section_payment).setBackgroundColor(Color.parseColor("#1A1A1A"));
            
            ((TextView) findViewById(R.id.label_order_summary)).setTextColor(Color.parseColor("#999999"));
            ((TextView) findViewById(R.id.label_subtotal)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.checkoutSubtotal)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.label_discount)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.label_grand_total)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.label_seller)).setTextColor(Color.parseColor("#999999"));
            ((TextView) findViewById(R.id.label_payment_method)).setTextColor(Color.parseColor("#999999"));
            ((TextView) findViewById(R.id.label_change)).setTextColor(Color.WHITE);
            
            discountInput.setTextColor(Color.WHITE);
            discountInput.setHintTextColor(Color.parseColor("#444444"));
            cashReceivedInput.setTextColor(Color.BLACK); // Black text as requested
            cashReceivedInput.setHintTextColor(Color.parseColor("#444444"));
        }
    }

    private void initiateActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Checkout");
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        }
    }

    private void initUI() {
        subtotalText = (TextView) findViewById(R.id.checkoutSubtotal);
        grandTotalText = (TextView) findViewById(R.id.checkoutGrandTotal);
        changeText = (TextView) findViewById(R.id.cashChange);
        discountInput = (EditText) findViewById(R.id.checkoutDiscount);
        cashReceivedInput = (EditText) findViewById(R.id.cashAmountReceived);
        itemsListView = (ListView) findViewById(R.id.checkoutItemsList);
        btnCash = (Button) findViewById(R.id.btnCash);
        btnGCash = (Button) findViewById(R.id.btnGCash);
        btnCharge = (Button) findViewById(R.id.btnCharge);
        cashInputContainer = (LinearLayout) findViewById(R.id.cashPaymentInput);
        sellerSpinner = (Spinner) findViewById(R.id.sellerSpinner);

        initSellerSpinner();

        subtotal = currentSale.getTotal();
        subtotalText.setText("₱" + String.format("%.2f", subtotal));

        discountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    discount = Double.parseDouble(s.toString());
                } catch (Exception e) {
                    discount = 0;
                }
                updateTotals();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        cashReceivedInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateChange();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "CASH";
                cashInputContainer.setVisibility(View.VISIBLE);
                btnCash.setBackgroundResource(R.drawable.btn_blue);
                btnGCash.setBackgroundResource(R.drawable.btn_purple);
                cashReceivedInput.setHint("Cash Received (₱)");
                updateChange();
            }
        });

        btnGCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "GCASH";
                cashInputContainer.setVisibility(View.VISIBLE); // Now visible for GCash too
                btnGCash.setBackgroundResource(R.drawable.btn_blue); 
                btnCash.setBackgroundResource(R.drawable.btn_purple);
                cashReceivedInput.setHint("GCash Amount Received (₱)");
                updateChange();
            }
        });

        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double received = 0;
                try {
                    received = Double.parseDouble(cashReceivedInput.getText().toString());
                } catch (Exception e) {}
                
                if (received < grandTotal) {
                    Toast.makeText(CheckoutActivity.this, "Insufficient amount received", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                showConfirmationDialog();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Payment");
        builder.setMessage("Are you sure you want to charge ₱" + String.format("%.2f", grandTotal) + " via " + paymentMethod + "?");
        builder.setPositiveButton("Charge", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmCharge();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateTotals() {
        grandTotal = subtotal - discount;
        if (grandTotal < 0) grandTotal = 0;
        grandTotalText.setText("₱" + String.format("%.2f", grandTotal));
        updateChange();
    }

    private void updateChange() {
        double received = 0;
        try {
            received = Double.parseDouble(cashReceivedInput.getText().toString());
        } catch (Exception e) {}
        double change = received - grandTotal;
        if (change < 0) change = 0;
        changeText.setText("₱" + String.format("%.2f", change));
    }

    private void initSellerSpinner() {
        List<String> sellers = new ArrayList<String>();
        sellers.add("Owner");
        
        String today = DateTimeStrategy.getSQLDateFormat(Calendar.getInstance());
        List<Staff> allStaff = StaffController.getInstance().getAllStaff();
        for (Staff s : allStaff) {
            if (StaffController.getInstance().hasWorkedToday(s.getId(), today)) {
                sellers.add(s.getName());
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sellers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sellerSpinner.setAdapter(adapter);
        sellerSpinner.setSelection(0); // Default to "Owner"
    }

    private void showItems() {
        List<LineItem> lineItems = currentSale.getAllLineItem();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (LineItem item : lineItems) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", item.getProduct().getName());
            
            if (item.getProduct().isPack()) {
                if (item.isPieceSale()) {
                    int pieces = (int) Math.round(item.getQuantity() * item.getProduct().getPiecesPerPack());
                    map.put("quantity", pieces + " Pieces x ₱" + String.format("%.2f", item.getProduct().getPiecePrice()));
                } else {
                    map.put("quantity", (int)item.getQuantity() + " Pack x ₱" + item.getPriceAtSale());
                }
            } else {
                map.put("quantity", (int)item.getQuantity() + "x ₱" + item.getPriceAtSale());
            }

            map.put("price", "₱" + String.format("%.2f", item.getTotalPriceAtSale()));
            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.listview_lineitem,
                new String[]{"name", "quantity", "price"},
                new int[]{R.id.name, R.id.quantity, R.id.price}) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int theme = com.refresh.pos.domain.ThemeController.getInstance(CheckoutActivity.this).getTheme();
                if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
                    ((TextView) view.findViewById(R.id.name)).setTextColor(Color.WHITE);
                    ((TextView) view.findViewById(R.id.quantity)).setTextColor(Color.WHITE);
                    ((TextView) view.findViewById(R.id.price)).setTextColor(Color.WHITE);
                }
                return view;
            }
        };

        itemsListView.setAdapter(adapter);
    }

    private void confirmCharge() {
        currentSale.setPaymentMethod(paymentMethod);
        currentSale.setSellerName(sellerSpinner.getSelectedItem().toString());
        
        int saleId = currentSale.getId();
        register.endSale(DateTimeStrategy.getCurrentTime());

        // Update balances
        com.refresh.pos.domain.capital.CapitalController capital = com.refresh.pos.domain.capital.CapitalController.getInstance();
        double paid = grandTotal;
        if ("CASH".equals(paymentMethod)) {
            capital.setStoreCash(capital.getStoreCash() + grandTotal);
            try {
                paid = Double.parseDouble(cashReceivedInput.getText().toString());
            } catch (Exception e) {}
        } else if ("GCASH".equals(paymentMethod)) {
            capital.setGcashBalance(capital.getGcashBalance() + grandTotal);
        }

        Intent intent = new Intent(this, ReceiptPreviewActivity.class);
        intent.putExtra("saleId", saleId);
        intent.putExtra("amountPaid", paid);
        setResult(RESULT_OK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
