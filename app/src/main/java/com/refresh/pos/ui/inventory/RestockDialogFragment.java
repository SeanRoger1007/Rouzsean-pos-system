package com.refresh.pos.ui.inventory;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.DateTimeStrategy;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.MainActivity;

@SuppressLint("ValidFragment")
public class RestockDialogFragment extends DialogFragment {

    private Product product;
    private EditText costPriceBox;
    private EditText quantityBox;
    private TextView productNameText;
    private Button confirmButton;
    private Button cancelButton;

    public RestockDialogFragment(Product product) {
        this.product = product;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_restock, container, false);

        productNameText = v.findViewById(R.id.text_product_name);
        costPriceBox = v.findViewById(R.id.edit_cost_price);
        quantityBox = v.findViewById(R.id.edit_quantity);
        confirmButton = v.findViewById(R.id.btn_confirm);
        cancelButton = v.findViewById(R.id.btn_cancel);

        productNameText.setText("Restock: " + product.getName());
        
        // Suggest latest cost price
        try {
            java.util.List<com.refresh.pos.domain.inventory.ProductLot> lots = Inventory.getInstance().getStock().getProductLotByProductId(product.getId());
            if (lots != null && !lots.isEmpty()) {
                costPriceBox.setText(String.valueOf(lots.get(lots.size() - 1).unitCost()));
            }
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initUI();
        return v;
    }

    private void initUI() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qtyStr = quantityBox.getText().toString();
                String costStr = costPriceBox.getText().toString();

                if (qtyStr.isEmpty() || costStr.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter both quantity and cost.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int qty = Integer.parseInt(qtyStr);
                    double cost = Double.parseDouble(costStr);

                    if (qty <= 0) {
                        Toast.makeText(getActivity(), "Quantity must be greater than zero.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = Inventory.getInstance().getStock().addProductLot(
                            DateTimeStrategy.getCurrentTime(),
                            qty, product, cost);

                    if (success) {
                        // Log as financial expense
                        logRestockExpense(product.getName(), qty, cost);

                        Toast.makeText(getActivity(), "Restocked " + qty + " units of " + product.getName(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).updateAllFragments();
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Failed to restock.", Toast.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Invalid number format.", Toast.LENGTH_SHORT).show();
                } catch (NoDaoSetException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Database error.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logRestockExpense(String productName, int qty, double cost) {
        com.refresh.pos.domain.finance.FinanceController finance = com.refresh.pos.domain.finance.FinanceController.getInstance();
        int invCatId = finance.findOrCreateCategory("Inventory", "EXPENSE");

        if (invCatId != -1) {
            finance.addTransaction(
                    invCatId,
                    qty * cost,
                    DateTimeStrategy.getSQLDateFormat(java.util.Calendar.getInstance()),
                    "CASH",
                    "Restock: " + productName + " (" + qty + " x " + cost + ")",
                    "EXPENSE"
            );
        }
    }
}
