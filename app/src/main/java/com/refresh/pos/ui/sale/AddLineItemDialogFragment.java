package com.refresh.pos.ui.sale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.refresh.pos.R;
import com.refresh.pos.domain.inventory.Inventory;
import com.refresh.pos.domain.inventory.Product;
import com.refresh.pos.domain.sale.Register;
import com.refresh.pos.techicalservices.NoDaoSetException;
import com.refresh.pos.ui.component.UpdatableFragment;

@SuppressLint("ValidFragment")
public class AddLineItemDialogFragment extends DialogFragment {

    private Product product;
    private Register register;
    private UpdatableFragment fragment;
    
    private TextView nameText;
    private TextView priceText;
    private EditText quantityBox;
    private EditText priceBox;
    private ImageView productImageView;
    private Button confirmButton;
    private Button cancelButton;

    public AddLineItemDialogFragment(UpdatableFragment fragment, Product product) {
        this.fragment = fragment;
        this.product = product;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        View v = inflater.inflate(R.layout.dialog_add_lineitem, container, false);
        
        nameText = (TextView) v.findViewById(R.id.nameText);
        priceText = (TextView) v.findViewById(R.id.priceText);
        quantityBox = (EditText) v.findViewById(R.id.quantityBox);
        priceBox = (EditText) v.findViewById(R.id.priceBox);
        productImageView = (ImageView) v.findViewById(R.id.productImageView);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);

        nameText.setText(product.getName());
        priceText.setText("Standard Price: " + product.getUnitPrice());
        priceBox.setText(product.getUnitPrice() + "");
        quantityBox.setText("1");

        if (product.getImagePath() != null && product.getImagePath().length() > 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(product.getImagePath());
            if (bitmap != null) {
                productImageView.setImageBitmap(bitmap);
            }
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qtyStr = quantityBox.getText().toString();
                String prcStr = priceBox.getText().toString();

                if (qtyStr.isEmpty() || prcStr.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(qtyStr);
                double price = Double.parseDouble(prcStr);

                // We need to handle custom price for this specific sale
                // Register.addItem(product, quantity) uses standard price.
                // Let's modify it or use updateItem after adding.
                
                com.refresh.pos.domain.inventory.LineItem item = register.addItem(product, quantity);
                register.updateItem(register.getCurrentSale().getId(), item, quantity, price);

                fragment.update();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
