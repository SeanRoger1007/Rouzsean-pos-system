package com.refresh.pos.ui.sale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.LineItem;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.Sale;
import com.refresh.pos.domain.sale.SaleLedger;
import com.refresh.pos.techicalservices.BluetoothPrinterService;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.MainActivity;

import java.util.Locale;

public class ReceiptPreviewActivity extends Activity {

    private LinearLayout itemsContainer;
    private TextView textId, textDate, textTotal, textPaid, textChange, textSeller;
    private TextView textAddress, textPhone;
    private Button btnDone, btnPrint;
    private Sale currentSale;
    private double amountPaid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_receipt_preview);

        itemsContainer = findViewById(R.id.receipt_items_container);
        textId = findViewById(R.id.text_receipt_id);
        textDate = findViewById(R.id.text_receipt_date);
        textTotal = findViewById(R.id.text_receipt_total);
        textPaid = findViewById(R.id.text_receipt_paid);
        textChange = findViewById(R.id.text_receipt_change);
        textSeller = findViewById(R.id.text_receipt_seller);
        textAddress = findViewById(R.id.text_receipt_address);
        textPhone = findViewById(R.id.text_receipt_phone);
        btnDone = findViewById(R.id.btn_done);
        btnPrint = findViewById(R.id.btn_print);

        int saleId = getIntent().getIntExtra("saleId", -1);
        amountPaid = getIntent().getDoubleExtra("amountPaid", 0.0);

        try {
            currentSale = SaleLedger.getInstance().getSaleById(saleId);
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (currentSale != null) {
            fillReceipt();
            applyDarkMode();
        } else {
            Toast.makeText(this, "Error loading sale data", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToSale();
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printToBluetooth();
            }
        });
    }

    private void applyDarkMode() {
        int theme = com.refresh.pos.domain.ThemeController.getInstance(this).getTheme();
        if (theme == com.refresh.pos.domain.ThemeController.THEME_DARK) {
            findViewById(R.id.receipt_preview_root).setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            findViewById(R.id.receipt_header).setBackgroundColor(android.graphics.Color.parseColor("#3F51B5"));
            findViewById(R.id.receipt_scroll).setBackgroundColor(android.graphics.Color.BLACK);
            findViewById(R.id.receipt_button_bar).setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"));
            
            ((TextView) findViewById(R.id.receipt_title)).setTextColor(android.graphics.Color.WHITE);
        }
    }

    private void printToBluetooth() {
        SharedPreferences printerPrefs = getSharedPreferences("PrinterPrefs", MODE_PRIVATE);
        final String address = printerPrefs.getString("address", null);
        
        if (address == null) {
            Toast.makeText(this, "No printer saved. Go to Settings -> Printers.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Connecting to printer...", Toast.LENGTH_SHORT).show();
        final BluetoothPrinterService service = BluetoothPrinterService.getInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = service.connect(address);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            performActualPrint(service);
                        } else {
                            Toast.makeText(ReceiptPreviewActivity.this, "Printing failed: could not connect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void performActualPrint(BluetoothPrinterService service) {
        SharedPreferences receiptPrefs = getSharedPreferences("ReceiptPrefs", MODE_PRIVATE);
        String address = receiptPrefs.getString("address", "Pob 4 Calatagan Batangas");
        String phone = receiptPrefs.getString("phone", "09270727160");
        boolean hidePhone = receiptPrefs.getBoolean("hide_phone", false);

        service.print(BluetoothPrinterService.RESET);
        service.print(BluetoothPrinterService.ALIGN_CENTER);
        service.print(BluetoothPrinterService.BOLD_ON);
        service.print(BluetoothPrinterService.SIZE_LARGE);
        service.printText("ROUZSEAN\n");
        service.printText("VARIETY STORE\n");
        service.print(BluetoothPrinterService.SIZE_NORMAL);
        service.print(BluetoothPrinterService.BOLD_OFF);
        service.printText(address + "\n");
        if (!hidePhone) service.printText("Tel: " + phone + "\n");
        
        service.print(BluetoothPrinterService.ALIGN_LEFT);
        service.printText("--------------------------------\n");
        service.printText("TRX: #" + currentSale.getId() + "\n");
        service.printText("Date: " + currentSale.getEndTime() + "\n");
        service.printText("--------------------------------\n");

        for (LineItem item : currentSale.getAllLineItem()) {
            Product p = item.getProduct();
            String name = p.getName();
            if (name.length() > 20) name = name.substring(0, 17) + "...";
            
            double qty = item.getQuantity();
            String unit = "";
            double pricePerUnit = item.getPriceAtSale();
            
            if (p.isPack()) {
                if (item.isPieceSale()) {
                    unit = " Pc";
                    pricePerUnit = p.getPiecePrice();
                    qty = Math.round(qty * p.getPiecesPerPack());
                } else {
                    unit = " Pk";
                }
            } else {
                unit = "x";
            }
            
            String qtyStr = (qty == (int)qty) ? String.valueOf((int)qty) : String.format(Locale.US, "%.2f", qty);
            String totalPrice = String.format(Locale.US, "%.2f", item.getTotalPriceAtSale());
            
            // Basic alignment for 32-char thermal paper
            service.printText(String.format("%-22s %8s\n", name, totalPrice));
            if (p.isPack()) {
                service.printText(String.format("  %s%s @ %s\n", qtyStr, unit, String.format(Locale.US, "%.2f", pricePerUnit)));
            } else {
                service.printText(String.format("  %s %s @ %s\n", qtyStr, unit, String.format(Locale.US, "%.2f", pricePerUnit)));
            }
        }

        service.printText("--------------------------------\n");
        service.print(BluetoothPrinterService.ALIGN_RIGHT);
        service.print(BluetoothPrinterService.BOLD_ON);
        service.printText("TOTAL: " + String.format(Locale.US, "PHP %.2f", currentSale.getTotal()) + "\n");
        service.print(BluetoothPrinterService.BOLD_OFF);
        service.printText("PAID: " + String.format(Locale.US, "PHP %.2f", amountPaid) + "\n");
        service.printText("CHANGE: " + String.format(Locale.US, "PHP %.2f", amountPaid - currentSale.getTotal()) + "\n");
        
        service.print(BluetoothPrinterService.ALIGN_LEFT);
        String seller = currentSale.getSellerName();
        if ("Owner".equalsIgnoreCase(seller)) {
            seller = "ROUZSEAN VARIETY STORE";
        }
        service.printText("Seller: " + seller + "\n");

        service.print(BluetoothPrinterService.ALIGN_CENTER);
        service.printNewLine();
        service.printText("Thank you for shopping!\n");
        service.printText("Owner: Rouzsean\n");
        service.printNewLine();
        service.printNewLine();
        service.printNewLine();

        Toast.makeText(this, "Receipt sent to printer", Toast.LENGTH_SHORT).show();
    }

    private void fillReceipt() {
        android.content.SharedPreferences prefs = getSharedPreferences("ReceiptPrefs", MODE_PRIVATE);
        String address = prefs.getString("address", "Pob 4 Calatagan Batangas");
        String phone = prefs.getString("phone", "09270727160");
        boolean hidePhone = prefs.getBoolean("hide_phone", false);

        textAddress.setText(address);
        if (hidePhone) {
            textPhone.setVisibility(View.GONE);
        } else {
            textPhone.setVisibility(View.VISIBLE);
            textPhone.setText("Number: (" + phone + ")");
        }

        textId.setText("TRX: #" + currentSale.getId());
        textDate.setText(currentSale.getEndTime());
        textTotal.setText(String.format(Locale.US, "₱%.2f", currentSale.getTotal()));
        textPaid.setText(String.format(Locale.US, "₱%.2f", amountPaid));
        textChange.setText(String.format(Locale.US, "₱%.2f", amountPaid - currentSale.getTotal()));
        
        String seller = currentSale.getSellerName();
        if ("Owner".equalsIgnoreCase(seller)) {
            seller = "ROUZSEAN VARIETY STORE";
        }
        textSeller.setText("Seller: " + seller);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (LineItem item : currentSale.getAllLineItem()) {
            View row = inflater.inflate(R.layout.listview_receipt_item, itemsContainer, false);
            Product p = item.getProduct();
            double qty = item.getQuantity();
            String unit = "";
            
            if (p.isPack()) {
                if (item.isPieceSale()) {
                    unit = "Pc";
                    qty = Math.round(qty * p.getPiecesPerPack());
                } else {
                    unit = "Pk";
                }
            }
            
            String qtyStr = (qty == (int)qty) ? String.valueOf((int)qty) : String.format(Locale.US, "%.2f", qty);
            if (p.isPack()) {
                ((TextView) row.findViewById(R.id.receipt_item_qty)).setText(qtyStr + unit + "x");
            } else {
                ((TextView) row.findViewById(R.id.receipt_item_qty)).setText(qtyStr + "x");
            }
            ((TextView) row.findViewById(R.id.receipt_item_name)).setText(p.getName());
            ((TextView) row.findViewById(R.id.receipt_item_price)).setText(String.format(Locale.US, "₱%.2f", item.getTotalPriceAtSale()));
            itemsContainer.addView(row);
        }
    }

    private void returnToSale() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnToSale();
    }
}
